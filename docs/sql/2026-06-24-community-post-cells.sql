CREATE TABLE IF NOT EXISTS `COMMUNITY_POST_CELL` (
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
