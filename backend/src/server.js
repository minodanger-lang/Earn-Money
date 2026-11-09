import { createServer } from "node:http";

const port = Number(process.env.PORT || 3000);

const server = createServer((req, res) => {
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");

  if (req.method === "GET" && req.url === "/health") {
    res.writeHead(200);
    res.end(JSON.stringify({
      ok: true,
      service: "earn-money-api",
      environment: process.env.NODE_ENV || "development"
    }));
    return;
  }

  res.writeHead(404);
  res.end(JSON.stringify({ ok: false, error: "NOT_FOUND" }));
});

server.listen(port, "0.0.0.0", () => {
  console.log(`Earn Money API listening on port ${port}`);
});
