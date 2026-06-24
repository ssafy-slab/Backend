CREATE DATABASE IF NOT EXISTS `finalproject`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `finalproject`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `REPORT`;
DROP TABLE IF EXISTS `COMMENT`;
DROP TABLE IF EXISTS `COMMUNITY_POST_LIKE`;
DROP TABLE IF EXISTS `COMMUNITY_POST_CELL`;
DROP TABLE IF EXISTS `COMMUNITY_POST`;
DROP TABLE IF EXISTS `AI_RECOMMENDATION`;
DROP TABLE IF EXISTS `CHECKLIST_ITEM`;
DROP TABLE IF EXISTS `AI_SUGGESTION_VOTE`;
DROP TABLE IF EXISTS `VOTE_BALLOT`;
DROP TABLE IF EXISTS `VOTE_OPTION`;
DROP TABLE IF EXISTS `VOTE`;
DROP TABLE IF EXISTS `AI_SUGGESTION`;
DROP TABLE IF EXISTS `AI_ANALYSIS_STATE`;
DROP TABLE IF EXISTS `AI_ANALYSIS_RUN`;
DROP TABLE IF EXISTS `CHAT_MESSAGE`;
DROP TABLE IF EXISTS `SCHEDULE_ITEM`;
DROP TABLE IF EXISTS `TRIP_INVITE_CODE`;
DROP TABLE IF EXISTS `TRIP_MEMBER`;
DROP TABLE IF EXISTS `TRIP`;
DROP TABLE IF EXISTS `PLACE_REVIEW`;
DROP TABLE IF EXISTS `PLACE_NEARBY_FACILITY`;
DROP TABLE IF EXISTS `PLACE_NEARBY_FACILITY_CACHE`;
DROP TABLE IF EXISTS `FACILITY`;
DROP TABLE IF EXISTS `PLACE`;
DROP TABLE IF EXISTS `REGION`;
DROP TABLE IF EXISTS `OAUTH_ACCOUNT`;
DROP TABLE IF EXISTS `APP_USER`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `APP_USER` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NULL,
  `nickname` VARCHAR(100) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'USER',
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_app_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `OAUTH_ACCOUNT` (
  `oauth_account_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `provider` VARCHAR(30) NOT NULL,
  `provider_user_id` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`oauth_account_id`),
  UNIQUE KEY `uk_oauth_account_provider_user` (`provider`, `provider_user_id`),
  KEY `idx_oauth_account_user` (`user_id`),
  CONSTRAINT `fk_oauth_account_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `REGION` (
  `region_id` BIGINT NOT NULL AUTO_INCREMENT,
  `ldong_code` VARCHAR(10) NOT NULL,
  `region_name` VARCHAR(150) NOT NULL,
  `region_full_name` VARCHAR(300) NOT NULL,
  `region_level` TINYINT NOT NULL,
  `parent_region_id` BIGINT NULL,
  `ldong_regn_cd` VARCHAR(10) NOT NULL,
  `ldong_signgu_cd` VARCHAR(10) NOT NULL DEFAULT '000',
  `area_code` VARCHAR(10) NULL,
  `sigungu_code` VARCHAR(10) NULL,
  PRIMARY KEY (`region_id`),
  UNIQUE KEY `uk_region_ldong_code` (`ldong_code`),
  UNIQUE KEY `uk_region_ldong` (`ldong_regn_cd`, `ldong_signgu_cd`),
  KEY `idx_region_parent` (`parent_region_id`),
  KEY `idx_region_ldong_regn` (`ldong_regn_cd`, `ldong_signgu_cd`),
  KEY `idx_region_area` (`area_code`, `sigungu_code`),
  CONSTRAINT `fk_region_parent`
    FOREIGN KEY (`parent_region_id`)
    REFERENCES `REGION` (`region_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `PLACE` (
  `place_id` BIGINT NOT NULL AUTO_INCREMENT,
  `source_provider` VARCHAR(30) NOT NULL DEFAULT 'TOUR_API',
  `content_id` BIGINT NOT NULL,
  `content_type_id` VARCHAR(20) NULL,
  `region_id` BIGINT NOT NULL,
  `place_name` VARCHAR(150) NOT NULL,
  `category` VARCHAR(100) NULL,
  `cat1` VARCHAR(20) NULL,
  `cat2` VARCHAR(20) NULL,
  `cat3` VARCHAR(20) NULL,
  `lcls_systm1` VARCHAR(20) NULL,
  `lcls_systm2` VARCHAR(20) NULL,
  `lcls_systm3` VARCHAR(20) NULL,
  `area_code` VARCHAR(10) NULL,
  `sigungu_code` VARCHAR(10) NULL,
  `ldong_code` VARCHAR(10) NULL,
  `ldong_regn_cd` VARCHAR(10) NULL,
  `ldong_signgu_cd` VARCHAR(10) NULL,
  `address` VARCHAR(500) NULL,
  `zipcode` VARCHAR(20) NULL,
  `tel` VARCHAR(100) NULL,
  `latitude` DECIMAL(13,10) NULL,
  `longitude` DECIMAL(13,10) NULL,
  `map_level` VARCHAR(10) NULL,
  `description` TEXT NULL,
  `image_url` VARCHAR(1000) NULL,
  `thumbnail_url` VARCHAR(1000) NULL,
  `copyright_type` VARCHAR(50) NULL,
  `source_created_time` VARCHAR(14) NULL,
  `source_modified_time` VARCHAR(14) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`place_id`),
  UNIQUE KEY `uk_place_source_content` (`source_provider`, `content_id`),
  KEY `idx_place_region` (`region_id`),
  KEY `idx_place_category` (`category`),
  KEY `idx_place_content_type` (`content_type_id`),
  KEY `idx_place_ldong_code` (`ldong_code`),
  KEY `idx_place_ldong` (`ldong_regn_cd`, `ldong_signgu_cd`),
  KEY `idx_place_area` (`area_code`, `sigungu_code`),
  CONSTRAINT `fk_place_region`
    FOREIGN KEY (`region_id`)
    REFERENCES `REGION` (`region_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `FACILITY` (
  `facility_id` BIGINT NOT NULL AUTO_INCREMENT,
  `source_provider` VARCHAR(30) NOT NULL DEFAULT 'KAKAO',
  `external_id` VARCHAR(100) NOT NULL,
  `facility_type` VARCHAR(50) NOT NULL,
  `category_group_code` VARCHAR(20) NOT NULL,
  `category_name` VARCHAR(255) NULL,
  `facility_name` VARCHAR(150) NOT NULL,
  `phone` VARCHAR(100) NULL,
  `address` VARCHAR(500) NULL,
  `road_address` VARCHAR(500) NULL,
  `latitude` DECIMAL(13,10) NULL,
  `longitude` DECIMAL(13,10) NULL,
  `place_url` VARCHAR(1000) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`facility_id`),
  UNIQUE KEY `uk_facility_source_external` (`source_provider`, `external_id`),
  KEY `idx_facility_type` (`facility_type`),
  KEY `idx_facility_category_group` (`category_group_code`),
  KEY `idx_facility_location` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `PLACE_NEARBY_FACILITY_CACHE` (
  `place_id` BIGINT NOT NULL,
  `facility_type` VARCHAR(50) NOT NULL,
  `search_radius_m` INT NOT NULL,
  `result_count` INT NOT NULL DEFAULT 0,
  `fetched_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`place_id`, `facility_type`, `search_radius_m`),
  KEY `idx_place_nearby_facility_cache_fetched` (`fetched_at`),
  CONSTRAINT `fk_place_nearby_facility_cache_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `PLACE_NEARBY_FACILITY` (
  `place_id` BIGINT NOT NULL,
  `facility_id` BIGINT NOT NULL,
  `facility_type` VARCHAR(50) NOT NULL,
  `distance_m` INT NULL,
  `search_radius_m` INT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`place_id`, `facility_id`, `search_radius_m`),
  KEY `idx_place_nearby_facility_place_type` (`place_id`, `facility_type`, `distance_m`),
  KEY `idx_place_nearby_facility_facility` (`facility_id`),
  CONSTRAINT `fk_place_nearby_facility_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_place_nearby_facility_facility`
    FOREIGN KEY (`facility_id`)
    REFERENCES `FACILITY` (`facility_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `PLACE_REVIEW` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `place_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `rating` INT NOT NULL,
  `content` TEXT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  UNIQUE KEY `uk_place_review_place_user` (`place_id`, `user_id`),
  KEY `idx_place_review_user` (`user_id`),
  CONSTRAINT `fk_place_review_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_place_review_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `ck_place_review_rating`
    CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `TRIP` (
  `trip_id` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_user_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `trip_type` VARCHAR(50) NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`trip_id`),
  KEY `idx_trip_owner` (`owner_user_id`),
  KEY `idx_trip_owner_status` (`owner_user_id`, `status`),
  CONSTRAINT `fk_trip_owner`
    FOREIGN KEY (`owner_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `TRIP_MEMBER` (
  `trip_member_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `member_role` VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
  `invite_status` VARCHAR(50) NOT NULL DEFAULT 'ACCEPTED',
  `joined_at` DATETIME NULL,
  PRIMARY KEY (`trip_member_id`),
  UNIQUE KEY `uk_trip_member_trip_user` (`trip_id`, `user_id`),
  KEY `idx_trip_member_user` (`user_id`),
  CONSTRAINT `fk_trip_member_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_trip_member_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `TRIP_INVITE_CODE` (
  `trip_invite_code_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `invite_code` VARCHAR(20) NOT NULL,
  `created_by_user_id` BIGINT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`trip_invite_code_id`),
  UNIQUE KEY `uk_trip_invite_code` (`invite_code`),
  KEY `idx_trip_invite_code_trip_status` (`trip_id`, `status`),
  KEY `idx_trip_invite_code_creator` (`created_by_user_id`),
  CONSTRAINT `fk_trip_invite_code_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_trip_invite_code_creator`
    FOREIGN KEY (`created_by_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `SCHEDULE_ITEM` (
  `schedule_item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `place_id` BIGINT NULL,
  `created_by_user_id` BIGINT NOT NULL,
  `day_no` INT NULL,
  `schedule_date` DATE NOT NULL,
  `start_time` TIME NOT NULL,
  `end_time` TIME NULL,
  `title` VARCHAR(255) NULL,
  `memo` TEXT NULL,
  `sort_order` INT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`schedule_item_id`),
  UNIQUE KEY `uk_schedule_trip_date_time` (`trip_id`, `schedule_date`, `start_time`),
  KEY `idx_schedule_trip_date` (`trip_id`, `schedule_date`),
  KEY `idx_schedule_trip_day_order` (`trip_id`, `day_no`, `sort_order`),
  KEY `idx_schedule_place` (`place_id`),
  KEY `idx_schedule_creator` (`created_by_user_id`),
  CONSTRAINT `fk_schedule_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_schedule_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT `fk_schedule_creator`
    FOREIGN KEY (`created_by_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `CHAT_MESSAGE` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `sender_user_id` BIGINT NOT NULL,
  `message_type` VARCHAR(50) NOT NULL DEFAULT 'TEXT',
  `content` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `idx_chat_trip_created` (`trip_id`, `created_at`),
  KEY `idx_chat_sender` (`sender_user_id`),
  CONSTRAINT `fk_chat_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_chat_sender`
    FOREIGN KEY (`sender_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE `VOTE` (
  `vote_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `creator_user_id` BIGINT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'OPEN',
  `started_at` DATETIME NULL,
  `ended_at` DATETIME NULL,
  PRIMARY KEY (`vote_id`),
  KEY `idx_vote_trip_status` (`trip_id`, `status`),
  KEY `idx_vote_creator` (`creator_user_id`),
  CONSTRAINT `fk_vote_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_creator`
    FOREIGN KEY (`creator_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `VOTE_OPTION` (
  `vote_option_id` BIGINT NOT NULL AUTO_INCREMENT,
  `vote_id` BIGINT NOT NULL,
  `place_id` BIGINT NULL,
  `option_title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `sort_order` INT NULL,
  PRIMARY KEY (`vote_option_id`),
  KEY `idx_vote_option_vote_order` (`vote_id`, `sort_order`),
  KEY `idx_vote_option_place` (`place_id`),
  CONSTRAINT `fk_vote_option_vote`
    FOREIGN KEY (`vote_id`)
    REFERENCES `VOTE` (`vote_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_option_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `VOTE_BALLOT` (
  `vote_ballot_id` BIGINT NOT NULL AUTO_INCREMENT,
  `vote_id` BIGINT NOT NULL,
  `vote_option_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `voted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`vote_ballot_id`),
  UNIQUE KEY `uk_vote_ballot_vote_user` (`vote_id`, `user_id`),
  KEY `idx_vote_ballot_option` (`vote_option_id`),
  KEY `idx_vote_ballot_user` (`user_id`),
  CONSTRAINT `fk_vote_ballot_vote`
    FOREIGN KEY (`vote_id`)
    REFERENCES `VOTE` (`vote_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_ballot_option`
    FOREIGN KEY (`vote_option_id`)
    REFERENCES `VOTE_OPTION` (`vote_option_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_ballot_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `CHECKLIST_ITEM` (
  `checklist_item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `trip_id` BIGINT NOT NULL,
  `assignee_user_id` BIGINT NULL,
  `title` VARCHAR(255) NOT NULL,
  `is_done` BOOLEAN NOT NULL DEFAULT FALSE,
  `due_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME NULL,
  PRIMARY KEY (`checklist_item_id`),
  KEY `idx_checklist_trip_done` (`trip_id`, `is_done`),
  KEY `idx_checklist_assignee` (`assignee_user_id`),
  CONSTRAINT `fk_checklist_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_checklist_assignee`
    FOREIGN KEY (`assignee_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `AI_RECOMMENDATION` (
  `recommendation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `trip_id` BIGINT NULL,
  `place_id` BIGINT NULL,
  `condition_text` TEXT NULL,
  `reason` TEXT NULL,
  `sort_order` INT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`recommendation_id`),
  KEY `idx_ai_recommendation_user` (`user_id`),
  KEY `idx_ai_recommendation_trip` (`trip_id`),
  KEY `idx_ai_recommendation_place` (`place_id`),
  CONSTRAINT `fk_ai_recommendation_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_ai_recommendation_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT `fk_ai_recommendation_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `COMMUNITY_POST` (
  `post_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `trip_id` BIGINT NULL,
  `place_id` BIGINT NULL,
  `category` VARCHAR(100) NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NULL,
  `image_url` VARCHAR(1000) NULL,
  `view_count` BIGINT NOT NULL DEFAULT 0,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`),
  KEY `idx_community_post_user` (`user_id`),
  KEY `idx_community_post_trip` (`trip_id`),
  KEY `idx_community_post_place` (`place_id`),
  KEY `idx_community_post_category_status` (`category`, `status`),
  KEY `idx_community_post_created` (`created_at`, `post_id`),
  CONSTRAINT `fk_community_post_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_community_post_trip`
    FOREIGN KEY (`trip_id`)
    REFERENCES `TRIP` (`trip_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT `fk_community_post_place`
    FOREIGN KEY (`place_id`)
    REFERENCES `PLACE` (`place_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `AI_SUGGESTION_VOTE` (
  `ai_suggestion_vote_id` BIGINT NOT NULL AUTO_INCREMENT,
  `ai_suggestion_id` BIGINT NOT NULL,
  `vote_id` BIGINT NOT NULL,
  `approve_option_id` BIGINT NOT NULL,
  `reject_option_id` BIGINT NOT NULL,
  `resolution` VARCHAR(30) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME NULL,
  PRIMARY KEY (`ai_suggestion_vote_id`),
  UNIQUE KEY `uk_ai_suggestion_vote_suggestion` (`ai_suggestion_id`),
  UNIQUE KEY `uk_ai_suggestion_vote_vote` (`vote_id`),
  KEY `idx_ai_suggestion_vote_approve_option` (`approve_option_id`),
  KEY `idx_ai_suggestion_vote_reject_option` (`reject_option_id`),
  CONSTRAINT `fk_ai_suggestion_vote_suggestion`
    FOREIGN KEY (`ai_suggestion_id`) REFERENCES `AI_SUGGESTION` (`ai_suggestion_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_suggestion_vote_vote`
    FOREIGN KEY (`vote_id`) REFERENCES `VOTE` (`vote_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_suggestion_vote_approve_option`
    FOREIGN KEY (`approve_option_id`) REFERENCES `VOTE_OPTION` (`vote_option_id`)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_ai_suggestion_vote_reject_option`
    FOREIGN KEY (`reject_option_id`) REFERENCES `VOTE_OPTION` (`vote_option_id`)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `COMMUNITY_POST_CELL` (
  `post_cell_id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `sort_order` INT NOT NULL,
  `cell_type` VARCHAR(20) NOT NULL,
  `text_content` TEXT NULL,
  `image_url` VARCHAR(1000) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_cell_id`),
  KEY `idx_community_post_cell_post_order` (`post_id`, `sort_order`),
  CONSTRAINT `fk_community_post_cell_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `COMMUNITY_POST` (`post_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `COMMUNITY_POST_LIKE` (
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`, `user_id`),
  KEY `idx_community_post_like_user` (`user_id`),
  CONSTRAINT `fk_community_post_like_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `COMMUNITY_POST` (`post_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_community_post_like_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `COMMENT` (
  `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `commenter_user_id` BIGINT NOT NULL,
  `parent_comment_id` BIGINT NULL,
  `content` TEXT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_comment_post_created` (`post_id`, `created_at`),
  KEY `idx_comment_commenter` (`commenter_user_id`),
  KEY `idx_comment_parent` (`parent_comment_id`),
  CONSTRAINT `fk_comment_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `COMMUNITY_POST` (`post_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_comment_commenter`
    FOREIGN KEY (`commenter_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_comment_parent`
    FOREIGN KEY (`parent_comment_id`)
    REFERENCES `COMMENT` (`comment_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `REPORT` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `reporter_user_id` BIGINT NOT NULL,
  `target_type` VARCHAR(50) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `reason` TEXT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME NULL,
  PRIMARY KEY (`report_id`),
  KEY `idx_report_reporter` (`reporter_user_id`),
  KEY `idx_report_target` (`target_type`, `target_id`),
  KEY `idx_report_status` (`status`),
  CONSTRAINT `fk_report_reporter`
    FOREIGN KEY (`reporter_user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
