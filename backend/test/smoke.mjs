import assert from "node:assert/strict";
import { spawn } from "node:child_process";

const port = 3187;
const child = spawn(process.execPath, ["src/server.js"], {
  cwd: new URL("..", import.meta.url),
  env: { ...process.env, PORT: String(port), NODE_ENV: "test" },
  stdio: "ignore"
});

const baseUrl = `http://127.0.0.1:${port}`;
let started = false;

try {
  for (let attempt = 0; attempt < 40; attempt += 1) {
    if (child.exitCode !== null) throw new Error("API process exited before becoming ready");
    try {
      const response = await fetch(`${baseUrl}/health`);
      if (response.ok) { started = true; break; }
    } catch {
      await new Promise((resolve) => setTimeout(resolve, 250));
    }
  }

  assert.equal(started, true, "API should start and respond to health checks");

  const healthResponse = await fetch(`${baseUrl}/health`);
  const health = await healthResponse.json();
  assert.equal(health.ok, true);
  assert.equal(health.service, "earn-money-api");
  assert.equal(health.databaseConfigured, false);

  const protectedResponse = await fetch(`${baseUrl}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: "test@example.com", password: "password123", displayName: "Test" })
  });
  assert.equal(protectedResponse.status, 503, "Auth must fail closed without Supabase configuration");

  const missingResponse = await fetch(`${baseUrl}/does-not-exist`);
  assert.equal(missingResponse.status, 404);

  process.stdout.write("Backend smoke tests passed.\n");
} finally {
  child.kill("SIGTERM");
}
