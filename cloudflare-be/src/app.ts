import { Hono } from "hono";
import { successResponse } from "./lib/api-response";
import { createAuthRoute } from "./routes/auth";
import { createEventRoute } from "./routes/events";
import { createFinanceReportRoute } from "./routes/finance-reports";
import { healthRoute } from "./routes/health";
import { createMemberRoute } from "./routes/members";
import { createPaymentObligationRoute } from "./routes/payment-obligations";
import { createArisanParticipantRoute } from "./routes/arisan-participants";
import { createUserRoute } from "./routes/users";
import { createWorshipTemplateRoute } from "./routes/worship-templates";
import type { AppBindings } from "./types/env";

export const app = new Hono<{ Bindings: AppBindings }>();

app.route("/health", healthRoute);
app.route("/auth", createAuthRoute());
app.route("/v1/events", createEventRoute());
app.route("/v1/worship-templates", createWorshipTemplateRoute());
app.route("/v1/members", createMemberRoute());
app.route("/v1/finance-reports", createFinanceReportRoute());
app.route("/v1/payment-obligations", createPaymentObligationRoute());
app.route("/v1/arisan/participants", createArisanParticipantRoute());
app.route("/v1/users", createUserRoute());
app.post("/v1/device/fcm-token", async (c) => {
  return successResponse(null, { message: "FCM token synced" });
});

app.get("/", (c) => {
  return successResponse({
    service: "bakti-marsada-be"
  });
});
