import { AuthError } from "../lib/auth-errors";
import {
  EventRepository,
  type EventSummary,
  type SaveEventInput,
  type SaveEventProgramItemInput
} from "../repositories/event.repository";

const webCrypto = (globalThis as unknown as { crypto: { randomUUID(): string } }).crypto;

export interface SaveEventProgramItemRequest {
  id?: string;
  eventId?: string;
  orderIndex?: number;
  title?: string;
  content?: string;
  leader?: string;
  type?: string;
  scriptureReference?: string;
  scriptureText?: string;
  note?: string;
}

export interface SaveEventRequest {
  id?: string;
  title?: string;
  description?: string;
  scheduledAt?: string;
  location?: string;
  sectorId?: string;
  sectorName?: string;
  programItems?: SaveEventProgramItemRequest[];
}

export class EventService {
  constructor(private readonly repository: EventRepository) {}

  async list(sectorId: string): Promise<EventSummary[]> {
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    return this.repository.listBySector(sectorId.trim());
  }

  async save(input: SaveEventRequest): Promise<EventSummary> {
    const eventId = input.id?.trim() || webCrypto.randomUUID();
    const payload: SaveEventInput = {
      id: eventId,
      title: input.title?.trim() ?? "",
      description: input.description?.trim() ?? "",
      scheduledAt: input.scheduledAt?.trim() ?? "",
      location: input.location?.trim() ?? "",
      sectorId: input.sectorId?.trim() ?? "",
      sectorName: input.sectorName?.trim() ?? "",
      programItems: (input.programItems ?? []).map((item, index) => this.toProgramItemInput(item, eventId, index))
    };

    this.assertRequired(payload.title, "title", "INVALID_TITLE");
    this.assertRequired(payload.scheduledAt, "scheduledAt", "INVALID_SCHEDULED_AT");
    this.assertRequired(payload.location, "location", "INVALID_LOCATION");
    this.assertRequired(payload.sectorId, "sectorId", "INVALID_SECTOR_ID");
    this.assertRequired(payload.sectorName, "sectorName", "INVALID_SECTOR_NAME");

    return this.repository.save(payload);
  }

  async delete(eventId: string): Promise<void> {
    this.assertRequired(eventId, "id", "INVALID_EVENT_ID");
    await this.repository.delete(eventId.trim());
  }

  private toProgramItemInput(
    item: SaveEventProgramItemRequest,
    eventId: string,
    index: number
  ): SaveEventProgramItemInput {
    return {
      id: item.id?.trim() || webCrypto.randomUUID(),
      eventId,
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

export function createEventService(db: D1Database): EventService {
  return new EventService(new EventRepository(db));
}
