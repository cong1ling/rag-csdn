ALTER TABLE `session`
    ADD COLUMN `conversation_summary` TEXT NULL COMMENT '对话摘要' AFTER `video_id`,
    ADD COLUMN `summary_update_time` DATETIME NULL COMMENT '摘要更新时间' AFTER `conversation_summary`;
