import { AuthError } from "../lib/auth-errors";
import {
  FinanceReportRepository,
  type FinanceReportSummary,
  type SaveFinanceReportInput
} from "../repositories/finance-report.repository";

const webCrypto = (globalThis as unknown as { crypto: { randomUUID(): string } }).crypto;

export interface SaveFinanceReportRequest {
  id?: string;
  title?: string;
  description?: string;
  periodLabel?: string;
  amount?: number;
  isVisibleToJemaat?: boolean;
  sectorId?: string;
  sectorName?: string;
}

export class FinanceReportService {
  constructor(private readonly repository: FinanceReportRepository) {}

  async list(sectorId: string): Promise<FinanceReportSummary[]> {
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    const items = await this.repository.listBySector(sectorId.trim());
    return items.map((item) => this.repository.toSummary(item));
  }

  async save(input: SaveFinanceReportRequest): Promise<FinanceReportSummary> {
    const payload: SaveFinanceReportInput = {
      id: input.id?.trim() || webCrypto.randomUUID(),
      title: input.title?.trim() ?? "",
      description: input.description?.trim() ?? "",
      periodLabel: input.periodLabel?.trim() ?? "",
      amount: Number(input.amount ?? 0),
      isVisibleToJemaat: Boolean(input.isVisibleToJemaat),
      sectorId: input.sectorId?.trim() ?? "",
      sectorName: input.sectorName?.trim() ?? ""
    };

    this.assertRequired(payload.title, "title", "INVALID_TITLE");
    this.assertRequired(payload.periodLabel, "periodLabel", "INVALID_PERIOD_LABEL");
    this.assertRequired(payload.sectorId, "sectorId", "INVALID_SECTOR_ID");
    this.assertRequired(payload.sectorName, "sectorName", "INVALID_SECTOR_NAME");
    if (!Number.isFinite(payload.amount) || payload.amount < 0) {
      throw new AuthError(400, "INVALID_AMOUNT", "Amount must be zero or greater");
    }

    const saved = await this.repository.save(payload);
    return this.repository.toSummary(saved);
  }

  async delete(reportId: string): Promise<void> {
    this.assertRequired(reportId, "id", "INVALID_REPORT_ID");
    await this.repository.delete(reportId.trim());
  }

  private assertRequired(value: string, fieldName: string, code: string) {
    if (!value.trim()) {
      throw new AuthError(400, code, `${fieldName} is required`);
    }
  }
}

export function createFinanceReportService(db: D1Database): FinanceReportService {
  return new FinanceReportService(new FinanceReportRepository(db));
}
