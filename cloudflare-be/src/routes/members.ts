import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import { createMemberService, type MemberService } from "../services/member.service";
import type { AppBindings } from "../types/env";

type MemberRouteDependencies = {
  createService?: (db: D1Database) => MemberService;
};

export function createMemberRoute(deps: MemberRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createMemberService;

  route.get("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const result = await service.list(c.req.query("sectorId") ?? "");
      return successResponse(result);
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.post("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const result = await service.save(await c.req.json());
      return successResponse(result);
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.delete("/:id", async (c) => {
    try {
      const service = createService(c.env.DB);
      await service.delete(c.req.param("id"));
      return successResponse(null, { message: "Member deleted" });
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
  console.error("member_unhandled_error", error);
  return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
}
