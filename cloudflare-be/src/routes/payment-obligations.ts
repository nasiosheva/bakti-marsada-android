import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import { createPaymentObligationService, type PaymentObligationService } from "../services/payment-obligation.service";
import type { AppBindings } from "../types/env";

type PaymentObligationRouteDependencies = {
  createService?: (db: D1Database) => PaymentObligationService;
};

export function createPaymentObligationRoute(deps: PaymentObligationRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createPaymentObligationService;

  route.get("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const sectorId = c.req.query("sectorId") ?? "";
      const result = await service.list(sectorId);
      return successResponse(result);
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.post("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const body = await c.req.json<{
        id?: string;
        memberId?: string;
        memberName?: string;
        title?: string;
        description?: string;
        amount?: number;
        dueDate?: string;
        status?: string;
        sectorId?: string;
        sectorName?: string;
      }>();

      const result = await service.save(body);
      return successResponse(result);
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.delete("/:id", async (c) => {
    try {
      const service = createService(c.env.DB);
      await service.delete(c.req.param("id"));
      return successResponse(null, { message: "Payment obligation deleted" });
    } catch (error) {
      return mapRouteError(error);
    }
  });

  return route;
}

function mapRouteError(error: unknown) {
  if (error instanceof AuthError) {
    return errorResponse(error.status, error.code, error.message);
  }

  console.error("payment_obligation_unhandled_error", error);
  return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
}
