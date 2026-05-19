import { describe, expect, it, vi } from "vitest";
import { Hono } from "hono";
import { createArisanParticipantRoute } from "../src/routes/arisan-participants";

describe("arisan participants route", () => {
  it("lists arisan participants by sector", async () => {
    const list = vi.fn().mockResolvedValue([
      {
        memberId: "member-1",
        memberName: "Nasio"
      }
    ]);
    const app = new Hono();
    app.route("/v1/arisan/participants", createArisanParticipantRoute({
      createService: () => ({
        list,
        replace: async () => [],
        fillFromMembers: async () => []
      } as never)
    }));

    const response = await app.request("http://localhost/v1/arisan/participants?sectorId=sector-1", {}, { DB: {} as never });
    expect(response.status).toBe(200);
    expect(list).toHaveBeenCalledWith("sector-1");
  });

  it("replaces arisan participants", async () => {
    const replace = vi.fn().mockResolvedValue([{ memberId: "member-2", memberName: "Sihombing" }]);
    const app = new Hono();
    app.route("/v1/arisan/participants", createArisanParticipantRoute({
      createService: () => ({
        list: async () => [],
        replace,
        fillFromMembers: async () => []
      } as never)
    }));

    const response = await app.request("http://localhost/v1/arisan/participants", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        sectorId: "sector-1",
        memberIds: ["member-2"]
      })
    }, { DB: {} as never });

    expect(response.status).toBe(200);
    expect(replace).toHaveBeenCalledWith(expect.objectContaining({
      sectorId: "sector-1",
      memberIds: ["member-2"]
    }));
  });
});
