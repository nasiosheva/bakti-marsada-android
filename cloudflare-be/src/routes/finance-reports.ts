import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import { createFinanceReportService, type FinanceReportService } from "../services/finance-report.service";
import type { AppBindings } from "../types/env";

type FinanceReportRouteDependencies = {
  createService?: (db: D1Database) => FinanceReportService;
};

export function createFinanceReportRoute(deps: FinanceReportRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createFinanceReportService;

  route.get("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      return successResponse(await service.list(c.req.query("sectorId") ?? ""));
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.post("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      return successResponse(await service.save(await c.req.json()));
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.delete("/:id", async (c) => {
    try {
      const service = createService(c.env.DB);
      await service.delete(c.req.param("id"));
      return successResponse(null, { message: "Finance report deleted" });
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
  console.error("finance_report_unhandled_error", error);
  return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
}
