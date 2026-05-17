export class HealthRepository {
  constructor(private readonly db: D1Database) {}

  async ping(): Promise<number> {
    const row = await this.db.prepare("SELECT 1 AS ok").first<{ ok: number }>();
    return row?.ok ?? 0;
  }
}
