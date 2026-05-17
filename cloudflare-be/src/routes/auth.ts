import { Hono } from "hono";
import { AuthError } from "../lib/auth-errors";
import type { AppBindings } from "../types/env";
import { createAuthService, type AuthService } from "../services/auth.service";

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

      return c.json(result, 201);
    } catch (error) {
      return mapAuthError(error);
    }
  });

  route.post("/login", async (c) => {
    try {
      const body = await c.req.json<{
        email?: string;
        password?: string;
      }>();

      const service = createService(c.env.DB);
      const result = await service.login({
        email: body.email ?? "",
        password: body.password ?? ""
      });

      return c.json(result);
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
    return Response.json(
      {
        ok: false,
        code: error.code,
        message: error.message
      },
      { status: error.status }
    );
  }

  return Response.json(
    {
      ok: false,
      code: "INTERNAL_SERVER_ERROR",
      message: "Internal server error"
    },
    { status: 500 }
  );
}
