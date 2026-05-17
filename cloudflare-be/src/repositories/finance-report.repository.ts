import { DEFAULT_TENANT_ID } from "../config/app-defaults";

export interface FinanceReportRecord {
  id: string;
  tenant_id: string;
  sector_id: string;
  sector_name: string;
  title: string;
  description: string;
  period_label: string;
  amount: number;
  is_visible_to_jemaat: number;
}

export interface FinanceReportSummary {
  id: string;
  title: string;
  description: string;
  periodLabel: string;
  amount: number;
  isVisibleToJemaat: boolean;
  sectorId: string;
  sectorName: string;
}

export interface SaveFinanceReportInput {
  id: string;
  title: string;
  description: string;
  periodLabel: string;
  amount: number;
  isVisibleToJemaat: boolean;
  sectorId: string;
  sectorName: string;
}

export class FinanceReportRepository {
  constructor(private readonly db: D1Database) {}

  async listBySector(sectorId: string): Promise<FinanceReportRecord[]> {
    const result = await this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, title, description, period_label, amount, is_visible_to_jemaat
         FROM finance_reports
         WHERE tenant_id = ?1 AND sector_id = ?2
         ORDER BY period_label DESC, title ASC`
      )
      .bind(DEFAULT_TENANT_ID, sectorId)
      .all<FinanceReportRecord>();
    return result.results ?? [];
  }

  async save(input: SaveFinanceReportInput): Promise<FinanceReportRecord> {
    await this.db
      .prepare(
        `INSERT INTO finance_reports (
          id, tenant_id, sector_id, sector_name, title, description, period_label, amount, is_visible_to_jemaat
        ) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9)
        ON CONFLICT(id) DO UPDATE SET
          sector_id = excluded.sector_id,
          sector_name = excluded.sector_name,
          title = excluded.title,
          description = excluded.description,
          period_label = excluded.period_label,
          amount = excluded.amount,
          is_visible_to_jemaat = excluded.is_visible_to_jemaat,
          updated_at = CURRENT_TIMESTAMP`
      )
      .bind(
        input.id,
        DEFAULT_TENANT_ID,
        input.sectorId,
        input.sectorName,
        input.title,
        input.description,
        input.periodLabel,
        input.amount,
        input.isVisibleToJemaat ? 1 : 0
      )
      .run();

    const record = await this.findById(input.id);
    if (!record) {
      throw new Error("Saved finance report was not found");
    }
    return record;
  }

  async delete(reportId: string): Promise<void> {
    await this.db.prepare("DELETE FROM finance_reports WHERE id = ?1").bind(reportId).run();
  }

  async findById(reportId: string): Promise<FinanceReportRecord | null> {
    return this.db
      .prepare(
        `SELECT id, tenant_id, sector_id, sector_name, title, description, period_label, amount, is_visible_to_jemaat
         FROM finance_reports
         WHERE id = ?1
         LIMIT 1`
      )
      .bind(reportId)
      .first<FinanceReportRecord>();
  }

  toSummary(record: FinanceReportRecord): FinanceReportSummary {
    return {
      id: record.id,
      title: record.title,
      description: record.description,
      periodLabel: record.period_label,
      amount: record.amount,
      isVisibleToJemaat: record.is_visible_to_jemaat === 1,
      sectorId: record.sector_id,
      sectorName: record.sector_name
    };
  }
}
