-- Check existing duplicates before adding the stricter unique key.
-- If this query returns rows, merge or move those schedules before running ALTER TABLE.
SELECT
  trip_id,
  schedule_date,
  start_time,
  COUNT(*) AS duplicate_count
FROM SCHEDULE_ITEM
GROUP BY trip_id, schedule_date, start_time
HAVING COUNT(*) > 1;

ALTER TABLE SCHEDULE_ITEM
  DROP INDEX uk_schedule_trip_date_time_place,
  MODIFY place_id BIGINT NULL,
  ADD UNIQUE KEY uk_schedule_trip_date_time (trip_id, schedule_date, start_time);
