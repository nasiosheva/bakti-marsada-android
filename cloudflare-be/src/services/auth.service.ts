import { AuthError } from "../lib/auth-errors";
import type { AuthCrypto } from "../lib/auth-crypto";
import { WebAuthCrypto } from "../lib/auth-crypto";
import { AuthRepository, type AuthSession, type AuthUserSummary, type UserRecord } from "../repositories/auth.repository";

const webCrypto = (globalThis as unknown as { crypto: { randomUUID(): string } }).crypto;

export interface RegisterInput {
  username?: string;
  email: string;
  password: string;
  fullName: string;
}

export interface LoginInput {
  identifier?: string;
  email?: string;
  password: string;
}

export interface AuthResult {
  user: AuthUserSummary;
  session: AuthSession;
}

export interface CreateUserByAdminInput {
  username: string;
  email: string;
  password: string;
  fullName: string;
  role: string;
}

export interface UserListItem {
  id: string;
  username: string | null;
  email: string;
  fullName: string;
  role: string;
}

export class AuthService {
  constructor(
    private readonly repository: AuthRepository,
    private readonly crypto: AuthCrypto = new WebAuthCrypto(),
    private readonly now: () => Date = () => new Date()
  ) {}

  async register(input: RegisterInput): Promise<AuthResult> {
    const email = normalizeEmail(input.email);
    const username = normalizeUsername(input.username || email.split("@")[0] || "");
    this.assertUsername(username);
    this.assertEmail(email);
    this.assertPassword(input.password);
    this.assertFullName(input.fullName);

    const existingUser = await this.repository.findUserByEmail(email);
    if (existingUser) {
      throw new AuthError(409, "EMAIL_ALREADY_EXISTS", "Email already registered");
    }
    const existingUsername = await this.repository.findUserByUsername(username);
    if (existingUsername) {
      throw new AuthError(409, "USERNAME_ALREADY_EXISTS", "Username already registered");
    }

    const userId = webCrypto.randomUUID();
    const passwordHash = await this.crypto.hashPassword(input.password);
    await this.repository.createUser({
      id: userId,
      username,
      email,
      passwordHash,
      fullName: input.fullName.trim(),
      role: "JEMAAT"
    });

    return this.createSessionForUser({
      id: userId,
      username,
      email,
      full_name: input.fullName.trim(),
      role: "JEMAAT",
      is_active: 1,
      created_at: this.now().toISOString(),
      updated_at: this.now().toISOString(),
      password_hash: passwordHash
    });
  }

  async login(input: LoginInput): Promise<AuthResult> {
    const identifier = normalizeIdentifier(input.identifier ?? input.email ?? "");
    this.assertIdentifier(identifier);
    this.assertPassword(input.password);

    const user = identifier.includes("@")
      ? await this.repository.findUserByEmail(identifier)
      : await this.repository.findAdminUserByUsername(identifier);
    if (!user || user.is_active !== 1) {
      throw new AuthError(401, "INVALID_CREDENTIALS", "Invalid credentials");
    }

    const isPasswordValid = await this.crypto.verifyPassword(input.password, user.password_hash);
    if (!isPasswordValid) {
      throw new AuthError(401, "INVALID_CREDENTIALS", "Invalid credentials");
    }

    return this.createSessionForUser(user);
  }

  async me(sessionToken: string): Promise<AuthUserSummary> {
    const user = await this.requireAuthenticatedUser(sessionToken);
    return this.repository.toSummary(user);
  }

  async logout(sessionToken: string): Promise<void> {
    const session = await this.requireActiveSession(sessionToken);
    await this.repository.revokeSession(session.id);
  }

