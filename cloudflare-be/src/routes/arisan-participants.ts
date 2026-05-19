import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import {
  createArisanParticipantService,
  type ArisanParticipantService
} from "../services/arisan-participant.service";
import type { AppBindings } from "../types/env";

type ArisanParticipantRouteDependencies = {
  createService?: (db: D1Database) => ArisanParticipantService;
};

export function createArisanParticipantRoute(deps: ArisanParticipantRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createArisanParticipantService;

  route.get("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const result = await service.list(c.req.query("sectorId") ?? "");
      return successResponse(result);
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.put("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const result = await service.replace(await c.req.json());
      return successResponse(result);
    } catch (error) {
      return mapRouteError(error);
    }
  });

  route.post("/fill-from-members", async (c) => {
    try {
      const service = createService(c.env.DB);
      const result = await service.fillFromMembers(await c.req.json());
      return successResponse(result);
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
  console.error("arisan_participant_unhandled_error", error);
  return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
}
