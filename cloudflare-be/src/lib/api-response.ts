export interface ApiSuccessResponse<T> {
  ok: true;
  code: string;
  message: string;
  data: T;
}

export interface ApiErrorResponse {
  ok: false;
  code: string;
  message: string;
  data: null;
}

export function successResponse<T>(
  data: T,
  init: {
    status?: number;
    code?: string;
    message?: string;
  } = {}
): Response {
  return Response.json(
    {
      ok: true,
      code: init.code ?? "SUCCESS",
      message: init.message ?? "Success",
      data
    } satisfies ApiSuccessResponse<T>,
    { status: init.status ?? 200 }
  );
}

export function errorResponse(
  status: number,
  code: string,
  message: string
): Response {
  return Response.json(
    {
      ok: false,
      code,
      message,
      data: null
    } satisfies ApiErrorResponse,
    { status }
  );
}
