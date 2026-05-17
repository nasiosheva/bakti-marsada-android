import { describe, expect, it } from "vitest";
import { app } from "../src/app";

describe("health route", () => {
  it("returns health status from the D1 binding", async () => {
    const fakeDb = {
      prepare: () => ({
        first: async () => ({ ok: 1 })
      })
    } as never;

    const response = await app.request("http://localhost/health", {}, { DB: fakeDb });
    const body = await response.json();

    expect(response.status).toBe(200);
    expect(body).toEqual({
      ok: true,
      database: "connected"
    });
  });
});
