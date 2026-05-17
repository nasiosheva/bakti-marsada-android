import { describe, expect, it } from "vitest";
import { Hono } from "hono";
import { createAuthRoute } from "../src/routes/auth";

describe("auth route", () => {
  it("registers a user", async () => {
    const app = new Hono();
    app.route("/auth", createAuthRoute({
      createService: () => ({
        register: async () => ({
          user: {
            id: "user-1",
            email: "user@example.com",
            fullName: "Test User",
            role: "user",
            isActive: true,
            createdAt: "2026-05-17T00:00:00.000Z",
            updatedAt: "2026-05-17T00:00:00.000Z"
          },
          session: {
            sessionId: "session-1",
            sessionToken: "session-token",
            expiresAt: "2026-05-24T00:00:00.000Z"
          }
        }),
        login: async () => {
          throw new Error("unused");
        },
        me: async () => {
          throw new Error("unused");
        },
        logout: async () => {}
      } as never)
    }));

    const response = await app.request("http://localhost/auth/register", {
      method: "POST",
      body: JSON.stringify({
        email: "user@example.com",
        password: "password123",
        fullName: "Test User"
      }),
      headers: {
        "Content-Type": "application/json"
      }
    }, {
      DB: {} as never
    });

    expect(response.status).toBe(201);
    await expect(response.json()).resolves.toMatchObject({
      authToken: "session-token",
      userId: "user-1",
      role: "JEMAAT",
      user: {
        id: "user-1",
        role: "JEMAAT"
      }
    });
  });
});
