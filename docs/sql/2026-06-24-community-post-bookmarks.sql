CREATE TABLE IF NOT EXISTS `COMMUNITY_POST_BOOKMARK` (
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`, `user_id`),
  KEY `idx_community_post_bookmark_user_created` (`user_id`, `created_at`),
  CONSTRAINT `fk_community_post_bookmark_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `COMMUNITY_POST` (`post_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT `fk_community_post_bookmark_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `APP_USER` (`user_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
