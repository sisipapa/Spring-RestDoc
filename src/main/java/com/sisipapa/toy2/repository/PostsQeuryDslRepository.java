package com.sisipapa.toy2.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sisipapa.toy2.domain.Posts;
import com.sisipapa.toy2.dto.PostsDTO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.sisipapa.toy2.domain.QPosts.posts;

@Repository
public class PostsQeuryDslRepository {

    @Autowired
    private JPAQueryFactory queryFactory;

    /**
     * 단위테스트 조회
     *
     * @param author
     * @return
     */
    public Posts findByAuthor(String author) {

        queryFactory.update(posts);

        return queryFactory.selectFrom(posts)
                .where(posts.author.eq(author))
                .fetchOne();
    }

    /**
     * REST API GET 단건 요청
     *
     * @param id
     * @return
     */
    public PostsDTO findById(Long id) {
        return queryFactory.select(
                Projections.constructor(
                        PostsDTO.class,
                        posts.id,
                        posts.title,
                        posts.author,
                        posts.content))
                .from(posts)
                .where(posts.id.eq(id))
                .fetchOne();
    }

}
