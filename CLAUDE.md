# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Lotus Laverne** — a Java Swing desktop application for hotel management. Language is Vietnamese throughout (UI text, variable/class names, SQL column names, comments).

## Commands

```bash
# Compile
mvn clean compile

# Build runnable JAR
mvn clean package

# Run (after package)
java -jar target/HotelManagement-1.0-SNAPSHOT.jar
```

No test framework or linter is configured.

## Database Setup

1. Install SQL Server 2017+ with SA login enabled.
2. Import schema: run `db/LotusLaverne_utf8.sql` against your SQL Server instance.
3. Update credentials in `src/main/java/com/lotuslaverne/util/ConnectDB.java`:
   - Default: `localhost:1433`, database `QuanLyKhachSan`, user `sa`, password `sapassword`

## Architecture

Classic 3-tier MVC with no framework — all wiring is manual.

```
src/main/java/com/lotuslaverne/
├── application/Main.java     # Entry point — sets FlatLaf theme, launches FrmLogin
├── entity/                   # 14 POJOs mapping to DB tables (e.g. KhachHang, Phong, HoaDon)
├── dao/                      # 11 DAO classes — raw SQL via PreparedStatement, no ORM
├── gui/                      # 10 Swing JFrames (FrmLogin → FrmMain → feature frames)
└── util/
    ├── ConnectDB.java         # Singleton DB connection with auto-reconnect
    └── UIFactory.java         # Shared Swing component styling helpers
```

**Data flow:** GUI frame calls DAO method → DAO executes SQL → maps ResultSet to entity → GUI renders in JTable.

**Navigation:** `FrmMain` uses `CardLayout`; each sidebar button instantiates or switches to a feature `JFrame` (FrmPhong, FrmDatPhong, FrmKhachHang, FrmDichVu, FrmThanhToan, FrmNhanVien, FrmThongKe, FrmTrangChu).

**Auth:** `FrmLogin` calls `TaiKhoanDAO.checkLogin()`, passes the `TaiKhoan` object to `FrmMain` on success.

## Key Conventions

- All DAO classes call `ConnectDB.getInstance().getConnection()` — never open a new connection directly.
- Entity field names mirror Vietnamese DB column names (e.g., `maKhach`, `tenPhong`, `giaPhong`).
- UI components are styled via `UIFactory` static methods — do not set fonts/colors inline.
- No connection pooling; the singleton reconnects if the connection is closed.
