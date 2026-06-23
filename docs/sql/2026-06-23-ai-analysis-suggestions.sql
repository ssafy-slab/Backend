-- Canonical tables for durable chat analysis and schedule suggestions.
-- SCHEDULE_ITEM.place_id must be nullable before applying free-form suggestions.

CREATE TABLE `AI_ANALYSIS_RUN` (
  `analysis_run_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `requested_by_user_id` BIGINT NULL,
  `trigger_type` VARCHAR(30) NOT NULL,
  `first_message_id` BIGINT NOT NULL,
  `last_message_id` BIGINT NOT NULL,
  `message_count` INT NOT NULL,
  `status` VARCHAR(30) NOT NULL DEFAULT 'RUNNING',
  `error_message` VARCHAR(1000) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME NULL,
  PRIMARY KEY (`analysis_run_id`),
  KEY `idx_ai_analysis_run_trip_created` (`trip_id`, `created_at`),
  KEY `idx_ai_analysis_run_status` (`status`),
  KEY `idx_ai_analysis_run_requester` (`requested_by_user_id`),
  CONSTRAINT `fk_ai_analysis_run_trip`
    FOREIGN KEY (`trip_id`) REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_analysis_run_requester`
    FOREIGN KEY (`requested_by_user_id`) REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT `fk_ai_analysis_run_first_message`
    FOREIGN KEY (`first_message_id`) REFERENCES `CHAT_MESSAGE` (`message_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_analysis_run_last_message`
    FOREIGN KEY (`last_message_id`) REFERENCES `CHAT_MESSAGE` (`message_id`)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `AI_ANALYSIS_STATE` (
  `trip_id` BIGINT NOT NULL,
  `last_analyzed_message_id` BIGINT NULL,
  `analysis_status` VARCHAR(30) NOT NULL DEFAULT 'IDLE',
  `retry_count` INT NOT NULL DEFAULT 0,
  `next_retry_at` DATETIME NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`trip_id`),
  KEY `idx_ai_analysis_state_last_message` (`last_analyzed_message_id`),
  CONSTRAINT `fk_ai_analysis_state_trip`
    FOREIGN KEY (`trip_id`) REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_analysis_state_last_message`
    FOREIGN KEY (`last_analyzed_message_id`) REFERENCES `CHAT_MESSAGE` (`message_id`)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `AI_SUGGESTION` (
  `ai_suggestion_id` BIGINT NOT NULL AUTO_INCREMENT,
  `analysis_run_id` BIGINT NOT NULL,
  `suggested_place_id` BIGINT NULL,
  `suggested_place_name` VARCHAR(255) NULL,
  `suggested_region_hint` VARCHAR(255) NULL,
  `suggestion_type` VARCHAR(50) NOT NULL DEFAULT 'SCHEDULE',
  `suggested_title` VARCHAR(255) NOT NULL,
  `summary` TEXT NULL,
  `reason` TEXT NULL,
  `schedule_date` DATE NOT NULL,
  `start_time` TIME NOT NULL,
  `end_time` TIME NULL,
  `day_no` INT NULL,
  `sort_order` INT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `applied_schedule_item_id` BIGINT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `applied_at` DATETIME NULL,
  PRIMARY KEY (`ai_suggestion_id`),
  KEY `idx_ai_suggestion_run_status` (`analysis_run_id`, `status`),
  KEY `idx_ai_suggestion_place` (`suggested_place_id`),
  KEY `idx_ai_suggestion_applied_schedule` (`applied_schedule_item_id`),
  CONSTRAINT `fk_ai_suggestion_analysis_run`
    FOREIGN KEY (`analysis_run_id`) REFERENCES `AI_ANALYSIS_RUN` (`analysis_run_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_suggestion_applied_schedule`
    FOREIGN KEY (`applied_schedule_item_id`) REFERENCES `SCHEDULE_ITEM` (`schedule_item_id`)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT `fk_ai_suggestion_place`
    FOREIGN KEY (`suggested_place_id`) REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
