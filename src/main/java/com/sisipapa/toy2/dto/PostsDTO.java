package com.sisipapa.toy2.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsDTO {
    private Long id;
    private String title;
    private String author;
    private String content;
}
