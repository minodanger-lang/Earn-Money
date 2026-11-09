import { createServer } from "node:http";
import { createClient } from "@supabase/supabase-js";

const port = Number(process.env.PORT || 3000);
const supabaseUrl = process.env.SUPABASE_URL;
const supabaseAnonKey = process.env.SUPABASE_ANON_KEY;
const supabaseServiceRoleKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
const allowedOrigins = (process.env.ALLOWED_ORIGINS || "")
  .split(",").map((value) => value.trim()).filter(Boolean);

const configured = Boolean(supabaseUrl && supabaseAnonKey && supabaseServiceRoleKey);
const authClient = configured
  ? createClient(supabaseUrl, supabaseAnonKey, { auth: { persistSession: false, autoRefreshToken: false } })
  : null;
const adminClient = configured
  ? createClient(supabaseUrl, supabaseServiceRoleKey, { auth: { persistSession: false, autoRefreshToken: false } })
  : null;

const rateBuckets = new Map();
const MAX_BODY_BYTES = 1024 * 1024;

function send(res, status, payload) {
  res.writeHead(status, { "Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store" });
  res.end(JSON.stringify(payload));
}

function applyCors(req, res) {
  const origin = req.headers.origin;
  if (origin && allowedOrigins.includes(origin)) {
    res.setHeader("Access-Control-Allow-Origin", origin);
    res.setHeader("Vary", "Origin");
  }
  res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
}

function rateLimit(req, res) {
  const key = req.socket.remoteAddress || "unknown";
  const now = Date.now();
  const current = rateBuckets.get(key);
  if (!current || now - current.startedAt > 60_000) {
    rateBuckets.set(key, { startedAt: now, count: 1 });
    return true;
  }
  current.count += 1;
  if (current.count > 60) {
    send(res, 429, { ok: false, error: "RATE_LIMITED" });
    return false;
  }
  return true;
}

async function readJson(req) {
  const chunks = [];
  let size = 0;
  for await (const chunk of req) {
    size += chunk.length;
    if (size > MAX_BODY_BYTES) throw Object.assign(new Error("BODY_TOO_LARGE"), { status: 413 });
    chunks.push(chunk);
  }
  if (!chunks.length) return {};
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch {
    throw Object.assign(new Error("INVALID_JSON"), { status: 400 });
  }
}

function requireBackend(res) {
  if (configured) return true;
  send(res, 503, { ok: false, error: "BACKEND_NOT_CONFIGURED", message: "Server database and authentication settings are not configured." });
  return false;
}

async function requireUser(req, res) {
  if (!requireBackend(res)) return null;
  const authorization = req.headers.authorization || "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  if (!token) {
    send(res, 401, { ok: false, error: "UNAUTHORIZED" });
    return null;
  }
  const { data, error } = await authClient.auth.getUser(token);
  if (error || !data?.user) {
    send(res, 401, { ok: false, error: "INVALID_SESSION" });
    return null;
  }
  return data.user;
}

async function requireAdmin(req, res) {
  const user = await requireUser(req, res);
  if (!user) return null;
  const adminIds = (process.env.ADMIN_USER_IDS || "").split(",").map((value) => value.trim()).filter(Boolean);
  if (!adminIds.includes(user.id)) {
    send(res, 403, { ok: false, error: "FORBIDDEN" });
    return null;
  }
  return user;
}

