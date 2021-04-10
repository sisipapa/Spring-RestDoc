package com.sisipapa.toy2.repository;

import com.sisipapa.toy2.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsRepository extends JpaRepository<Posts, Long> {
}
