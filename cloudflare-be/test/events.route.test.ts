import { describe, expect, it, vi } from "vitest";
import { Hono } from "hono";
import { createEventRoute } from "../src/routes/events";

describe("events route", () => {
  it("lists events", async () => {
    const list = vi.fn().mockResolvedValue([{ id: "event-1", title: "Partangiangan", sectorId: "sector-1", programItems: [] }]);
    const app = new Hono();
    app.route("/v1/events", createEventRoute({
      createService: () => ({ list, save: async () => ({}), delete: async () => {} } as never)
    }));

    const response = await app.request("http://localhost/v1/events?sectorId=sector-1", {}, { DB: {} as never });
    expect(response.status).toBe(200);
    expect(list).toHaveBeenCalledWith("sector-1");
  });
});
