import { AuthError } from "../lib/auth-errors";
import { MemberRepository, type MemberSummary, type SaveMemberInput } from "../repositories/member.repository";

const webCrypto = (globalThis as unknown as { crypto: { randomUUID(): string } }).crypto;

export interface SaveMemberRequest {
  id?: string;
  fullName?: string;
  familyGroup?: string;
  phoneNumber?: string;
  address?: string;
  roleInSector?: string;
  sectorId?: string;
  sectorName?: string;
}

export class MemberService {
  constructor(private readonly repository: MemberRepository) {}

  async list(sectorId: string): Promise<MemberSummary[]> {
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    const items = await this.repository.listBySector(sectorId.trim());
    return items.map((item) => this.repository.toSummary(item));
  }

  async save(input: SaveMemberRequest): Promise<MemberSummary> {
    const payload: SaveMemberInput = {
      id: input.id?.trim() || webCrypto.randomUUID(),
      fullName: input.fullName?.trim() ?? "",
      familyGroup: input.familyGroup?.trim() ?? "",
      phoneNumber: input.phoneNumber?.trim() ?? "",
      address: input.address?.trim() ?? "",
      roleInSector: input.roleInSector?.trim() ?? "",
      sectorId: input.sectorId?.trim() ?? "",
      sectorName: input.sectorName?.trim() ?? ""
    };

    this.assertRequired(payload.fullName, "fullName", "INVALID_FULL_NAME");
    this.assertRequired(payload.sectorId, "sectorId", "INVALID_SECTOR_ID");
    this.assertRequired(payload.sectorName, "sectorName", "INVALID_SECTOR_NAME");

    const saved = await this.repository.save(payload);
    return this.repository.toSummary(saved);
  }

  async delete(memberId: string): Promise<void> {
    this.assertRequired(memberId, "id", "INVALID_MEMBER_ID");
    await this.repository.delete(memberId.trim());
  }

  private assertRequired(value: string, fieldName: string, code: string) {
    if (!value.trim()) {
      throw new AuthError(400, code, `${fieldName} is required`);
    }
  }
}

export function createMemberService(db: D1Database): MemberService {
  return new MemberService(new MemberRepository(db));
}
