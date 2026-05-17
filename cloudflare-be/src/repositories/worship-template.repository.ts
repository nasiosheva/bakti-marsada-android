import { DEFAULT_TENANT_ID } from "../config/app-defaults";

export interface WorshipTemplateRecord {
  id: string;
  tenant_id: string;
  sector_id: string | null;
  title: string;
  description: string;
}

export interface WorshipTemplateItemRecord {
  id: string;
  template_id: string;
  order_index: number;
  title: string;
  content: string;
  leader: string;
  type: string;
  scripture_reference: string;
  scripture_text: string;
  note: string;
}

export interface WorshipTemplateItemSummary {
  id: string;
  templateId: string;
  orderIndex: number;
  title: string;
  content: string;
  leader: string;
  type: string;
  scriptureReference: string;
  scriptureText: string;
  note: string;
}

export interface WorshipTemplateSummary {
  id: string;
  tenantId: string;
  sectorId: string | null;
  title: string;
  description: string;
  items: WorshipTemplateItemSummary[];
}

export interface SaveWorshipTemplateItemInput {
  id: string;
  templateId: string;
  orderIndex: number;
  title: string;
  content: string;
  leader: string;
  type: string;
  scriptureReference: string;
  scriptureText: string;
  note: string;
}

export interface SaveWorshipTemplateInput {
  id: string;
  tenantId: string;
  sectorId: string | null;
  title: string;
  description: string;
  items: SaveWorshipTemplateItemInput[];
}

export class WorshipTemplateRepository {
  constructor(private readonly db: D1Database) {}

  async listBySector(sectorId: string): Promise<WorshipTemplateSummary[]> {
    const result = await this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, title, description
         FROM worship_templates
         WHERE tenant_id = ?1 AND (sector_id IS NULL OR sector_id = ?2)
         ORDER BY title ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<WorshipTemplateRecord>();
    const templates = result.results ?? [];
    const summaries: WorshipTemplateSummary[] = [];
    for (const template of templates) {
      const items = await this.listItems(template.id);
      summaries.push(this.toSummary(template, items));
    }
    return summaries;
  }

  async save(input: SaveWorshipTemplateInput): Promise<WorshipTemplateSummary> {
    await this.db
      .prepare(
        `INSERT INTO worship_templates (id, tenant_id, sector_id, title, description)
         VALUES (?1, ?2, ?3, ?4, ?5)
         ON CONFLICT(id) DO UPDATE SET
           sector_id = excluded.sector_id,
           title = excluded.title,
           description = excluded.description,
           updated_at = CURRENT_TIMESTAMP`
      )
      .bind(input.id, input.tenantId, input.sectorId, input.title, input.description)
      .run();

    await this.db.prepare("DELETE FROM worship_template_items WHERE template_id = ?1").bind(input.id).run();

    for (const item of input.items) {
      await this.db
        .prepare(
          `INSERT INTO worship_template_items (
            id, template_id, order_index, title, content, leader, type, scripture_reference, scripture_text, note
          ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)`
        )
        .bind(
          item.id,
          input.id,
          item.orderIndex,
          item.title,
          item.content,
          item.leader,
          item.type,
          item.scriptureReference,
          item.scriptureText,
          item.note
        )
        .run();
    }

    const record = await this.findById(input.id);
    if (!record) {
      throw new Error("Saved worship template was not found");
    }
    const items = await this.listItems(input.id);
    return this.toSummary(record, items);
  }

  async delete(templateId: string): Promise<void> {
    await this.db.prepare("DELETE FROM worship_template_items WHERE template_id = ?1").bind(templateId).run();
    await this.db.prepare("DELETE FROM worship_templates WHERE id = ?1").bind(templateId).run();
  }

  async findById(templateId: string): Promise<WorshipTemplateRecord | null> {
    return this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, title, description
         FROM worship_templates
         WHERE id = ?1
         LIMIT 1`
      )
      .bind(templateId)
      .first<WorshipTemplateRecord>();
  }

  private async listItems(templateId: string): Promise<WorshipTemplateItemRecord[]> {
    const result = await this.db
      .prepare(
        `SELECT id, template_id, order_index, title, content, leader, type, scripture_reference, scripture_text, note
         FROM worship_template_items
         WHERE template_id = ?1
         ORDER BY order_index ASC, title ASC`
      )
      .bind(templateId)
      .all<WorshipTemplateItemRecord>();
    return result.results ?? [];
  }

  private toItemSummary(item: WorshipTemplateItemRecord): WorshipTemplateItemSummary {
    return {
      id: item.id,
      templateId: item.template_id,
      orderIndex: item.order_index,
      title: item.title,
      content: item.content,
      leader: item.leader,
      type: item.type,
      scriptureReference: item.scripture_reference,
      scriptureText: item.scripture_text,
      note: item.note
    };
  }

  private toSummary(record: WorshipTemplateRecord, items: WorshipTemplateItemRecord[]): WorshipTemplateSummary {
    return {
      id: record.id,
      tenantId: record.tenant_id,
      sectorId: record.sector_id,
      title: record.title,
      description: record.description,
      items: items.map((item) => this.toItemSummary(item))
    };
  }
}
