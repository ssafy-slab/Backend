USE `finalproject`;

CREATE TABLE IF NOT EXISTS `AI_SUGGESTION_VOTE` (
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
