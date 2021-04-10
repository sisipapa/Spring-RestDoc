package com.sisipapa.toy2.controller;


import com.sisipapa.toy2.dto.PostsDTO;
import com.sisipapa.toy2.response.BasicResponse;
import com.sisipapa.toy2.response.ErrorResponse;
import com.sisipapa.toy2.response.PostsResponse;
import com.sisipapa.toy2.service.PostsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/v1")
@RestController
public class PostsController {

    private final PostsService service;

    /**
     * 등록(Create)
     *
     * @param dto
     * @return
     */
    @PostMapping("/posts")
    public ResponseEntity<? extends BasicResponse> insertPost(@RequestBody PostsDTO dto) {

        PostsDTO rDto = service.savePost(dto);
        if (rDto == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("등록 실패", "500"));
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * 조회(Read)
     *
     * @param id
     * @return
     */
    @GetMapping("/posts/{id}")
    public ResponseEntity<? extends BasicResponse> getPost(@PathVariable Long id) {

        PostsDTO dto = service.getPost(id);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("일치하는 정보가 없습니다. id를 확인해주세요."));
        }
        return ResponseEntity.ok().body(new PostsResponse(dto));
    }

    /**
     * 수정(Update)
     *
     * @param dto
     * @return
     */
    @PatchMapping("/posts/{id}")
    public ResponseEntity<? extends BasicResponse> patchPost(@PathVariable Long id,
                                                             @RequestBody PostsDTO dto) {
        dto.setId(id);
        PostsDTO rDto = service.savePost(dto);
        if (rDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("일치하는 정보가 없습니다. id를 확인해주세요."));
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * 삭제(Delete)
     *
     * @param id
     * @return
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<? extends BasicResponse> deletePost(@PathVariable Long id) {

        if (!service.deletePost(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("일치하는 정보가 없습니다. id를 확인해주세요."));
        }
        return ResponseEntity.noContent().build();
    }

}
