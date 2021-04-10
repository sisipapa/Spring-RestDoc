package com.sisipapa.toy2.service;

import com.sisipapa.toy2.domain.Posts;
import com.sisipapa.toy2.dto.PostsDTO;
import com.sisipapa.toy2.repository.PostsQeuryDslRepository;
import com.sisipapa.toy2.repository.PostsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class PostsService {

    private final PostsRepository repository;
    private final PostsQeuryDslRepository queryDslRepository;

    /**
     * 단건 조회
     *
     * @param id
     * @return
     */
    public PostsDTO getPost(Long id) {
        PostsDTO dto = queryDslRepository.findById(id);
        return dto;
    }

    /**
     * 등록/수정
     *
     * @param dto
     * @return
     */
    public PostsDTO savePost(PostsDTO dto) {

        Posts posts = Posts.builder()
                .id(dto.getId())
                .author(dto.getAuthor())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        posts = repository.save(posts);

        return PostsDTO.builder()
                .id(posts.getId())
                .author(posts.getAuthor())
                .title(posts.getTitle())
                .content(posts.getContent())
                .build();
    }

    /**
     * 삭제
     *
     * @param id
     */
    public boolean deletePost(Long id) {

        boolean result = true;
        repository.deleteById(id);
        return result;

    }
}
