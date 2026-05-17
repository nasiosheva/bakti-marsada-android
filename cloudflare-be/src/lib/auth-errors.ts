export class AuthError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string
  ) {
    super(message);
    Object.setPrototypeOf(this, AuthError.prototype);
  }
}
