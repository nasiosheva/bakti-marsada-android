import { AuthError } from "../lib/auth-errors";
import {
  PaymentObligationRepository,
  type PaymentObligationSummary,
  type SavePaymentObligationInput
} from "../repositories/payment-obligation.repository";

const webCrypto = (globalThis as unknown as { crypto: { randomUUID(): string } }).crypto;

export interface SavePaymentObligationRequest {
  id?: string;
  memberId?: string;
  memberName?: string;
  title?: string;
  description?: string;
  amount?: number;
  dueDate?: string;
  status?: string;
  sectorId?: string;
  sectorName?: string;
}

export class PaymentObligationService {
  constructor(private readonly repository: PaymentObligationRepository) {}

  async list(sectorId: string): Promise<PaymentObligationSummary[]> {
    this.assertRequired(sectorId, "sectorId", "INVALID_SECTOR_ID");
    const items = await this.repository.listBySector(sectorId.trim());
    return items.map((item) => this.repository.toSummary(item));
  }

  async save(input: SavePaymentObligationRequest): Promise<PaymentObligationSummary> {
    const payload: SavePaymentObligationInput = {
      id: input.id?.trim() || webCrypto.randomUUID(),
      memberId: input.memberId?.trim() ?? "",
      memberName: input.memberName?.trim() ?? "",
      title: input.title?.trim() ?? "",
      description: input.description?.trim() ?? "",
      amount: Number(input.amount ?? 0),
      dueDate: input.dueDate?.trim() ?? "",
      status: input.status?.trim().toUpperCase() ?? "",
      sectorId: input.sectorId?.trim() ?? "",
      sectorName: input.sectorName?.trim() ?? ""
    };

    this.assertRequired(payload.memberId, "memberId", "INVALID_MEMBER_ID");
    this.assertRequired(payload.memberName, "memberName", "INVALID_MEMBER_NAME");
    this.assertRequired(payload.title, "title", "INVALID_TITLE");
    this.assertRequired(payload.dueDate, "dueDate", "INVALID_DUE_DATE");
    this.assertRequired(payload.status, "status", "INVALID_STATUS");
    this.assertRequired(payload.sectorId, "sectorId", "INVALID_SECTOR_ID");
    this.assertRequired(payload.sectorName, "sectorName", "INVALID_SECTOR_NAME");
    if (!Number.isFinite(payload.amount) || payload.amount < 0) {
      throw new AuthError(400, "INVALID_AMOUNT", "Amount must be zero or greater");
    }

    const saved = await this.repository.save(payload);
    return this.repository.toSummary(saved);
  }

  async delete(obligationId: string): Promise<void> {
    this.assertRequired(obligationId, "id", "INVALID_OBLIGATION_ID");
    await this.repository.delete(obligationId.trim());
  }

  private assertRequired(value: string, fieldName: string, code: string) {
    if (!value.trim()) {
      throw new AuthError(400, code, `${fieldName} is required`);
    }
  }
}

export function createPaymentObligationService(db: D1Database): PaymentObligationService {
  return new PaymentObligationService(new PaymentObligationRepository(db));
}
