import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import { createWorshipTemplateService, type WorshipTemplateService } from "../services/worship-template.service";
import type { AppBindings } from "../types/env";

type WorshipTemplateRouteDependencies = {
  createService?: (db: D1Database) => WorshipTemplateService;
};

export function createWorshipTemplateRoute(deps: WorshipTemplateRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createWorshipTemplateService;

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
      return successResponse(null, { message: "Worship template deleted" });
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
  console.error("worship_template_unhandled_error", error);
  return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
}
