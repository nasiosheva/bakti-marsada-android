import { DEFAULT_TENANT_ID } from "../config/app-defaults";

export interface EventRecord {
  id: string;
  tenant_id: string;
  sector_id: string;
  sector_name: string;
  title: string;
  description: string;
  scheduled_at: string;
  location: string;
}

export interface EventProgramItemRecord {
  id: string;
  event_id: string;
  order_index: number;
  title: string;
  content: string;
  leader: string;
  type: string;
  scripture_reference: string;
  scripture_text: string;
  note: string;
}

export interface EventProgramItemSummary {
  id: string;
  eventId: string;
  orderIndex: number;
  title: string;
  content: string;
  leader: string;
  type: string;
  scriptureReference: string;
  scriptureText: string;
  note: string;
}

export interface EventSummary {
  id: string;
  title: string;
  description: string;
  scheduledAt: string;
  location: string;
  sectorId: string;
  sectorName: string;
  programItems: EventProgramItemSummary[];
}

export interface SaveEventProgramItemInput {
  id: string;
  eventId: string;
  orderIndex: number;
  title: string;
  content: string;
  leader: string;
  type: string;
  scriptureReference: string;
  scriptureText: string;
  note: string;
}

export interface SaveEventInput {
  id: string;
  title: string;
  description: string;
  scheduledAt: string;
  location: string;
  sectorId: string;
  sectorName: string;
  programItems: SaveEventProgramItemInput[];
}

export class EventRepository {
  constructor(private readonly db: D1Database) {}

  async listBySector(sectorId: string): Promise<EventSummary[]> {
    const eventResult = await this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, title, description, scheduled_at, location
         FROM events
         WHERE tenant_id = ?1 AND sector_id = ?2
         ORDER BY scheduled_at DESC, title ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<EventRecord>();

    const events = eventResult.results ?? [];
    const summaries: EventSummary[] = [];
    for (const event of events) {
      const items = await this.listProgramItems(event.id);
      summaries.push(this.toSummary(event, items));
    }
    return summaries;
  }

  async save(input: SaveEventInput): Promise<EventSummary> {
    await this.db
      .prepare(
        `INSERT INTO events (
          id, tenant_id, sector_id, sector_name, title, description, scheduled_at, location
        ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)
        ON CONFLICT(id) DO UPDATE SET
          sector_id = excluded.sector_id,
          sector_name = excluded.sector_name,
          title = excluded.title,
          description = excluded.description,
          scheduled_at = excluded.scheduled_at,
          location = excluded.location,
          updated_at = CURRENT_TIMESTAMP`
      )
      .bind(
        input.id,
        DEFAULT_TENANT_ID,
        input.sectorId,
        input.sectorName,
        input.title,
        input.description,
        input.scheduledAt,
        input.location
      )
      .run();

    await this.db.prepare("DELETE FROM event_program_items WHERE event_id = ?1").bind(input.id).run();

    for (const item of input.programItems) {
      await this.db
        .prepare(
          `INSERT INTO event_program_items (
            id, event_id, order_index, title, content, leader, type, scripture_reference, scripture_text, note
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
      throw new Error("Saved event was not found");
    }
    const items = await this.listProgramItems(input.id);
    return this.toSummary(record, items);
  }

  async delete(eventId: string): Promise<void> {
    await this.db.prepare("DELETE FROM event_program_items WHERE event_id = ?1").bind(eventId).run();
    await this.db.prepare("DELETE FROM events WHERE id = ?1").bind(eventId).run();
  }

  async findById(eventId: string): Promise<EventRecord | null> {
    return this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, title, description, scheduled_at, location
         FROM events
         WHERE id = ?1
         LIMIT 1`
      )
      .bind(eventId)
      .first<EventRecord>();
  }

  private async listProgramItems(eventId: string): Promise<EventProgramItemRecord[]> {
    const result = await this.db
      .prepare(
        `SELECT id, event_id, order_index, title, content, leader, type, scripture_reference, scripture_text, note
         FROM event_program_items
         WHERE event_id = ?1
         ORDER BY order_index ASC, title ASC`
      )
      .bind(eventId)
      .all<EventProgramItemRecord>();
    return result.results ?? [];
  }

  private toProgramItemSummary(item: EventProgramItemRecord): EventProgramItemSummary {
    return {
      id: item.id,
      eventId: item.event_id,
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

  private toSummary(record: EventRecord, items: EventProgramItemRecord[]): EventSummary {
    return {
      id: record.id,
      title: record.title,
      description: record.description,
      scheduledAt: record.scheduled_at,
      location: record.location,
      sectorId: record.sector_id,
      sectorName: record.sector_name,
      programItems: items.map((item) => this.toProgramItemSummary(item))
    };
  }
}
