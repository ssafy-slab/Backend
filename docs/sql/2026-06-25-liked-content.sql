INSERT IGNORE INTO COMMUNITY_POST_LIKE (post_id, user_id, created_at)
SELECT post_id, user_id, created_at
FROM COMMUNITY_POST_BOOKMARK;

DROP TABLE COMMUNITY_POST_BOOKMARK;

CREATE TABLE IF NOT EXISTS PLACE_LIKE (
  place_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (place_id, user_id),
  KEY idx_place_like_user_created (user_id, created_at),
  CONSTRAINT fk_place_like_place
    FOREIGN KEY (place_id) REFERENCES PLACE (place_id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_place_like_user
    FOREIGN KEY (user_id) REFERENCES APP_USER (user_id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
