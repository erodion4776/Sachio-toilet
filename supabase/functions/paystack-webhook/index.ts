// supabase/functions/paystack-webhook/index.ts
//
// Paystack calls this URL directly (the app never calls it). Set this as
// your webhook URL in the Paystack dashboard:
//   https://<project-ref>.supabase.co/functions/v1/paystack-webhook
//
// Deploy:   supabase functions deploy paystack-webhook --no-verify-jwt
// Secret:   supabase secrets set PAYSTACK_SECRET_KEY=sk_live_xxxxx
//
// --no-verify-jwt is required because Paystack calls this without a
// Supabase auth token - security instead comes from verifying Paystack's
// HMAC-SHA512 signature below.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const PAYSTACK_SECRET_KEY = Deno.env.get("PAYSTACK_SECRET_KEY")!;
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

async function hmacSha512Hex(secret: string, payload: string): Promise<string> {
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    { name: "HMAC", hash: "SHA-512" },
    false,
    ["sign"],
  );
  const sigBuffer = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(payload));
  return Array.from(new Uint8Array(sigBuffer))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405 });
  }

  const rawBody = await req.text();
  const signature = req.headers.get("x-paystack-signature") ?? "";

  const computedSignature = await hmacSha512Hex(PAYSTACK_SECRET_KEY, rawBody);
  if (signature !== computedSignature) {
    return new Response(JSON.stringify({ status: "error", message: "Invalid signature" }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  }

  const event = JSON.parse(rawBody);
  const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);

  if (event.event === "charge.success") {
    const reference = event.data.reference as string;

    const { data: payment } = await supabase
      .from("payments")
      .update({ status: "success" })
      .eq("reference", reference)
      .select("booking_id")
      .single();

    if (payment) {
      await supabase
        .from("bookings")
        .update({
          payment_status: "success",
          payment_ref: reference,
          status: "Booking Confirmed",
        })
        .eq("id", payment.booking_id);
    }
  } else if (event.event === "charge.failed") {
    const reference = event.data.reference as string;
    await supabase.from("payments").update({ status: "failed" }).eq("reference", reference);
  }

  return new Response(JSON.stringify({ status: "success", message: "Webhook handled successfully" }), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
});
