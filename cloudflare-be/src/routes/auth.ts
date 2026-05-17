import { Hono } from "hono";
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
        email?: string;
        password?: string;
        fullName?: string;
      }>();

      const service = createService(c.env.DB);
      const result = await service.register({
        email: body.email ?? "",
        password: body.password ?? "",
        fullName: body.fullName ?? ""
      });

      return c.json(toAuthResponse(result), 201);
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

      return c.json(toAuthResponse(result));
    } catch (error) {
      return mapAuthError(error);
    }
  });

  route.get("/me", async (c) => {
    try {
      const service = createService(c.env.DB);
      const sessionToken = getBearerToken(c.req.header("Authorization"));
      const user = await service.me(sessionToken);

      return c.json({ user });
    } catch (error) {
      return mapAuthError(error);
    }
  });

  route.post("/logout", async (c) => {
    try {
      const service = createService(c.env.DB);
      const sessionToken = getBearerToken(c.req.header("Authorization"));
      await service.logout(sessionToken);

      return c.json({ ok: true });
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
    return Response.json(
      {
        ok: false,
        code: error.code,
        message: error.message
      },
      { status: error.status }
    );
  }

  console.error("auth_unhandled_error", error);
  return Response.json(
    {
      ok: false,
      code: "INTERNAL_SERVER_ERROR",
      message: "Internal server error"
    },
    { status: 500 }
  );
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
