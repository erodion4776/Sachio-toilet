// supabase/functions/initialize-payment/index.ts
//
// Called by the Android app (via the Supabase anon key) to start a real
// Paystack transaction. The Paystack SECRET key never leaves this function.
//
// Deploy:   supabase functions deploy initialize-payment
// Secret:   supabase secrets set PAYSTACK_SECRET_KEY=sk_live_xxxxx
//
// Request body (JSON):  { "bookingId": "<uuid>", "email": "client@x.com", "amount": 550000 }

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const PAYSTACK_SECRET_KEY = Deno.env.get("PAYSTACK_SECRET_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const { bookingId, email, amount } = await req.json();

    if (!bookingId || !email || !amount || amount <= 0) {
      return new Response(
        JSON.stringify({ status: false, message: "bookingId, email, and a positive amount are required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

    // 1. Confirm the booking exists in database
    const { data: booking, error: bookingError } = await supabase
      .from("bookings")
      .select("id, client_name, service_type, quoted_price, logistics_cost, vat_amount")
      .eq("id", bookingId)
      .single();

    if (bookingError || !booking) {
      return new Response(
        JSON.stringify({ status: false, message: "Booking record not found in database" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // 2. Initialize transaction on Paystack
    const paystackRes = await fetch("https://api.paystack.co/transaction/initialize", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${PAYSTACK_SECRET_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email: email.trim().toLowerCase(),
        amount: Math.round(amount * 100), // Paystack accepts amount in Kobo
        metadata: {
          bookingId,
          clientName: booking.client_name,
          serviceType: booking.service_type,
          custom_fields: [
            {
              display_name: "Booking Ref",
              variable_name: "booking_id",
              value: bookingId,
            },
            {
              display_name: "Service Class",
              variable_name: "service_type",
              value: booking.service_type,
            }
          ]
        },
        // Directs browser automatically back to Android App on completion
        callback_url: "sachioexpress://payment-complete",
      }),
    });

    const paystackJson = await paystackRes.json();

    if (!paystackRes.ok || !paystackJson.status) {
      return new Response(
        JSON.stringify({ status: false, message: paystackJson.message ?? "Paystack initialization failed" }),
        { status: 502, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const reference = paystackJson.data.reference as string;
    const authorizationUrl = paystackJson.data.authorization_url as string;

    // 3. Record pending payment row in Supabase
    const { error: insertError } = await supabase.from("payments").insert({
      booking_id: bookingId,
      amount,
      reference,
      status: "pending",
      channel: "paystack",
      customer_email: email.trim().toLowerCase(),
      payment_date: new Date().toISOString(),
    });

    if (insertError) {
      return new Response(
        JSON.stringify({ status: false, message: `Payment record failed: ${insertError.message}` }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    return new Response(
      JSON.stringify({
        status: true,
        message: "Authorization URL created successfully",
        authorizationUrl,
        reference,
        amount,
      }),
      { status: 200, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  } catch (err) {
    return new Response(
      JSON.stringify({ status: false, message: `Unexpected error: ${(err as Error).message}` }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }
});
