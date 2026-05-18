export interface AppBindings {
  DB: D1Database;
  GOOGLE_OAUTH_CLIENT_IDS?: string;
}

export interface AppVariables {
  userId?: string;
  sessionId?: string;
}