async function handle(req, res) {
  applyCors(req, res);
  if (req.method === "OPTIONS") return send(res, 204, {});
  if (!rateLimit(req, res)) return;

  const url = new URL(req.url || "/", "http://localhost");
  const path = url.pathname;

  if (req.method === "GET" && (path === "/health" || path === "/api/health")) {
    return send(res, 200, {
      ok: true,
      service: "earn-money-api",
      environment: process.env.NODE_ENV || "development",
      databaseConfigured: configured
    });
  }

  if (req.method === "POST" && path === "/api/auth/register") {
    if (!requireBackend(res)) return;
    const body = await readJson(req);
    const email = typeof body.email === "string" ? body.email.trim().toLowerCase() : "";
    const password = typeof body.password === "string" ? body.password : "";
    const displayName = typeof body.displayName === "string" ? body.displayName.trim().slice(0, 80) : "";
    if (!email || password.length < 8 || !displayName) {
      return send(res, 400, { ok: false, error: "INVALID_REGISTRATION", message: "أدخل بريدًا صحيحًا وكلمة مرور لا تقل عن 8 أحرف والاسم." });
    }
    const { data, error } = await authClient.auth.signUp({
      email,
      password,
      options: { data: { display_name: displayName } }
    });
    if (error) return send(res, 400, { ok: false, error: "REGISTRATION_FAILED", message: "تعذر إنشاء الحساب. تحقق من البيانات أو إعدادات تأكيد البريد." });
    return send(res, 201, {
      ok: true,
      message: data.session ? "تم إنشاء الحساب." : "تم إنشاء الحساب. تحقق من بريدك لتأكيده قبل تسجيل الدخول.",
      user: data.user ? { id: data.user.id, email: data.user.email } : null,
      session: data.session ? {
        accessToken: data.session.access_token,
        refreshToken: data.session.refresh_token,
        expiresIn: data.session.expires_in
      } : null
    });
  }

  if (req.method === "POST" && path === "/api/auth/login") {
    if (!requireBackend(res)) return;
    const body = await readJson(req);
    const email = typeof body.email === "string" ? body.email.trim().toLowerCase() : "";
    const password = typeof body.password === "string" ? body.password : "";
    if (!email || !password) return send(res, 400, { ok: false, error: "MISSING_CREDENTIALS" });
    const { data, error } = await authClient.auth.signInWithPassword({ email, password });
    if (error || !data.session) return send(res, 401, { ok: false, error: "LOGIN_FAILED", message: "بيانات الدخول غير صحيحة أو الحساب غير مؤكد." });
    return send(res, 200, {
      ok: true,
      user: { id: data.user.id, email: data.user.email },
      session: {
        accessToken: data.session.access_token,
        refreshToken: data.session.refresh_token,
        expiresIn: data.session.expires_in
      }
    });
  }

  if (req.method === "GET" && path === "/api/me") {
    const user = await requireUser(req, res);
    if (!user) return;
    const { data: profile, error } = await adminClient
      .from("profiles").select("id, display_name, created_at").eq("id", user.id).maybeSingle();
    if (error) return send(res, 500, { ok: false, error: "PROFILE_READ_FAILED" });
    return send(res, 200, {
      ok: true,
      user: { id: user.id, email: user.email, profile: profile || null }
    });
  }

  if (req.method === "GET" && path === "/api/tasks") {
    const user = await requireUser(req, res);
    if (!user) return;
    const { data, error } = await adminClient
      .from("tasks")
      .select("id, title, description, category, terms_url")
      .eq("is_active", true)
      .order("created_at", { ascending: false })
      .limit(50);
    if (error) return send(res, 500, { ok: false, error: "TASKS_READ_FAILED" });
    return send(res, 200, { ok: true, tasks: data || [] });
  }

  if (req.method === "GET" && path === "/api/wallet") {
    const user = await requireUser(req, res);
    if (!user) return;
    const { data: balances, error: balanceError } = await adminClient
      .from("wallet_balances").select("pending_amount, available_amount")
      .eq("user_id", user.id).maybeSingle();
    if (balanceError) return send(res, 500, { ok: false, error: "WALLET_READ_FAILED" });
    const { data: transactions, error: ledgerError } = await adminClient
      .from("wallet_ledger")
      .select("id, entry_type, bucket, amount, currency, description, created_at")
      .eq("user_id", user.id)
      .order("created_at", { ascending: false })
      .limit(30);
    if (ledgerError) return send(res, 500, { ok: false, error: "LEDGER_READ_FAILED" });
    return send(res, 200, {
      ok: true,
      wallet: {
        pendingAmount: balances?.pending_amount || "0.00",
        availableAmount: balances?.available_amount || "0.00",
        currency: "EGP"
      },
      transactions: transactions || []
    });
  }

  if (req.method === "GET" && path === "/api/admin/withdrawals") {
    const admin = await requireAdmin(req, res);
    if (!admin) return;
    const { data, error } = await adminClient
      .from("withdrawal_requests")
      .select("id, user_id, amount, currency, payout_method, status, created_at")
      .in("status", ["requested", "reviewing"])
      .order("created_at", { ascending: true })
      .limit(100);
    if (error) return send(res, 500, { ok: false, error: "WITHDRAWALS_READ_FAILED" });
    return send(res, 200, { ok: true, withdrawals: data || [] });
  }

  if (req.method === "GET" && path === "/api/admin/users") {
    const admin = await requireAdmin(req, res);
    if (!admin) return;
    const { data, error } = await adminClient
      .from("profiles")
      .select("id, display_name, created_at")
      .order("created_at", { ascending: false })
      .limit(100);
    if (error) return send(res, 500, { ok: false, error: "USERS_READ_FAILED" });
    return send(res, 200, { ok: true, users: data || [] });
  }

  if (req.method === "POST" && path === "/api/admin/tasks") {
    const admin = await requireAdmin(req, res);
    if (!admin) return;
    const body = await readJson(req);
    const provider = typeof body.provider === "string" ? body.provider.trim().slice(0, 60) : "";
    const providerTaskId = typeof body.providerTaskId === "string" ? body.providerTaskId.trim().slice(0, 120) : "";
    const title = typeof body.title === "string" ? body.title.trim().slice(0, 120) : "";
    const description = typeof body.description === "string" ? body.description.trim().slice(0, 1000) : "";
    const category = typeof body.category === "string" ? body.category.trim().slice(0, 60) : "general";
    const termsUrl = typeof body.termsUrl === "string" ? body.termsUrl.trim().slice(0, 500) : null;
    if (!provider || !providerTaskId || !title) {
      return send(res, 400, { ok: false, error: "INVALID_TASK" });
    }
    const { data, error } = await adminClient.from("tasks").insert({
      provider, provider_task_id: providerTaskId, title, description,
      category, terms_url: termsUrl || null, is_active: false
    }).select("id, provider, provider_task_id, title, is_active").single();
    if (error) return send(res, 400, { ok: false, error: "TASK_CREATE_FAILED" });
    return send(res, 201, { ok: true, task: data, message: "تم إنشاء المهمة كغير نشطة. لا تنشرها قبل التأكد من موافقة المزود." });
  }

  if (req.method === "POST" && /^\/api\/admin\/withdrawals\/[0-9a-f-]+\/review$/i.test(path)) {
    const admin = await requireAdmin(req, res);
    if (!admin) return;
    const id = path.split("/")[4];
    const { data, error } = await adminClient
      .from("withdrawal_requests")
      .update({ status: "reviewing", updated_at: new Date().toISOString() })
      .eq("id", id).eq("status", "requested")
      .select("id, status").maybeSingle();
    if (error) return send(res, 500, { ok: false, error: "WITHDRAWAL_REVIEW_FAILED" });
    if (!data) return send(res, 409, { ok: false, error: "WITHDRAWAL_NOT_PENDING" });
    return send(res, 200, { ok: true, withdrawal: data });
  }

  if (req.method === "POST" && path === "/api/withdrawals") {
    return send(res, 503, {
      ok: false,
      error: "WITHDRAWALS_DISABLED",
      message: "طلبات السحب غير مفعّلة حتى اعتماد مزود الدفع واختبار التحويلات الفعلية."
    });
  }

  return send(res, 404, { ok: false, error: "NOT_FOUND" });
}

export function handler(req, res) {
  handle(req, res).catch((error) => {
    const status = Number.isInteger(error.status) ? error.status : 500;
    send(res, status, {
      ok: false,
      error: status === 500 ? "INTERNAL_SERVER_ERROR" : error.message
    });
  });
}

export default handler;

// Vercel invokes the exported handler; local development uses the standalone HTTP server.
if (!process.env.VERCEL) {
  createServer(handler).listen(port, "0.0.0.0", () => {
    console.log(`Earn Money API listening on port ${port}`);
  });
}
