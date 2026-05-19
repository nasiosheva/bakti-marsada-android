import { AuthError } from "../lib/auth-errors";
import {
  ArisanParticipantRepository,
  type ArisanParticipantSummary
} from "../repositories/arisan-participant.repository";

export interface ReplaceArisanParticipantsRequest {
  sectorId?: string;
  memberIds?: string[];
}

export interface FillArisanParticipantsRequest {
  sectorId?: string;
}

export class ArisanParticipantService {
  constructor(private readonly repository: ArisanParticipantRepository) {}

  async list(sectorId: string): Promise<ArisanParticipantSummary[]> {
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    const items = await this.repository.listBySector(sectorId.trim());
    return items.map((item) => this.repository.toSummary(item));
  }

  async replace(input: ReplaceArisanParticipantsRequest): Promise<ArisanParticipantSummary[]> {
    const sectorId = input.sectorId?.trim() ?? "";
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");

    const memberIds = Array.from(
      new Set(
        (input.memberIds ?? [])
          .map((item) => item.trim())
          .filter((item) => item.length > 0)
      )
    );

    const validIds = await this.repository.findValidMemberIds(sectorId, memberIds);
    if (validIds.length != memberIds.length) {
      throw new AuthError(
        400,
        "INVALID_MEMBER_IDS",
        "One or more memberIds are invalid for the selected sector"
      );
    }

    await this.repository.replaceBySector(sectorId, validIds);
    return this.list(sectorId);
  }

  async fillFromMembers(input: FillArisanParticipantsRequest): Promise<ArisanParticipantSummary[]> {
    const sectorId = input.sectorId?.trim() ?? "";
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    const memberIds = await this.repository.listMemberIdsBySector(sectorId);
    await this.repository.replaceBySector(sectorId, memberIds);
    return this.list(sectorId);
  }

  private assertRequired(value: string, fieldName: string, code: string) {
    if (!value.trim()) {
      throw new AuthError(400, code, `${fieldName} is required`);
    }
  }
}

export function createArisanParticipantService(db: D1Database): ArisanParticipantService {
  return new ArisanParticipantService(new ArisanParticipantRepository(db));
}
