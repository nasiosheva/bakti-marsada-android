const encoder = new TextEncoder();

interface WebCryptoLike {
  getRandomValues<T extends ArrayBufferView>(array: T): T;
  randomUUID(): string;
  subtle: {
    digest(algorithm: string, data: BufferSource): Promise<ArrayBuffer>;
    importKey(
      format: "raw",
      keyData: BufferSource,
      algorithm: { name: string },
      extractable: boolean,
      keyUsages: string[]
    ): Promise<unknown>;
    deriveBits(
      algorithm: {
        name: string;
        salt: BufferSource;
        iterations: number;
        hash: string;
      },
      baseKey: unknown,
      length: number
    ): Promise<ArrayBuffer>;
  };
}

const webCrypto = (globalThis as unknown as { crypto: WebCryptoLike }).crypto;

export interface AuthCrypto {
  hashPassword(password: string): Promise<string>;
  verifyPassword(password: string, storedHash: string): Promise<boolean>;
  createSessionToken(): string;
  hashSessionToken(token: string): Promise<string>;
}

export class WebAuthCrypto implements AuthCrypto {
  private readonly iterations = 100000;
  private readonly maxSupportedIterations = 100000;

  async hashPassword(password: string): Promise<string> {
    const salt = webCrypto.getRandomValues(new Uint8Array(16));
    const derivedKey = await this.deriveBits(password, salt, this.iterations);
    return `pbkdf2$${this.iterations}$${toBase64Url(salt)}$${toBase64Url(derivedKey)}`;
  }

  async verifyPassword(password: string, storedHash: string): Promise<boolean> {
    const parts = storedHash.split("$");
    if (parts.length !== 4 || parts[0] !== "pbkdf2") {
      return false;
    }

    const iterations = Number(parts[1]);
    if (!Number.isFinite(iterations) || iterations <= 0 || iterations > this.maxSupportedIterations) {
      return false;
    }
    const salt = fromBase64Url(parts[2]);
    const expected = parts[3];
    const actual = await this.deriveBits(password, salt, iterations);

    return toBase64Url(actual) === expected;
  }

  createSessionToken(): string {
    const bytes = webCrypto.getRandomValues(new Uint8Array(32));
    return toBase64Url(bytes);
  }

  async hashSessionToken(token: string): Promise<string> {
    const digest = await webCrypto.subtle.digest("SHA-256", encoder.encode(token));
    return toBase64Url(new Uint8Array(digest));
  }

  private async deriveBits(password: string, salt: Uint8Array, iterations: number): Promise<Uint8Array> {
    const key = await webCrypto.subtle.importKey(
      "raw",
      encoder.encode(password),
      { name: "PBKDF2" },
      false,
      ["deriveBits"]
    );

    const bits = await webCrypto.subtle.deriveBits(
      {
        name: "PBKDF2",
        salt,
        iterations,
        hash: "SHA-256"
      },
      key,
      256
    );

    return new Uint8Array(bits);
  }
}

function toBase64Url(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}

function fromBase64Url(value: string): Uint8Array {
  const base64 = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
  const binary = atob(padded);
  const bytes = new Uint8Array(binary.length);
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index);
  }
  return bytes;
}
