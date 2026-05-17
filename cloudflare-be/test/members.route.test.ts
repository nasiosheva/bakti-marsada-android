import { describe, expect, it, vi } from "vitest";
import { Hono } from "hono";
import { createMemberRoute } from "../src/routes/members";

describe("members route", () => {
  it("lists members", async () => {
    const list = vi.fn().mockResolvedValue([{ id: "member-1", fullName: "Nasio", sectorId: "sector-1" }]);
    const app = new Hono();
    app.route("/v1/members", createMemberRoute({
      createService: () => ({ list, save: async () => ({}), delete: async () => {} } as never)
    }));

    const response = await app.request("http://localhost/v1/members?sectorId=sector-1", {}, { DB: {} as never });
    expect(response.status).toBe(200);
    expect(list).toHaveBeenCalledWith("sector-1");
  });
});
