import { DEFAULT_TENANT_ID } from "../config/app-defaults";

export interface MemberRecord {
  id: string;
  tenant_id: string;
  sector_id: string;
  sector_name: string;
  full_name: string;
  family_group: string;
  phone_number: string;
  address: string;
  role_in_sector: string;
}

export interface MemberSummary {
  id: string;
  fullName: string;
  familyGroup: string;
  phoneNumber: string;
  address: string;
  roleInSector: string;
  sectorId: string;
  sectorName: string;
}

export interface SaveMemberInput {
  id: string;
  fullName: string;
  familyGroup: string;
  phoneNumber: string;
  address: string;
  roleInSector: string;
  sectorId: string;
  sectorName: string;
}

export class MemberRepository {
  constructor(private readonly db: D1Database) {}

  async listBySector(sectorId: string): Promise<MemberRecord[]> {
    const result = await this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, full_name, family_group, phone_number, address, role_in_sector
         FROM members
         WHERE tenant_id = ?1 AND sector_id = ?2
         ORDER BY full_name ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<MemberRecord>();

    return result.results ?? [];
  }

  async save(input: SaveMemberInput): Promise<MemberRecord> {
    await this.db
      .prepare(
        `INSERT INTO members (
          id, tenant_id, sector_id, sector_name, full_name, family_group, phone_number, address, role_in_sector
        ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)
        ON CONFLICT(id) DO UPDATE SET
          sector_id = excluded.sector_id,
          sector_name = excluded.sector_name,
          full_name = excluded.full_name,
          family_group = excluded.family_group,
          phone_number = excluded.phone_number,
          address = excluded.address,
          role_in_sector = excluded.role_in_sector,
          updated_at = CURRENT_TIMESTAMP`
      )
      .bind(
        input.id,
        DEFAULT_TENANT_ID,
        input.sectorId,
        input.sectorName,
        input.fullName,
        input.familyGroup,
        input.phoneNumber,
        input.address,
        input.roleInSector
      )
      .run();

    const record = await this.findById(input.id);
    if (!record) {
      throw new Error("Saved member was not found");
    }
    return record;
  }

  async delete(memberId: string): Promise<void> {
    await this.db.prepare("DELETE FROM members WHERE id = ?1").bind(memberId).run();
  }

  async findById(memberId: string): Promise<MemberRecord | null> {
    return this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, full_name, family_group, phone_number, address, role_in_sector
         FROM members
         WHERE id = ?1
         LIMIT 1`
      )
      .bind(memberId)
      .first<MemberRecord>();
  }

  toSummary(record: MemberRecord): MemberSummary {
    return {
      id: record.id,
      fullName: record.full_name,
      familyGroup: record.family_group,
      phoneNumber: record.phone_number,
      address: record.address,
      roleInSector: record.role_in_sector,
      sectorId: record.sector_id,
      sectorName: record.sector_name
    };
  }
}
