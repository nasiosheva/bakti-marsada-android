import { Hono } from "hono";
import { errorResponse, successResponse } from "../lib/api-response";
import { AuthError } from "../lib/auth-errors";
import type { AppBindings } from "../types/env";
import { createAuthService, type AuthResult, type AuthService } from "../services/auth.service";

const DEFAULT_TENANT = {
  tenantId: "hkbp",
  tenantName: "HKBP",
  subTenantId: "hkbp-kedaton",
  subTenantName: "HKBP Kedaton",
  sectorId: "sector-1",
  sectorName: "Sektor 1 HKBP"
};

type AuthRouteDependencies = {
  createService?: (db: D1Database) => AuthService;
};

export function createAuthRoute(deps: AuthRouteDependencies = {}) {
  const route = new Hono<{ Bindings: AppBindings }>();
  const createService = deps.createService ?? createAuthService;

  route.post("/register", async (c) => {
    try {
      const body = await c.req.json<{
        username?: string;
        email?: string;
        password?: string;
        fullName?: string;
      }>();

      const service = createService(c.env.DB);
      const result = await service.register({
        username: body.username ?? body.email?.split("@")[0] ?? "",
        email: body.email ?? "",
        password: body.password ?? "",
        fullName: body.fullName ?? ""
      });

      return successResponse(toAuthResponse(result), { status: 201, code: "CREATED", message: "User registered" });
    } catch (error) {
      return mapAuthError(error);
    }
  });

  route.post("/login", async (c) => {
    try {
      const body = await c.req.json<{
        identifier?: string;
        email?: string;
        password?: string;
      }>();

      const service = createService(c.env.DB);
      const result = await service.login({
        identifier: body.identifier ?? body.email ?? "",
        password: body.password ?? ""
      });

      return successResponse(toAuthResponse(result), { message: "Login success" });
    } catch (error) {
      return mapAuthError(error);
    }
  });

  route.get("/me", async (c) => {
    try {
      const service = createService(c.env.DB);
      const sessionToken = getBearerToken(c.req.header("Authorization"));
      const user = await service.me(sessionToken);

      return successResponse({ user });
    } catch (error) {
      return mapAuthError(error);
    }
  });

  route.post("/logout", async (c) => {
    try {
      const service = createService(c.env.DB);
      const sessionToken = getBearerToken(c.req.header("Authorization"));
      await service.logout(sessionToken);

      return successResponse({ success: true }, { message: "Logout success" });
    } catch (error) {
      return mapAuthError(error);
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

function mapAuthError(error: unknown) {
  if (error instanceof AuthError) {
    console.warn("auth_error", {
      status: error.status,
      code: error.code,
      message: error.message
    });
    return errorResponse(error.status, error.code, error.message);
  }

  console.error("auth_unhandled_error", error);
  return errorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error");
}

function toAuthResponse(result: AuthResult) {
  const role = toAppRole(result.user.role);
  return {
    authToken: result.session.sessionToken,
    userId: result.user.id,
    displayName: result.user.fullName,
    role,
    ...DEFAULT_TENANT,
    user: {
      id: result.user.id,
      username: result.user.username,
      fullName: result.user.fullName,
      role
    },
    session: {
      sessionId: result.session.sessionId,
      sessionToken: result.session.sessionToken,
      expiresAt: result.session.expiresAt
    }
  };
}

function toAppRole(role: string): "ADMIN" | "JEMAAT" {
  return role.trim().toUpperCase() === "ADMIN" ? "ADMIN" : "JEMAAT";
}
