import { describe, expect, it, vi } from "vitest";
import { Hono } from "hono";
import { createPaymentObligationRoute } from "../src/routes/payment-obligations";

describe("payment obligations route", () => {
  it("lists payment obligations by sector", async () => {
    const list = vi.fn().mockResolvedValue([
      {
        id: "payment-1",
        memberId: "member-1",
        memberName: "Nasio",
        title: "Iuran Sektor",
        description: "Mei 2026",
        amount: 150000,
        dueDate: "2026-05-31",
        status: "UNPAID",
        sectorId: "sector-1",
        sectorName: "Sektor 1 HKBP"
      }
    ]);

    const app = new Hono();
    app.route("/v1/payment-obligations", createPaymentObligationRoute({
      createService: () => ({
        list,
        save: async () => {
          throw new Error("unused");
        },
        delete: async () => {}
      } as never)
    }));

    const response = await app.request("http://localhost/v1/payment-obligations?sectorId=sector-1", {}, {
      DB: {} as never
    });

    expect(response.status).toBe(200);
    await expect(response.json()).resolves.toMatchObject({
      ok: true,
      data: [
        expect.objectContaining({
          id: "payment-1",
          sectorId: "sector-1"
        })
      ]
    });
    expect(list).toHaveBeenCalledWith("sector-1");
  });

  it("saves a payment obligation", async () => {
    const save = vi.fn().mockResolvedValue({
      id: "payment-1",
      memberId: "member-1",
      memberName: "Nasio",
      title: "Iuran Sektor",
      description: "Mei 2026",
      amount: 150000,
      dueDate: "2026-05-31",
      status: "UNPAID",
      sectorId: "sector-1",
      sectorName: "Sektor 1 HKBP"
    });

    const app = new Hono();
    app.route("/v1/payment-obligations", createPaymentObligationRoute({
      createService: () => ({
        list: async () => [],
        save,
        delete: async () => {}
      } as never)
    }));

    const response = await app.request("http://localhost/v1/payment-obligations", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        memberId: "member-1",
        memberName: "Nasio",
        title: "Iuran Sektor",
        description: "Mei 2026",
        amount: 150000,
        dueDate: "2026-05-31",
        status: "UNPAID",
        sectorId: "sector-1",
        sectorName: "Sektor 1 HKBP"
      })
    }, {
      DB: {} as never
    });

    expect(response.status).toBe(200);
    expect(save).toHaveBeenCalledWith(expect.objectContaining({
      memberId: "member-1",
      sectorId: "sector-1"
    }));
  });
});
