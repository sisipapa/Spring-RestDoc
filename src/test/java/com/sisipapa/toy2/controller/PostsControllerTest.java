package com.sisipapa.toy2.controller;

import com.google.gson.GsonBuilder;
import com.sisipapa.toy2.dto.PostsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles(value = "local")
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
class PostsControllerTest {

    private MockMvc mockMvc;

    private RestDocumentationResultHandler document;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.document = document(
                "{class-name}/{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())
        );
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentation).uris().withScheme("http").withHost("localhost").withPort(80))
                .alwaysDo(document)
                .build();
    }

    @Test
    public void insertPost() throws Exception {

        PostsDTO postDto = PostsDTO.builder()
                            .author("??????1")
                            .title("??????1")
                            .content("??????1").build();

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(postDto);

        mockMvc.perform(
                post("/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("author").description("??????"),
                                fieldWithPath("title").description("??????"),
                                fieldWithPath("content").description("??????")
                        )
                ));
    }

    @Test
    public void getPost() throws Exception {

        this.mockMvc.perform(get("/v1/posts/{id}", 1l)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(parameterWithName("id").description("Index ???")),
                        /*requestFields(
                                fieldWithPath("name").description("The name of the input")),*/
                        responseFields(
                                fieldWithPath("count").description("?????????"),
                                fieldWithPath("data.id").description("Post Id"),
                                fieldWithPath("data.author").description("??????"),
                                fieldWithPath("data.title").description("??????"),
                                fieldWithPath("data.content").description("??????"))));
    }

    @Test
    public void patchPost() throws Exception {


        PostsDTO postDto = PostsDTO.builder()
                .author("??????1-??????1")
                .title("??????1-??????1")
                .content("??????1-??????1").build();

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(postDto);

        mockMvc.perform(
                patch("/v1/posts/{id}", 1l)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id").description("Post Id")
                        ),
                        requestFields(
                                fieldWithPath("author").description("??????"),
                                fieldWithPath("title").description("??????"),
                                fieldWithPath("content").description("??????")
                        )
                ));
    }

    @Test
    public void deletePost() throws Exception {
        this.mockMvc.perform(delete("/v1/posts/{id}", 11L))
                .andExpect(status().isNoContent())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("id").description("Post Id")
                        )
                ));
    }

}