export interface UserRecord {
  id: string;
  username: string | null;
  email: string;
  password_hash: string;
  full_name: string;
  role: string;
  is_active: number;
  created_at: string;
  updated_at: string;
}

export interface SessionRecord {
  id: string;
  user_id: string;
  refresh_token_hash: string;
  expires_at: string;
  created_at: string;
  revoked_at: string | null;
}

export interface AuthUserSummary {
  id: string;
  username: string | null;
  email: string;
  fullName: string;
  role: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthSession {
  sessionId: string;
  sessionToken: string;
  expiresAt: string;
}

export class AuthRepository {
  constructor(private readonly db: D1Database) {}

  async findUserByEmail(email: string): Promise<UserRecord | null> {
    return this.db
      .prepare("SELECT * FROM users WHERE email = ?1 LIMIT 1")
      .bind(email)
      .first<UserRecord>();
  }

  async findUserByUsername(username: string): Promise<UserRecord | null> {
    try {
      return await this.db
        .prepare(
          `SELECT * FROM users
           WHERE (
             lower(username) = ?1
             OR lower(substr(email, 1, instr(email, '@') - 1)) = ?1
           )
           LIMIT 1`
        )
        .bind(username.toLowerCase())
        .first<UserRecord>();
    } catch (error) {
      if (!isMissingUsernameColumnError(error)) throw error;
      return this.db
        .prepare(
          `SELECT * FROM users
           WHERE lower(substr(email, 1, instr(email, '@') - 1)) = ?1
           LIMIT 1`
        )
        .bind(username.toLowerCase())
        .first<UserRecord>();
    }
  }

  async findUserById(userId: string): Promise<UserRecord | null> {
    return this.db
      .prepare("SELECT * FROM users WHERE id = ?1 LIMIT 1")
      .bind(userId)
      .first<UserRecord>();
  }

  async listUsersByRole(role: "ADMIN" | "JEMAAT"): Promise<UserRecord[]> {
    const { results } = await this.db
      .prepare(
        `SELECT * FROM users
         WHERE upper(role) = ?1
         ORDER BY datetime(created_at) DESC`
      )
      .bind(role)
      .all<UserRecord>();
    return results ?? [];
  }

  async createUser(input: {
    id: string;
    username: string;
    email: string;
    passwordHash: string;
    fullName: string;
    role: string;
  }): Promise<void> {
    try {
      await this.db
        .prepare(
          `INSERT INTO users (id, username, email, password_hash, full_name, role, is_active)
           VALUES (?1, ?2, ?3, ?4, ?5, ?6, 1)`
        )
        .bind(input.id, input.username, input.email, input.passwordHash, input.fullName, input.role)
        .run();
    } catch (error) {
      if (!isMissingUsernameColumnError(error)) throw error;
      await this.db
        .prepare(
          `INSERT INTO users (id, email, password_hash, full_name, role, is_active)
           VALUES (?1, ?2, ?3, ?4, ?5, 1)`
        )
        .bind(input.id, input.email, input.passwordHash, input.fullName, input.role)
        .run();
    }
  }

  async createSession(input: {
    id: string;
    userId: string;
    refreshTokenHash: string;
    expiresAt: string;
  }): Promise<void> {
    await this.db
      .prepare(
        `INSERT INTO sessions (id, user_id, refresh_token_hash, expires_at)
         VALUES (?1, ?2, ?3, ?4)`
      )
      .bind(input.id, input.userId, input.refreshTokenHash, input.expiresAt)
      .run();
  }

  async findSessionByTokenHash(tokenHash: string): Promise<SessionRecord | null> {
    return this.db
      .prepare("SELECT * FROM sessions WHERE refresh_token_hash = ?1 LIMIT 1")
      .bind(tokenHash)
      .first<SessionRecord>();
  }

  async revokeSession(sessionId: string): Promise<void> {
    await this.db
      .prepare("UPDATE sessions SET revoked_at = CURRENT_TIMESTAMP WHERE id = ?1")
      .bind(sessionId)
      .run();
  }

  toSummary(user: UserRecord): AuthUserSummary {
    return {
      id: user.id,
      username: user.username,
      email: user.email,
      fullName: user.full_name,
      role: user.role,
      isActive: user.is_active === 1,
      createdAt: user.created_at,
      updatedAt: user.updated_at
    };
  }
}

function isMissingUsernameColumnError(error: unknown): boolean {
  if (!(error instanceof Error)) return false;
  const message = error.message.toLowerCase();
  return message.includes("no such column: username") || message.includes("has no column named username");
}
