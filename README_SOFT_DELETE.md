Scheduled cleanup & soft-delete (URL Shortener)

Overview

This document explains the scheduled cleanup feature added to the URL Shortener project. The goal: automatically mark expired URL records as soft-deleted (keep records for auditing/restore) and prevent redirects for expired links.

How it tracks and how soft-delete works

- Expiry tracking:
  - Each UrlMapping has an expiresAt (timestamp). If expiresAt is non-null and now() > expiresAt, the link is considered expired.
  - The existing ExpiryUtil provides helpers to calculate and format expiry times.

- Soft-delete fields:
  - deleted (BOOLEAN NOT NULL DEFAULT FALSE)
  - deleted_at (TIMESTAMP NULL)

  These fields are added to the url_mapping table. Soft-delete does not remove rows — it marks them so the application treats them as non-existent for redirect/duplicate checks, while keeping data for analytics/audit.

Database changes / Migration

- Required ALTER statements (run once):
  ALTER TABLE url_mapping ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
  ALTER TABLE url_mapping ADD COLUMN deleted_at TIMESTAMP;

- The project currently uses spring.jpa.hibernate.ddl-auto=update. On startup, Hibernate attempted to add columns automatically; if that fails (NeonDB or permissions), run the SQL above in the Neon Console or via psql.

Entity & Repository changes

- UrlMapping entity now includes:
  - private Boolean deleted = false;
  - private LocalDateTime deletedAt;
  - public boolean isDeleted() helper

- Repository additions/changes:
  - findByShortCodeAndDeletedFalse(String shortCode)
  - findByLongUrlAndDeletedFalse(String longUrl)
  - findAllExpiredLinks() query updated to ignore already-deleted rows

Service behavior

- shortenUrl dedup logic uses findByLongUrlAndDeletedFalse so previously soft-deleted URLs do not prevent re-creation of a short code.
- getLongUrl uses findByShortCodeAndDeletedFalse — soft-deleted links return 404 (same UX as not found).
- getAnalytics remains available (returns the UrlMapping record) — controllers can show "deleted" or "expired" in response where appropriate.
- New method: softDeleteExpiredLinks()
  - Finds expired (non-deleted) rows via repository.findAllExpiredLinks()
  - Marks each with deleted=true and deletedAt=now()
  - Saves changed records
  - Returns number of marked rows

Scheduling

- Scheduling enabled via @EnableScheduling on the main application class.
- ScheduledCleanupService runs softDeleteExpiredLinks() on a cron schedule.
- Default schedule (configurable in application.properties):
  cleanup.expired.cron=0 0 * * * *   # runs hourly at minute 0

Configuration & properties

- application.properties additions:
  - cleanup.expired.cron (cron expression)

Behavior & compatibility

- Redirects (/ {shortCode}) now return 404 for soft-deleted records because lookups exclude deleted rows.
- Analytics endpoint can still return deleted rows; controllers may include deleted/expired info in the response payload.
- Existing behavior preserved for non-expired links.

Verification steps

1. Apply DB changes (or allow Hibernate to auto-update if it has permission).
2. Start the application.
3. Create a link with an expiresAt in the past (via API or DB insert).
4. Wait for the scheduled task or trigger softDeleteExpiredLinks() manually.
5. Verify the record has deleted = true and deleted_at is populated.
6. GET /{shortCode} should return 404 for that record.
7. GET /api/analytics/{shortCode} will still return the mapping (useful for audits).

Manual trigger / admin endpoint (optional)

- For convenience, an authenticated admin endpoint can be added to invoke softDeleteExpiredLinks() on demand for emergency cleanups.

Testing

- Add unit tests for UrlServiceImpl.softDeleteExpiredLinks():
  - Create records with past expiresAt and ensure method marks them deleted and sets deletedAt.
  - Ensure non-expired records remain unchanged.
- Add integration tests to assert redirect behavior and analytics behavior after soft-delete.

Rollback & archival

- Soft-delete keeps rows; if desired, implement an archival/hard-delete job that permanently removes soft-deleted rows older than N days.

Future improvements

- Expose metrics (Prometheus) for number of soft-deleted rows per run.
- Add an admin UI showing soft-deleted links with restore option.
- Add Flyway/Liquibase migrations for deterministic schema changes in CI/CD.

Contact / Notes

- This feature was implemented to preserve audit data and avoid accidental hard-deletions. For NeonDB ensure DB user has ALTER permissions if relying on Hibernate to auto-update schema; otherwise apply the ALTER statements manually or add a migration file.

