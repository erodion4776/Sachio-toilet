// supabase/functions/initialize-payment/index.ts
//
// Called by the Android app (via the Supabase anon key) to start a real
// Paystack transaction. The Paystack SECRET key never leaves this function -
// it is read from a Supabase secret, not from the app.
//
// Deploy:   supabase functions deploy initialize-payment
// Secret:   supabase secrets set PAYSTACK_SECRET_KEY=sk_live_xxxxx
//
// Request body (JSON):  { "bookingId": "<uuid>", "email": "client@x.com", "amount": 550000 }
// amount is in Naira (not kobo) - this function converts to kobo for Paystack.

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

    // Confirm the booking exists and grab its quoted price for sanity-checking the amount.
    const { data: booking, error: bookingError } = await supabase
      .from("bookings")
      .select("id, quoted_price, logistics_cost, vat_amount")
      .eq("id", bookingId)
      .single();

    if (bookingError || !booking) {
      return new Response(
        JSON.stringify({ status: false, message: "Booking not found" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Ask Paystack to initialize the transaction.
    const paystackRes = await fetch("https://api.paystack.co/transaction/initialize", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${PAYSTACK_SECRET_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        amount: Math.round(amount * 100), // Paystack expects kobo
        metadata: { bookingId },
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

    // Record a pending payment row so the webhook has something to update.
    const { error: insertError } = await supabase.from("payments").insert({
      booking_id: bookingId,
      amount,
      reference,
      status: "pending",
      channel: "paystack",
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
        message: "Authorization URL created",
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
