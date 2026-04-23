ALTER TABLE `chunk`
    ADD FULLTEXT KEY `ft_title_chunk_text` (`title`, `chunk_text`) WITH PARSER ngram;
