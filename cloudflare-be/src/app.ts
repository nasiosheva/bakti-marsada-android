import { Hono } from "hono";
import { createAuthRoute } from "./routes/auth";
import { healthRoute } from "./routes/health";
import type { AppBindings } from "./types/env";

export const app = new Hono<{ Bindings: AppBindings }>();

app.route("/health", healthRoute);
app.route("/auth", createAuthRoute());

app.get("/", (c) => {
  return c.json({
    ok: true,
    service: "bakti-marsada-be"
  });
});