  async createUserByAdmin(sessionToken: string, input: CreateUserByAdminInput): Promise<AuthUserSummary> {
    const actor = await this.requireAuthenticatedUser(sessionToken);
    if (actor.role.trim().toUpperCase() !== "ADMIN") {
      throw new AuthError(403, "FORBIDDEN", "Only admin can create account");
    }

    const username = normalizeUsername(input.username);
    const email = normalizeEmail(input.email);
    const fullName = input.fullName.trim();
    const role = normalizeRole(input.role);

    this.assertUsername(username);
    this.assertEmail(email);
    this.assertPassword(input.password);
    this.assertFullName(fullName);

    const existingEmail = await this.repository.findUserByEmail(email);
    if (existingEmail) {
      throw new AuthError(409, "EMAIL_ALREADY_EXISTS", "Email already registered");
    }
    const existingUsername = await this.repository.findUserByUsername(username);
    if (existingUsername) {
      throw new AuthError(409, "USERNAME_ALREADY_EXISTS", "Username already registered");
    }

    const passwordHash = await this.crypto.hashPassword(input.password);
    const userId = webCrypto.randomUUID();
    await this.repository.createUser({
      id: userId,
      username,
      email,
      passwordHash,
      fullName,
      role
    });

    const created = await this.repository.findUserById(userId);
    if (!created) {
      throw new AuthError(500, "INTERNAL_SERVER_ERROR", "Failed to create user");
    }

    return this.repository.toSummary(created);
  }

  async listUsersByAdmin(sessionToken: string, role: string): Promise<UserListItem[]> {
    const actor = await this.requireAuthenticatedUser(sessionToken);
    if (actor.role.trim().toUpperCase() !== "ADMIN") {
      throw new AuthError(403, "FORBIDDEN", "Only admin can access user list");
    }
    const normalizedRole = normalizeRole(role);
    const users = await this.repository.listUsersByRole(normalizedRole);
    return users.map((user) => ({
      id: user.id,
      username: user.username,
      email: user.email,
      fullName: user.full_name,
      role: user.role
    }));
  }

  private async createSessionForUser(user: UserRecord): Promise<AuthResult> {
    const sessionToken = this.crypto.createSessionToken();
    const sessionId = webCrypto.randomUUID();
    const refreshTokenHash = await this.crypto.hashSessionToken(sessionToken);
    const expiresAt = new Date(this.now().getTime() + 1000 * 60 * 60 * 24 * 7).toISOString();

    await this.repository.createSession({
      id: sessionId,
      userId: user.id,
      refreshTokenHash,
      expiresAt
    });

    return {
      user: this.repository.toSummary(user),
      session: {
        sessionId,
        sessionToken,
        expiresAt
      }
    };
  }

  private async requireAuthenticatedUser(sessionToken: string) {
    const session = await this.requireActiveSession(sessionToken);
    const user = await this.repository.findUserById(session.user_id);

    if (!user || user.is_active !== 1) {
      throw new AuthError(401, "UNAUTHORIZED", "Unauthorized");
    }

    return user;
  }

  private async requireActiveSession(sessionToken: string) {
    const tokenHash = await this.crypto.hashSessionToken(sessionToken);
    const session = await this.repository.findSessionByTokenHash(tokenHash);

    if (!session || session.revoked_at) {
      throw new AuthError(401, "UNAUTHORIZED", "Unauthorized");
    }

    if (new Date(session.expires_at).getTime() <= this.now().getTime()) {
      throw new AuthError(401, "SESSION_EXPIRED", "Session expired");
    }

    return session;
  }

  private assertIdentifier(identifier: string) {
    if (!identifier) {
      throw new AuthError(400, "INVALID_IDENTIFIER", "Identifier is required");
    }
    if (identifier.includes("@")) return
    if (identifier.length < 3) {
      throw new AuthError(400, "INVALID_IDENTIFIER", "Identifier is too short");
    }
  }

  private assertUsername(username: string) {
    if (username.length < 3) {
      throw new AuthError(400, "INVALID_USERNAME", "Username must be at least 3 characters");
    }
  }

  private assertEmail(email: string) {
    if (!email || !email.includes("@")) {
      throw new AuthError(400, "INVALID_EMAIL", "Invalid email");
    }
  }

  private assertPassword(password: string) {
    if (password.length < 8) {
      throw new AuthError(400, "WEAK_PASSWORD", "Password must be at least 8 characters");
    }
  }

  private assertFullName(fullName: string) {
    if (!fullName.trim()) {
      throw new AuthError(400, "INVALID_FULL_NAME", "Full name is required");
    }
  }
}

export function createAuthService(db: D1Database): AuthService {
  return new AuthService(new AuthRepository(db));
}

function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

function normalizeIdentifier(identifier: string): string {
  return identifier.trim().toLowerCase();
}

function normalizeUsername(username: string): string {
  return username.trim().toLowerCase();
}

function normalizeRole(role: string): "ADMIN" | "JEMAAT" {
  return role.trim().toUpperCase() === "ADMIN" ? "ADMIN" : "JEMAAT";
}
