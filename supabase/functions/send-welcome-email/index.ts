// supabase/functions/send-welcome-email/index.ts
//
// Sends the "Welcome to Sachio Express" email via Resend. A Supabase
// Database Webhook calls this URL directly whenever a new row is inserted
// into public.profiles (i.e. someone just signed up).
//
// Deploy:   supabase functions deploy send-welcome-email --no-verify-jwt
// Secrets:  supabase secrets set RESEND_API_KEY=re_xxxxx
//           supabase secrets set FROM_EMAIL="Sachio Express <welcome@sachioexpress.com>"
//           supabase secrets set LOGO_URL=https://sachioexpress.com/logo.png
//           supabase secrets set WEBHOOK_SECRET=<a long random string>
//
// --no-verify-jwt is required because the Database Webhook calls this
// without a Supabase auth token - security instead comes from checking the
// x-webhook-secret header below, which you set as a custom HTTP header on
// the webhook itself (Database -> Webhooks -> your webhook -> HTTP Headers).

const RESEND_API_KEY = Deno.env.get("RESEND_API_KEY")!;
const FROM_EMAIL = Deno.env.get("FROM_EMAIL") ?? "Sachio Express <welcome@sachioexpress.com>";
const LOGO_URL = Deno.env.get("LOGO_URL") ?? "https://sachioexpress.com/logo.png";
const WEBHOOK_SECRET = Deno.env.get("WEBHOOK_SECRET")!;

function escapeHtml(str: string): string {
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

function welcomeEmailHtml(name: string): string {
  return `
  <div style="background-color:#f4f4f5;padding:32px 16px;font-family:Arial,Helvetica,sans-serif;">
    <div style="max-width:480px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;">
      <div style="background-color:#000000;padding:32px;text-align:center;">
        <img src="${LOGO_URL}" alt="Sachio Express" width="72" style="display:block;margin:0 auto 12px;" />
        <p style="color:#D2CC46;font-size:12px;letter-spacing:2px;font-weight:bold;margin:0;">SACHIO EXPRESS</p>
      </div>
      <div style="padding:32px;">
        <h1 style="font-size:20px;color:#111111;margin:0 0 12px;">Welcome, ${escapeHtml(name)}!</h1>
        <p style="font-size:14px;color:#444444;line-height:1.6;margin:0 0 20px;">
          Your Sachio Express account is ready. You can now browse our catalog,
          request quotations, and track your bookings right from the app -
          "Prestige in Every Flush."
        </p>
        <p style="font-size:13px;color:#888888;line-height:1.6;margin:0;">
          If you didn't create this account, you can safely ignore this email.
        </p>
      </div>
      <div style="background-color:#fafafa;padding:16px 32px;text-align:center;">
        <p style="font-size:11px;color:#aaaaaa;margin:0;">
          Need help? Reply to this email or contact
          <a href="mailto:hello@sachioexpress.com" style="color:#D2CC46;">hello@sachioexpress.com</a>
        </p>
      </div>
    </div>
  </div>`;
}

Deno.serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405 });
  }

  const providedSecret = req.headers.get("x-webhook-secret") ?? "";
  if (!WEBHOOK_SECRET || providedSecret !== WEBHOOK_SECRET) {
    return new Response(JSON.stringify({ error: "Invalid or missing webhook secret" }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  }

  const payload = await req.json();
  const record = payload?.record;
  const email = record?.email as string | undefined;
  const name = (record?.full_name || record?.name || "there") as string;

  if (!email) {
    // Not fatal - just nothing useful to email. Acknowledge so Supabase
    // doesn't keep retrying a payload that will never have an email.
    return new Response(JSON.stringify({ skipped: true, reason: "No email on record" }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  }

  const resendRes = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${RESEND_API_KEY}`,
    },
    body: JSON.stringify({
      from: FROM_EMAIL,
      to: email,
      subject: "Welcome to Sachio Express",
      html: welcomeEmailHtml(name),
    }),
  });

  if (!resendRes.ok) {
    const errText = await resendRes.text();
    console.error("Resend send failed:", errText);
    // 500 so Supabase's webhook delivery retries this (transient email
    // provider hiccups shouldn't silently drop a welcome email).
    return new Response(JSON.stringify({ error: "Failed to send welcome email" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }

  return new Response(JSON.stringify({ sent: true }), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
});
