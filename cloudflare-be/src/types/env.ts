export interface AppBindings {
  DB: D1Database;
  GOOGLE_OAUTH_CLIENT_IDS?: string;
  GOOGLE_WEB_CLIENT_ID?: string;
}

export interface AppVariables {
  userId?: string;
  sessionId?: string;
}
