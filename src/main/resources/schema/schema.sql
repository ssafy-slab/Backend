CREATE DATABASE IF NOT EXISTS `finalproject`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `finalproject`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `REPORT`;
DROP TABLE IF EXISTS `COMMENT`;
DROP TABLE IF EXISTS `COMMUNITY_POST`;
DROP TABLE IF EXISTS `AI_RECOMMENDATION`;
DROP TABLE IF EXISTS `CHECKLIST_ITEM`;
DROP TABLE IF EXISTS `VOTE_BALLOT`;
DROP TABLE IF EXISTS `VOTE_OPTION`;
DROP TABLE IF EXISTS `VOTE`;
DROP TABLE IF EXISTS `AI_SUGGESTION`;
DROP TABLE IF EXISTS `CHAT_MESSAGE`;
DROP TABLE IF EXISTS `SCHEDULE_ITEM`;
DROP TABLE IF EXISTS `TRIP_MEMBER`;
DROP TABLE IF EXISTS `TRIP`;
DROP TABLE IF EXISTS `PLACE_REVIEW`;
DROP TABLE IF EXISTS `PLACE`;
DROP TABLE IF EXISTS `REGION`;
DROP TABLE IF EXISTS `USER`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `USER` (
  `user_id` VARCHAR(64) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `password_hash` VARCHAR(255) NOT NULL,
  `nickname` VARCHAR(100) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'USER',
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `REGION` (
  `region_id` BIGINT NOT NULL,
  `region_name` VARCHAR(150) NOT NULL,
  `parent_region_id` BIGINT NULL,
  `parent_region_name` VARCHAR(150) NULL,
  PRIMARY KEY (`region_id`, `region_name`),
  CONSTRAINT `fk_region_parent`
    FOREIGN KEY (`parent_region_id`, `parent_region_name`)
    REFERENCES `REGION` (`region_id`, `region_name`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `PLACE` (
  `region_id` BIGINT NOT NULL,
  `region_name` VARCHAR(150) NOT NULL,
  `place_id` BIGINT NOT NULL,
  `place_name` VARCHAR(150) NOT NULL,
  `category` VARCHAR(100) NULL,
  `address` VARCHAR(500) NULL,
  `latitude` DECIMAL(10,7) NULL,
  `longitude` DECIMAL(10,7) NULL,
  `description` TEXT NULL,
  `image_url` VARCHAR(1000) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`region_id`, `region_name`, `place_id`, `place_name`),
  KEY `idx_place_id` (`place_id`),
  CONSTRAINT `fk_place_region`
    FOREIGN KEY (`region_id`, `region_name`)
    REFERENCES `REGION` (`region_id`, `region_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `PLACE_REVIEW` (
  `region_id` BIGINT NOT NULL,
  `region_name` VARCHAR(150) NOT NULL,
  `place_id` BIGINT NOT NULL,
  `place_name` VARCHAR(150) NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `rating` INT NOT NULL,
  `content` TEXT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`region_id`, `region_name`, `place_id`, `place_name`, `user_id`, `review_id`),
  KEY `idx_place_review_review_id` (`review_id`),
  KEY `idx_place_review_user` (`user_id`),
  CONSTRAINT `fk_place_review_place`
    FOREIGN KEY (`region_id`, `region_name`, `place_id`, `place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_place_review_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `ck_place_review_rating`
    CHECK (`rating` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `TRIP` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `trip_type` VARCHAR(50) NULL,
  `start_date` DATE NULL,
  `end_date` DATE NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`owner_user_id`, `trip_id`),
  KEY `idx_trip_id` (`trip_id`),
  CONSTRAINT `fk_trip_owner`
    FOREIGN KEY (`owner_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `TRIP_MEMBER` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `member_role` VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
  `invite_status` VARCHAR(50) NOT NULL DEFAULT 'ACCEPTED',
  `joined_at` DATETIME NULL,
  PRIMARY KEY (`owner_user_id`, `trip_id`, `user_id`),
  KEY `idx_trip_member_user` (`user_id`),
  CONSTRAINT `fk_trip_member_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_trip_member_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `SCHEDULE_ITEM` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `region_id` BIGINT NOT NULL,
  `region_name` VARCHAR(150) NOT NULL,
  `place_id` BIGINT NOT NULL,
  `place_name` VARCHAR(150) NOT NULL,
  `created_by_user_id` VARCHAR(64) NOT NULL,
  `day_no` INT NULL,
  `schedule_date` DATE NOT NULL,
  `start_time` TIME NOT NULL,
  `end_time` TIME NULL,
  `title` VARCHAR(255) NULL,
  `memo` TEXT NULL,
  `sort_order` INT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`owner_user_id`, `trip_id`, `region_id`, `region_name`, `place_id`, `place_name`, `created_by_user_id`, `schedule_date`, `start_time`),
  KEY `idx_schedule_place` (`region_id`, `region_name`, `place_id`, `place_name`),
  KEY `idx_schedule_creator` (`created_by_user_id`),
  CONSTRAINT `fk_schedule_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_schedule_place`
    FOREIGN KEY (`region_id`, `region_name`, `place_id`, `place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT `fk_schedule_creator`
    FOREIGN KEY (`created_by_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `CHAT_MESSAGE` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `sender_user_id` VARCHAR(64) NOT NULL,
  `message_id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_type` VARCHAR(50) NOT NULL DEFAULT 'TEXT',
  `content` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`owner_user_id`, `trip_id`, `sender_user_id`, `message_id`),
  KEY `idx_chat_message_id` (`message_id`),
  KEY `idx_chat_sender` (`sender_user_id`),
  CONSTRAINT `fk_chat_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_chat_sender`
    FOREIGN KEY (`sender_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `AI_SUGGESTION` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `sender_user_id` VARCHAR(64) NOT NULL,
  `source_message_id` BIGINT NOT NULL,
  `ai_suggestion_id` BIGINT NOT NULL AUTO_INCREMENT,
  `region_id` BIGINT NULL,
  `region_name` VARCHAR(150) NULL,
  `suggested_place_id` BIGINT NULL,
  `suggested_place_name` VARCHAR(150) NULL,
  `suggestion_type` VARCHAR(50) NULL,
  `suggested_title` VARCHAR(255) NULL,
  `summary` TEXT NULL,
  `reason` TEXT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `applied_at` DATETIME NULL,
  PRIMARY KEY (`owner_user_id`, `trip_id`, `sender_user_id`, `source_message_id`, `ai_suggestion_id`),
  KEY `idx_ai_suggestion_id` (`ai_suggestion_id`),
  KEY `idx_ai_suggestion_place` (`region_id`, `region_name`, `suggested_place_id`, `suggested_place_name`),
  CONSTRAINT `fk_ai_suggestion_message`
    FOREIGN KEY (`owner_user_id`, `trip_id`, `sender_user_id`, `source_message_id`)
    REFERENCES `CHAT_MESSAGE` (`owner_user_id`, `trip_id`, `sender_user_id`, `message_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_ai_suggestion_place`
    FOREIGN KEY (`region_id`, `region_name`, `suggested_place_id`, `suggested_place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `VOTE` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `creator_user_id` VARCHAR(64) NOT NULL,
  `vote_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'OPEN',
  `started_at` DATETIME NULL,
  `ended_at` DATETIME NULL,
  PRIMARY KEY (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`),
  KEY `idx_vote_id` (`vote_id`),
  KEY `idx_vote_creator` (`creator_user_id`),
  CONSTRAINT `fk_vote_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_creator`
    FOREIGN KEY (`creator_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `VOTE_OPTION` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `creator_user_id` VARCHAR(64) NOT NULL,
  `vote_id` BIGINT NOT NULL,
  `region_id` BIGINT NULL,
  `region_name` VARCHAR(150) NULL,
  `place_id` BIGINT NULL,
  `place_name` VARCHAR(150) NULL,
  `vote_option_id` BIGINT NOT NULL AUTO_INCREMENT,
  `option_title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `sort_order` INT NULL,
  PRIMARY KEY (`vote_id`, `vote_option_id`),
  KEY `idx_vote_option_id` (`vote_option_id`),
  KEY `idx_vote_option_vote` (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`),
  KEY `idx_vote_option_place` (`region_id`, `region_name`, `place_id`, `place_name`),
  CONSTRAINT `fk_vote_option_vote`
    FOREIGN KEY (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`)
    REFERENCES `VOTE` (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_option_place`
    FOREIGN KEY (`region_id`, `region_name`, `place_id`, `place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `VOTE_BALLOT` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `creator_user_id` VARCHAR(64) NOT NULL,
  `vote_id` BIGINT NOT NULL,
  `region_id` BIGINT NULL,
  `region_name` VARCHAR(150) NULL,
  `place_id` BIGINT NULL,
  `place_name` VARCHAR(150) NULL,
  `vote_option_id` BIGINT NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `voted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`vote_id`, `vote_option_id`, `user_id`),
  KEY `idx_vote_ballot_vote` (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`),
  KEY `idx_vote_ballot_place` (`region_id`, `region_name`, `place_id`, `place_name`),
  KEY `idx_vote_ballot_user` (`user_id`),
  CONSTRAINT `fk_vote_ballot_vote`
    FOREIGN KEY (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`)
    REFERENCES `VOTE` (`owner_user_id`, `trip_id`, `creator_user_id`, `vote_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_ballot_option`
    FOREIGN KEY (`vote_id`, `vote_option_id`)
    REFERENCES `VOTE_OPTION` (`vote_id`, `vote_option_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_vote_ballot_place`
    FOREIGN KEY (`region_id`, `region_name`, `place_id`, `place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT `fk_vote_ballot_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `CHECKLIST_ITEM` (
  `owner_user_id` VARCHAR(64) NOT NULL,
  `trip_id` BIGINT NOT NULL,
  `checklist_item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `assignee_user_id` VARCHAR(64) NULL,
  `title` VARCHAR(255) NOT NULL,
  `is_done` BOOLEAN NOT NULL DEFAULT FALSE,
  `due_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_at` DATETIME NULL,
  PRIMARY KEY (`owner_user_id`, `trip_id`, `checklist_item_id`),
  KEY `idx_checklist_item_id` (`checklist_item_id`),
  KEY `idx_checklist_assignee` (`assignee_user_id`),
  CONSTRAINT `fk_checklist_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_checklist_assignee`
    FOREIGN KEY (`assignee_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `AI_RECOMMENDATION` (
  `user_id` VARCHAR(64) NOT NULL,
  `recommendation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_user_id` VARCHAR(64) NULL,
  `trip_id` BIGINT NULL,
  `region_id` BIGINT NULL,
  `region_name` VARCHAR(150) NULL,
  `place_id` BIGINT NULL,
  `place_name` VARCHAR(150) NULL,
  `condition_text` TEXT NULL,
  `reason` TEXT NULL,
  `sort_order` INT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `recommendation_id`),
  KEY `idx_ai_recommendation_id` (`recommendation_id`),
  KEY `idx_ai_recommendation_trip` (`owner_user_id`, `trip_id`),
  KEY `idx_ai_recommendation_place` (`region_id`, `region_name`, `place_id`, `place_name`),
  CONSTRAINT `fk_ai_recommendation_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_ai_recommendation_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT `fk_ai_recommendation_place`
    FOREIGN KEY (`region_id`, `region_name`, `place_id`, `place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `COMMUNITY_POST` (
  `user_id` VARCHAR(64) NOT NULL,
  `post_id` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_user_id` VARCHAR(64) NULL,
  `trip_id` BIGINT NULL,
  `region_id` BIGINT NULL,
  `region_name` VARCHAR(150) NULL,
  `place_id` BIGINT NULL,
  `place_name` VARCHAR(150) NULL,
  `category` VARCHAR(100) NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NULL,
  `image_url` VARCHAR(1000) NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `post_id`),
  KEY `idx_community_post_id` (`post_id`),
  KEY `idx_community_post_trip` (`owner_user_id`, `trip_id`),
  KEY `idx_community_post_place` (`region_id`, `region_name`, `place_id`, `place_name`),
  CONSTRAINT `fk_community_post_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_community_post_trip`
    FOREIGN KEY (`owner_user_id`, `trip_id`)
    REFERENCES `TRIP` (`owner_user_id`, `trip_id`)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT `fk_community_post_place`
    FOREIGN KEY (`region_id`, `region_name`, `place_id`, `place_name`)
    REFERENCES `PLACE` (`region_id`, `region_name`, `place_id`, `place_name`)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `COMMENT` (
  `post_user_id` VARCHAR(64) NOT NULL,
  `post_id` BIGINT NOT NULL,
  `commenter_user_id` VARCHAR(64) NOT NULL,
  `parent_comment_id` BIGINT NOT NULL DEFAULT 0,
  `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `content` TEXT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_user_id`, `post_id`, `commenter_user_id`, `parent_comment_id`, `comment_id`),
  KEY `idx_comment_id` (`comment_id`),
  KEY `idx_comment_post` (`post_user_id`, `post_id`),
  KEY `idx_comment_commenter` (`commenter_user_id`),
  KEY `idx_comment_parent` (`parent_comment_id`),
  CONSTRAINT `fk_comment_post`
    FOREIGN KEY (`post_user_id`, `post_id`)
    REFERENCES `COMMUNITY_POST` (`user_id`, `post_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_comment_commenter`
    FOREIGN KEY (`commenter_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `REPORT` (
  `reporter_user_id` VARCHAR(64) NOT NULL,
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` VARCHAR(50) NOT NULL,
  `target_id` VARCHAR(255) NOT NULL,
  `reason` TEXT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolved_at` DATETIME NULL,
  PRIMARY KEY (`reporter_user_id`, `report_id`),
  KEY `idx_report_id` (`report_id`),
  CONSTRAINT `fk_report_reporter`
    FOREIGN KEY (`reporter_user_id`)
    REFERENCES `USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
