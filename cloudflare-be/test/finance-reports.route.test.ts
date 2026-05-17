import { describe, expect, it, vi } from "vitest";
import { Hono } from "hono";
import { createFinanceReportRoute } from "../src/routes/finance-reports";

describe("finance reports route", () => {
  it("lists finance reports", async () => {
    const list = vi.fn().mockResolvedValue([{ id: "finance-1", title: "Kas", sectorId: "sector-1" }]);
    const app = new Hono();
    app.route("/v1/finance-reports", createFinanceReportRoute({
      createService: () => ({ list, save: async () => ({}), delete: async () => {} } as never)
    }));

    const response = await app.request("http://localhost/v1/finance-reports?sectorId=sector-1", {}, { DB: {} as never });
    expect(response.status).toBe(200);
    expect(list).toHaveBeenCalledWith("sector-1");
  });
});
