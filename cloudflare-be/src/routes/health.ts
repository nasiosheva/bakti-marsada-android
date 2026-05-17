import { Hono } from "hono";
import type { AppBindings } from "../types/env";
import { HealthService } from "../services/health.service";
import { HealthRepository } from "../repositories/health.repository";

export const healthRoute = new Hono<{ Bindings: AppBindings }>();

healthRoute.get("/", async (c) => {
  const repository = new HealthRepository(c.env.DB);
  const service = new HealthService(repository);
  const result = await service.check();

  return c.json(result);
});
