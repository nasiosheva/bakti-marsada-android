import { DEFAULT_TENANT_ID } from "../config/app-defaults";
import { AuthError } from "../lib/auth-errors";
import {
  WorshipTemplateRepository,
  type SaveWorshipTemplateInput,
  type SaveWorshipTemplateItemInput,
  type WorshipTemplateSummary
} from "../repositories/worship-template.repository";

const webCrypto = (globalThis as unknown as { crypto: { randomUUID(): string } }).crypto;

export interface SaveWorshipTemplateItemRequest {
  id?: string;
  templateId?: string;
  orderIndex?: number;
  title?: string;
  content?: string;
  leader?: string;
  type?: string;
  scriptureReference?: string;
  scriptureText?: string;
  note?: string;
}

export interface SaveWorshipTemplateRequest {
  id?: string;
  tenantId?: string;
  sectorId?: string | null;
  title?: string;
  description?: string;
  items?: SaveWorshipTemplateItemRequest[];
}

export class WorshipTemplateService {
  constructor(private readonly repository: WorshipTemplateRepository) {}

  async list(sectorId: string): Promise<WorshipTemplateSummary[]> {
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    return this.repository.listBySector(sectorId.trim());
  }

  async save(input: SaveWorshipTemplateRequest): Promise<WorshipTemplateSummary> {
    const templateId = input.id?.trim() || webCrypto.randomUUID();
    const payload: SaveWorshipTemplateInput = {
      id: templateId,
      tenantId: input.tenantId?.trim() || DEFAULT_TENANT_ID,
      sectorId: input.sectorId?.trim() || null,
      title: input.title?.trim() ?? "",
      description: input.description?.trim() ?? "",
      items: (input.items ?? []).map((item, index) => this.toItemInput(item, templateId, index))
    };

    this.assertRequired(payload.title, "title", "INVALID_TITLE");
    return this.repository.save(payload);
  }

  async delete(templateId: string): Promise<void> {
    this.assertRequired(templateId, "id", "INVALID_TEMPLATE_ID");
    await this.repository.delete(templateId.trim());
  }

  private toItemInput(
    item: SaveWorshipTemplateItemRequest,
    templateId: string,
    index: number
  ): SaveWorshipTemplateItemInput {
    return {
      id: item.id?.trim() || webCrypto.randomUUID(),
      templateId,
      orderIndex: Number.isFinite(item.orderIndex) ? Number(item.orderIndex) : index,
      title: item.title?.trim() ?? "",
      content: item.content?.trim() ?? "",
      leader: item.leader?.trim() ?? "",
      type: item.type?.trim().toUpperCase() ?? "CUSTOM",
      scriptureReference: item.scriptureReference?.trim() ?? "",
      scriptureText: item.scriptureText?.trim() ?? "",
      note: item.note?.trim() ?? ""
    };
  }

  private assertRequired(value: string, fieldName: string, code: string) {
    if (!value.trim()) {
      throw new AuthError(400, code, `${fieldName} is required`);
    }
  }
}

export function createWorshipTemplateService(db: D1Database): WorshipTemplateService {
  return new WorshipTemplateService(new WorshipTemplateRepository(db));
}
