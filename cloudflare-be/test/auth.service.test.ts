import { describe, expect, it } from "vitest";
import { AuthError } from "../src/lib/auth-errors";
import { AuthService } from "../src/services/auth.service";

describe("AuthService", () => {
  it("registers a user and returns a session", async () => {
    const repo = createFakeRepository();
    const crypto = createFakeCrypto();
    const service = new AuthService(repo, crypto, () => new Date("2026-05-17T00:00:00.000Z"));

    const result = await service.register({
      email: "User@Example.com",
      password: "password123",
      fullName: "Test User"
    });

    expect(result.user.email).toBe("user@example.com");
    expect(result.session.sessionToken).toBe("session-token");
    expect(repo.state.createdUsers).toHaveLength(1);
    expect(repo.state.createdSessions).toHaveLength(1);
  });

  it("rejects login with invalid credentials", async () => {
    const repo = createFakeRepository();
    const crypto = createFakeCrypto();
    const service = new AuthService(repo, crypto);

    await expect(
      service.login({
        email: "missing@example.com",
        password: "password123"
      })
    ).rejects.toMatchObject({
      status: 401,
      code: "INVALID_CREDENTIALS"
    });
  });

  it("logs out by revoking the session", async () => {
    const repo = createFakeRepository({
      user: {
        id: "user-1",
        email: "user@example.com",
        password_hash: "hash",
        full_name: "Test User",
        role: "user",
        is_active: 1,
        created_at: "2026-05-17T00:00:00.000Z",
        updated_at: "2026-05-17T00:00:00.000Z"
      },
      session: {
        id: "session-1",
        user_id: "user-1",
        refresh_token_hash: "token-hash:session-token",
        expires_at: "2026-05-24T00:00:00.000Z",
        created_at: "2026-05-17T00:00:00.000Z",
        revoked_at: null
      }
    });
    const crypto = createFakeCrypto();
    const service = new AuthService(repo, crypto, () => new Date("2026-05-17T00:00:00.000Z"));

    await service.logout("session-token");

    expect(repo.state.revokedSessionIds).toEqual(["session-1"]);
  });
});

function createFakeCrypto() {
  return {
    async hashPassword(password: string) {
      return `hash:${password}`;
    },
    async verifyPassword(password: string, storedHash: string) {
      return storedHash === `hash:${password}`;
    },
    createSessionToken() {
      return "session-token";
    },
    async hashSessionToken(token: string) {
      return `token-hash:${token}`;
    }
  };
}

function createFakeRepository(overrides: Partial<{
  user: any;
  session: any;
}> = {}) {
  const state = {
    user: overrides.user ?? null,
    session: overrides.session ?? null,
    createdUsers: [] as any[],
    createdSessions: [] as any[],
    revokedSessionIds: [] as string[]
  };

  return {
    state,
    ...state,
    async findUserByEmail(email: string) {
      return state.user && state.user.email === email ? state.user : null;
    },
    async findUserById(userId: string) {
      return state.user && state.user.id === userId ? state.user : null;
    },
    async createUser(input: any) {
      state.createdUsers.push(input);
      state.user = {
        id: input.id,
        email: input.email,
        password_hash: input.passwordHash,
        full_name: input.fullName,
        role: input.role,
        is_active: 1,
        created_at: "2026-05-17T00:00:00.000Z",
        updated_at: "2026-05-17T00:00:00.000Z"
      };
    },
    async createSession(input: any) {
      state.createdSessions.push(input);
      state.session = {
        id: input.id,
        user_id: input.userId,
        refresh_token_hash: input.refreshTokenHash,
        expires_at: input.expiresAt,
        created_at: "2026-05-17T00:00:00.000Z",
        revoked_at: null
      };
    },
    async findSessionByTokenHash(tokenHash: string) {
      return state.session && state.session.refresh_token_hash === tokenHash ? state.session : null;
    },
    async revokeSession(sessionId: string) {
      state.revokedSessionIds.push(sessionId);
      if (state.session && state.session.id === sessionId) {
        state.session.revoked_at = "2026-05-17T00:00:00.000Z";
      }
    },
    toSummary(user: any) {
      return {
        id: user.id,
        email: user.email,
        fullName: user.full_name,
        role: user.role,
        isActive: user.is_active === 1,
        createdAt: user.created_at,
        updatedAt: user.updated_at
      };
    }
  } as any;
}
