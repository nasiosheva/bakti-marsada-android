import { DEFAULT_TENANT_ID } from "../config/app-defaults";

export interface ArisanParticipantRecord {
  member_id: string;
  member_name: string;
}

export interface ArisanParticipantSummary {
  memberId: string;
  memberName: string;
}

export class ArisanParticipantRepository {
  constructor(private readonly db: D1Database) {}

  async listBySector(sectorId: string): Promise<ArisanParticipantRecord[]> {
    const result = await this.db
      .prepare(
        `SELECT ap.member_id,
                COALESCE(m.full_name, '') AS member_name
         FROM arisan_participants ap
         LEFT JOIN members m
            ON m.id = ap.member_id
           AND m.tenant_id = ap.tenant_id
         WHERE ap.tenant_id = ?1
           AND ap.sector_id = ?2
         ORDER BY m.full_name ASC, ap.member_id ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<ArisanParticipantRecord>();

    return result.results ?? [];
  }

  async findValidMemberIds(sectorId: string, memberIds: string[]): Promise<string[]> {
    if (memberIds.length === 0) return [];
    const placeholders = memberIds.map((_, index) => `?${index + 3}`).join(", ");
    const query = `SELECT id
                   FROM members
                   WHERE tenant_id = ?1
                     AND sector_id = ?2
                     AND id IN (${placeholders})`;
    const bindings: (string | number)[] = [DEFAULT_TENANT_ID, sectorId, ...memberIds];
    const result = await this.db.prepare(query).bind(...bindings).all<{ id: string }>();
    return (result.results ?? []).map((item) => item.id);
  }

  async listMemberIdsBySector(sectorId: string): Promise<string[]> {
    const result = await this.db
      .prepare(
        `SELECT id
         FROM members
         WHERE tenant_id = ?1
           AND sector_id = ?2
         ORDER BY full_name ASC, id ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<{ id: string }>();
    return (result.results ?? []).map((item) => item.id);
  }

  async replaceBySector(sectorId: string, memberIds: string[]): Promise<void> {
    await this.db
      .prepare(
        `DELETE FROM arisan_participants
         WHERE tenant_id = ?1
           AND sector_id = ?2`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .run();

    for (const memberId of memberIds) {
      await this.db
        .prepare(
          `INSERT INTO arisan_participants (tenant_id, sector_id, member_id)
           VALUES (?1, ?2, ?3)
           ON CONFLICT(tenant_id, sector_id, member_id) DO UPDATE SET
             updated_at = CURRENT_TIMESTAMP`
        )
        .bind(DEFAULT_TENANT_ID, sectorId, memberId)
        .run();
    }
  }

  toSummary(record: ArisanParticipantRecord): ArisanParticipantSummary {
    return {
      memberId: record.member_id,
      memberName: record.member_name
    };
  }
}
