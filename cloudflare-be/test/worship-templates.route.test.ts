import { describe, expect, it, vi } from "vitest";
import { Hono } from "hono";
import { createWorshipTemplateRoute } from "../src/routes/worship-templates";

describe("worship templates route", () => {
  it("lists worship templates", async () => {
    const list = vi.fn().mockResolvedValue([{ id: "template-1", title: "Partangiangan", sectorId: "sector-1", items: [] }]);
    const app = new Hono();
    app.route("/v1/worship-templates", createWorshipTemplateRoute({
      createService: () => ({ list, save: async () => ({}), delete: async () => {} } as never)
    }));

    const response = await app.request("http://localhost/v1/worship-templates?sectorId=sector-1", {}, { DB: {} as never });
    expect(response.status).toBe(200);
    expect(list).toHaveBeenCalledWith("sector-1");
  });
});
