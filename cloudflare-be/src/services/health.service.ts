import { HealthRepository } from "../repositories/health.repository";

export class HealthService {
  constructor(private readonly repository: HealthRepository) {}

  async check(): Promise<{
    ok: boolean;
    database: string;
  }> {
    const ping = await this.repository.ping();

    return {
      ok: ping === 1,
      database: ping === 1 ? "connected" : "unavailable"
    };
  }
}
