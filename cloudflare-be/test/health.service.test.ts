import { describe, expect, it } from "vitest";
import { HealthService } from "../src/services/health.service";

describe("HealthService", () => {
  it("marks database as connected when ping returns 1", async () => {
    const service = new HealthService({
      ping: async () => 1
    } as never);

    await expect(service.check()).resolves.toEqual({
      ok: true,
      database: "connected"
    });
  });

  it("marks database as unavailable when ping fails", async () => {
    const service = new HealthService({
      ping: async () => 0
    } as never);

    await expect(service.check()).resolves.toEqual({
      ok: false,
      database: "unavailable"
    });
  });
});
