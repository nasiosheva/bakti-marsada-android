import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import { createAuthService, type AuthService } from "../services/auth.service";
import type { AppBindings } from "../types/env";

type UserRouteDependencies = {
  createService?: (db: D1Database) => AuthService;
};

export function createUserRoute(deps: UserRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createAuthService;

  route.post("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const token = getBearerToken(c.req.header("Authorization"));
      const body = await c.req.json<{
        username?: string;
        email?: string;
        role?: string;
        password?: string;
        fullName?: string;
      }>();

      const user = await service.createUserByAdmin(token, {
        username: body.username ?? "",
        email: body.email ?? "",
        role: body.role ?? "",
        password: body.password ?? "",
        fullName: body.fullName ?? ""
      });

      return successResponse(
        {
          id: user.id,
          username: user.username,
          email: user.email,
          fullName: user.fullName,
          role: user.role
        },
        { status: 201, code: "CREATED", message: "User created" }
      );
    } catch (error) {
      if (error instanceof AuthError) {
        return errorResponse(error.status, error.code, error.message);
      }
      console.error("users_create_unhandled_error", error);
      return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
    }
  });

  route.get("/", async (c) => {
    try {
      const service = createService(c.env.DB);
      const token = getBearerToken(c.req.header("Authorization"));
      const role = (c.req.query("role") ?? "JEMAAT").trim().toUpperCase();
      const users = await service.listUsersByAdmin(token, role);
      return successResponse(users);
    } catch (error) {
      if (error instanceof AuthError) {
        return errorResponse(error.status, error.code, error.message);
      }
      console.error("users_list_unhandled_error", error);
      return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
    }
  });

  return route;
}

function getBearerToken(header: string | undefined): string {
  if (!header) {
    throw new AuthError(401, "UNAUTHORIZED", "Unauthorized");
  }

  const [scheme, token] = header.split(" ");
  if (scheme !== "Bearer" || !token) {
    throw new AuthError(401, "UNAUTHORIZED", "Unauthorized");
  }

  return token;
}
