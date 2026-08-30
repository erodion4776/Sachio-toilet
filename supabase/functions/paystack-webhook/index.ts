// supabase/functions/paystack-webhook/index.ts
//
// Deploy:   supabase functions deploy paystack-webhook --no-verify-jwt
// In Paystack Dashboard -> Settings -> Preferences -> Webhooks:
// Set Webhook URL to: https://<your-project>.supabase.co/functions/v1/paystack-webhook

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { crypto } from "https://deno.land/std@0.224.0/crypto/mod.ts";

const PAYSTACK_SECRET_KEY = Deno.env.get("PAYSTACK_SECRET_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405 });
  }

  try {
    const rawBody = await req.text();
    const signature = req.headers.get("x-paystack-signature");

    // 1. Verify Paystack HMAC-SHA512 Signature
    const encoder = new TextEncoder();
    const key = await crypto.subtle.importKey(
      "raw",
      encoder.encode(PAYSTACK_SECRET_KEY),
      { name: "HMAC", hash: "SHA-512" },
      false,
      ["sign"]
    );
    const signatureBuffer = await crypto.subtle.sign(
      "HMAC",
      key,
      encoder.encode(rawBody)
    );
    const hashArray = Array.from(new Uint8Array(signatureBuffer));
    const computedSignature = hashArray
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");

    if (computedSignature !== signature) {
      return new Response("Invalid signature", { status: 401 });
    }

    const payload = JSON.parse(rawBody);

    // 2. Handle successful charge event
    if (payload.event === "charge.success") {
      const data = payload.data;
      const reference = data.reference;
      const amount = data.amount / 100; // Convert Kobo back to Naira
      const bookingId = data.metadata?.bookingId;

      const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

      // Update Payments table
      await supabase
        .from("payments")
        .update({
          status: "success",
          channel: data.channel || "card",
          payment_date: new Date().toISOString(),
        })
        .eq("reference", reference);

      // Update Bookings table
      if (bookingId) {
        await supabase
          .from("bookings")
          .update({
            payment_status: "success",
            status: "Payment Confirmed",
          })
          .eq("id", bookingId);
      }
    }

    return new Response(JSON.stringify({ received: true }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (err) {
    return new Response(`Webhook handler error: ${(err as Error).message}`, {
      status: 500,
    });
  }
});
