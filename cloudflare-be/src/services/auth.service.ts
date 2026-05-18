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

export interface GoogleLoginInput {
  idToken: string;
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

export interface GoogleIdentityPayload {
  sub: string;
  email: string;
  emailVerified: boolean;
  fullName: string;
}

export interface GoogleTokenVerifier {
  verifyIdToken(idToken: string): Promise<GoogleIdentityPayload>;
}

export class GoogleTokenInfoVerifier implements GoogleTokenVerifier {
  constructor(
    private readonly fetchImpl: typeof fetch = fetch,
    private readonly allowedAudiences: string[] = []
  ) {}

  async verifyIdToken(idToken: string): Promise<GoogleIdentityPayload> {
    if (!idToken.trim()) {
      throw new AuthError(400, "INVALID_GOOGLE_TOKEN", "Google token is required");
    }

    const response = await this.fetchImpl(
      `https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`
    );
    if (!response.ok) {
      throw new AuthError(401, "INVALID_GOOGLE_TOKEN", "Invalid Google token");
    }

    const body = await response.json<{
      sub?: string;
      email?: string;
      email_verified?: string | boolean;
      name?: string;
      aud?: string;
    }>();

    const audience = body.aud?.trim();
    if (this.allowedAudiences.length > 0 && (!audience || !this.allowedAudiences.includes(audience))) {
      throw new AuthError(401, "INVALID_GOOGLE_AUDIENCE", "Google audience is not allowed");
    }

    return {
      sub: body.sub?.trim() ?? "",
      email: normalizeEmail(body.email ?? ""),
      emailVerified: String(body.email_verified).trim().toLowerCase() === "true",
      fullName: body.name?.trim() || body.email?.split("@")[0]?.trim() || "Google User"
    };
  }
}

export class AuthService {
  constructor(
    private readonly repository: AuthRepository,
    private readonly crypto: AuthCrypto = new WebAuthCrypto(),
    private readonly googleTokenVerifier: GoogleTokenVerifier = new GoogleTokenInfoVerifier(),
    private readonly now: () => Date = () => new Date()
  ) {}

  async register(input: RegisterInput): Promise<AuthResult> {
    const email = normalizeEmail(input.email);
    const username = normalizeUsername(input.username || email.split("@")[0] || "");
    this.assertRegistrationInput(username, email, input.password, input.fullName);
    await this.assertUniqueUserCredentials(username, email);

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

    const user = await this.findUserByIdentifier(identifier);
    this.assertActiveUser(user);

    const isPasswordValid = await this.crypto.verifyPassword(input.password, user.password_hash);
    if (!isPasswordValid) {
      throw new AuthError(401, "INVALID_CREDENTIALS", "Invalid credentials");
    }

    return this.createSessionForUser(user);
  }

  async loginWithGoogle(input: GoogleLoginInput): Promise<AuthResult> {
    const identity = await this.googleTokenVerifier.verifyIdToken(input.idToken);
    this.assertGoogleIdentity(identity);
    const existingUser = await this.findUserByEmail(identity.email);
    this.assertJemaatRoleForGoogle(existingUser);

    if (existingUser) {
      return this.createSessionForUser(existingUser);
    }

    const createdUser = await this.createGoogleJemaatUser(identity);
    if (!createdUser) {
      throw new AuthError(500, "INTERNAL_SERVER_ERROR", "Failed to create Google user");
    }

    return this.createSessionForUser(createdUser);
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

    this.assertRegistrationInput(username, email, input.password, fullName);
    await this.assertUniqueUserCredentials(username, email);

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

  private async resolveAvailableUsername(email: string): Promise<string> {
    const baseUsername = normalizeUsername(email.split("@")[0] || "");
    this.assertUsername(baseUsername);
    let candidate = baseUsername
    let suffix = 1
    while (await this.repository.findUserByUsername(candidate)) {
      suffix += 1
      candidate = `${baseUsername}${suffix}`
    }
    return candidate
  }

  private async createGoogleJemaatUser(identity: GoogleIdentityPayload): Promise<UserRecord | null> {
    const username = await this.resolveAvailableUsername(identity.email);
    const userId = webCrypto.randomUUID();
    await this.repository.createUser({
      id: userId,
      username,
      email: identity.email,
      passwordHash: `google:${identity.sub}`,
      fullName: identity.fullName,
      role: "JEMAAT"
    });
    return this.repository.findUserById(userId);
  }

  private assertGoogleIdentity(identity: GoogleIdentityPayload) {
    if (!identity.sub || !identity.email || !identity.emailVerified) {
      throw new AuthError(401, "INVALID_GOOGLE_TOKEN", "Google account is not verified");
    }
  }

  private assertJemaatRoleForGoogle(user: UserRecord | null) {
    if (!user) return;
    this.assertActiveUser(user);
    if (normalizeRole(user.role) !== "JEMAAT") {
      throw new AuthError(403, "GOOGLE_LOGIN_JEMAAT_ONLY", "Google login is only available for jemaat");
    }
  }

  private async findUserByIdentifier(identifier: string): Promise<UserRecord | null> {
    return identifier.includes("@")
      ? this.repository.findUserByEmail(identifier)
      : this.repository.findUserByUsername(identifier);
  }

  private async findUserByEmail(email: string): Promise<UserRecord | null> {
    return this.repository.findUserByEmail(email);
  }

  private assertActiveUser(user: UserRecord | null) {
    if (!user || user.is_active !== 1) {
      throw new AuthError(401, "INVALID_CREDENTIALS", "Invalid credentials");
    }
  }

  private assertRegistrationInput(username: string, email: string, password: string, fullName: string) {
    this.assertUsername(username);
    this.assertEmail(email);
    this.assertPassword(password);
    this.assertFullName(fullName);
  }

  private async assertUniqueUserCredentials(username: string, email: string) {
    const existingEmail = await this.repository.findUserByEmail(email);
    if (existingEmail) {
      throw new AuthError(409, "EMAIL_ALREADY_EXISTS", "Email already registered");
    }
    const existingUsername = await this.repository.findUserByUsername(username);
    if (existingUsername) {
      throw new AuthError(409, "USERNAME_ALREADY_EXISTS", "Username already registered");
    }
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

export function createAuthService(
  db: D1Database,
  options: {
    googleAllowedAudiences?: string[];
    fetchImpl?: typeof fetch;
  } = {}
): AuthService {
  return new AuthService(
    new AuthRepository(db),
    new WebAuthCrypto(),
    new GoogleTokenInfoVerifier(
      options.fetchImpl ?? fetch,
      options.googleAllowedAudiences ?? []
    )
  );
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
