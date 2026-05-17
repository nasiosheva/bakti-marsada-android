import { DEFAULT_TENANT_ID } from "../config/app-defaults";

export interface PaymentObligationRecord {
  id: string;
  tenant_id: string;
  sector_id: string;
  sector_name: string;
  member_id: string;
  member_name: string;
  title: string;
  description: string;
  amount: number;
  due_date: string;
  status: string;
}

export interface PaymentObligationSummary {
  id: string;
  memberId: string;
  memberName: string;
  title: string;
  description: string;
  amount: number;
  dueDate: string;
  status: string;
  sectorId: string;
  sectorName: string;
}

export interface SavePaymentObligationInput {
  id: string;
  memberId: string;
  memberName: string;
  title: string;
  description: string;
  amount: number;
  dueDate: string;
  status: string;
  sectorId: string;
  sectorName: string;
}

export class PaymentObligationRepository {
  constructor(private readonly db: D1Database) {}

  async listBySector(sectorId: string): Promise<PaymentObligationRecord[]> {
    const result = await this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, member_id, member_name, title, description, amount, due_date, status
         FROM payment_obligations
         WHERE tenant_id = ?1 AND sector_id = ?2
         ORDER BY due_date ASC, title ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<PaymentObligationRecord>();

    return result.results ?? [];
  }

  async save(input: SavePaymentObligationInput): Promise<PaymentObligationRecord> {
    await this.db
      .prepare(
        `INSERT INTO payment_obligations (
          id, tenant_id, sector_id, sector_name, member_id, member_name, title, description, amount, due_date, status
        ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11)
        ON CONFLICT(id) DO UPDATE SET
          sector_id = excluded.sector_id,
          sector_name = excluded.sector_name,
          member_id = excluded.member_id,
          member_name = excluded.member_name,
          title = excluded.title,
          description = excluded.description,
          amount = excluded.amount,
          due_date = excluded.due_date,
          status = excluded.status,
          updated_at = CURRENT_TIMESTAMP`
      )
      .bind(
        input.id,
        DEFAULT_TENANT_ID,
        input.sectorId,
        input.sectorName,
        input.memberId,
        input.memberName,
        input.title,
        input.description,
        input.amount,
        input.dueDate,
        input.status
      )
      .run();

    const record = await this.findById(input.id);
    if (!record) {
      throw new Error("Saved payment obligation was not found");
    }

    return record;
  }

  async delete(obligationId: string): Promise<void> {
    await this.db
      .prepare("DELETE FROM payment_obligations WHERE id = ?1")
      .bind(obligationId)
      .run();
  }

  async findById(obligationId: string): Promise<PaymentObligationRecord | null> {
    return this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, member_id, member_name, title, description, amount, due_date, status
         FROM payment_obligations
         WHERE id = ?1
         LIMIT 1`
      )
      .bind(obligationId)
      .first<PaymentObligationRecord>();
  }

  toSummary(record: PaymentObligationRecord): PaymentObligationSummary {
    return {
      id: record.id,
      memberId: record.member_id,
      memberName: record.member_name,
      title: record.title,
      description: record.description,
      amount: record.amount,
      dueDate: record.due_date,
      status: record.status,
      sectorId: record.sector_id,
      sectorName: record.sector_name
    };
  }
}
