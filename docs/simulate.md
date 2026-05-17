# Mode Simulasi Android

Mode simulasi dipakai untuk mencoba flow role-based Bakti Marsada tanpa backend aktif.

## Build Variant

Gunakan variant `simulate`.

```bash
./gradlew assembleSimulate
./gradlew installSimulate
```

Konfigurasi variant:

- `applicationIdSuffix`: `.simulate`
- `versionNameSuffix`: `-simulate`
- `APP_ENVIRONMENT`: `simulate`
- `SIMULATION_ENABLED`: `true`
- database cache: `bakti_marsada_simulate.db`

## Akun Demo

```text
Admin  : admin@baktimarsada.id / admin123
Jemaat : jemaat@baktimarsada.id / jemaat123
```

Pada login screen variant simulate:

- tombol `Masuk sebagai Admin` dan `Masuk sebagai Jemaat` langsung memakai akun demo
- tombol `Seed Ulang + Masuk Admin` mereset data simulasi ke seed awal lalu login sebagai admin

## Scope Data

Data simulate disediakan oleh `SimulateAppRemoteDataSource` sebagai remote datasource in-memory.

Flow yang bisa dicoba:

- login role `ADMIN` dan `JEMAAT`
- bottom navigation untuk masing-masing role
- list acara partangiangan sektor/wijk
- direktori anggota sektor/wijk
- laporan keuangan dengan filter visible untuk jemaat
- kewajiban pembayaran dan status
- CRUD admin untuk acara, anggota, laporan keuangan, dan tagihan
- reset ulang seed data simulasi dari dashboard admin melalui tombol `Reset Data Simulasi`

Catatan: data mutation hanya berlaku selama proses aplikasi berjalan. Restart app akan mengembalikan seed data simulate.
