# Springboot+Spring Data JPA+Querydsl+Spring RestDoc

오늘은 이전 블로그와 동일한 환경에 Document 라이브러리만 Spring RestDoc을 사용해서 샘플 프로젝트를 진행할 예정이다.  
개발 진행순서는 아래와 같다. Swagger Document 라이브러리 설치 때와 비교했을 때 설정 부분이 조금 더 많다.  

1. H2 DB설치
2. build.gradle 설정
3. Rest API 개발(POST,GET,PATCH,DELETE) - PostsController
4. Rest API 단위테스트 파일 개발 - PostsControllerTest
5. 단위테스트 실행
6. Gradle bootWar 실행
7. Spring RestDoc html 문서확인

## 1. H2 DB설치

- H2 database 다운로드 후 설치  
  http://www.h2database.com/html/main.html 페이지에서 OS맞는 설치파일을 다운로드 후 설치한다.
- 접속 확인
  http://localhost:8082/login.jsp?jsessionid=3ca85e5c51fb926f441f097c1a6d0434
- JDBC URL을 TCP 모드로 세팅후 연결 jdbc:h2:tcp://localhost/~/DB명

<img src="https://sisipapa.github.io/assets/images/posts/2021-04-02-h2.PNG" >   

## 2. build.gradle 설정 (//spring-restdoc 주석 부분 확인)
```yaml
plugins {
    id 'org.springframework.boot' version '2.4.4'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
    id 'war'
    id 'com.ewerk.gradle.plugins.querydsl' version '1.0.10' // querydsl 설정 추가
    id 'org.asciidoctor.convert' version '1.5.9.2'//spring-restdoc(1)
}

group = 'com.sisipapa'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

ext {//spring-restdoc(4)
    snippetsDir = file('build/generated-snippets')
}

test {//spring-restdoc(5)
    outputs.dir snippetsDir
    useJUnitPlatform()
}

asciidoctor {//spring-restdoc(6)
    inputs.dir snippetsDir
    dependsOn test
}

bootWar {
    dependsOn asciidoctor //spring-restdoc(7)
    from("${asciidoctor.outputDir}/html5") { //spring-restdoc(8)
        into 'static/docs'
    }
    archiveFileName = 'ROOT.war'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'                                      // Getter,Setter,Builder
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    runtimeOnly 'com.h2database:h2'
    implementation 'com.querydsl:querydsl-jpa' // querydsl 설정

    asciidoctor 'org.springframework.restdocs:spring-restdocs-asciidoctor' //spring-restdoc(2)
    testCompile 'org.springframework.restdocs:spring-restdocs-mockmvc' //spring-restdoc(3)

    compile 'com.google.code.gson:gson:2.8.5' // PostsControllerTest 단위테스트에서 사용
}

//querydsl 추가 시작
def querydslDir = 'build/generated/querydsl'

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets {
    main.java.srcDir querydslDir
}

configurations {
    querydsl.extendsFrom compileClasspath
}

compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}
//querydsl 추가 끝

test {
    useJUnitPlatform()
}

```

## 3. Rest API 개발(POST,GET,PATCH,DELETE)
Swagger 라이브러리와는 다르게 원본 소스에 별다른 Annotation 추가 설정을 하지 않는다.
```java
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

```

## 4. Rest API 단위테스트 파일 개발 - PostsControllerTest
```java
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
                            .author("저자1")
                            .title("제목1")
                            .content("내용1").build();

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
                                fieldWithPath("author").description("저자"),
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("content").description("내용")
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
                        pathParameters(parameterWithName("id").description("Index 키")),
                        /*requestFields(
                                fieldWithPath("name").description("The name of the input")),*/
                        responseFields(
                                fieldWithPath("count").description("카운트"),
                                fieldWithPath("data.id").description("Post Id"),
                                fieldWithPath("data.author").description("저자"),
                                fieldWithPath("data.title").description("제목"),
                                fieldWithPath("data.content").description("내용"))));
    }

    @Test
    public void patchPost() throws Exception {


        PostsDTO postDto = PostsDTO.builder()
                .author("저자1-수정1")
                .title("제목1-수정1")
                .content("내용1-수정1").build();

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
                                fieldWithPath("author").description("저자"),
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("content").description("내용")
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
```

## 5. 단위테스트 실행
API에 include할 snippet 생성이 생성된다. 단위테스트가 정상적으로 수행되고 나면 build.gradle에 설정한 아래 경로 하위에 adoc확장자의 snippet 파일들이 생성된다. 
```yaml
ext {//spring-restdoc(4)
  snippetsDir = file('build/generated-snippets')
}
```
<img src="https://sisipapa.github.io/assets/images/posts/2021-04-09-restdoc-snippet.PNG" >

## 6. API 템플릿 작성
src/docs/asciidoc/posts-docs.adoc 경로에 아래 API 템플릿 파일을 작성한다.  

```text
= API 명세(Spring Rest Docs)
:author: Kyunghun Kim
:email: sisipapa239@gmail.com
:source-highlighter: highlightjs
:toc: left
:toclevels: 4
:sectnums:
:sectlinks:
:operation-http-request-title: Request structure
:operation-http-response-title: Example response

== 등록[POST][/v1/posts]

=== Curl request
include::{snippets}/posts-controller-test/insert-post/curl-request.adoc[]

=== HTTP request
include::{snippets}/posts-controller-test/insert-post/http-request.adoc[]

=== HTTP response
include::{snippets}/posts-controller-test/insert-post/http-response.adoc[]

=== request fields
include::{snippets}/posts-controller-test/insert-post/request-fields.adoc[]

=== request body
include::{snippets}/posts-controller-test/insert-post/request-body.adoc[]


== 단건조회[GET][/v1/posts/{id}]

=== Curl request
include::{snippets}/posts-controller-test/get-post/curl-request.adoc[]

=== HTTP request
include::{snippets}/posts-controller-test/get-post/http-request.adoc[]

=== HTTP response
include::{snippets}/posts-controller-test/get-post/http-response.adoc[]

=== Path parameters
include::{snippets}/posts-controller-test/delete-post/path-parameters.adoc[]

=== request fields
include::{snippets}/posts-controller-test/get-post/request-fields.adoc[]

=== response body
include::{snippets}/posts-controller-test/get-post/response-body.adoc[]


== 수정[PATCH][/v1/posts/{id}]

=== Curl request
include::{snippets}/posts-controller-test/patch-post/curl-request.adoc[]

=== HTTP request
include::{snippets}/posts-controller-test/patch-post/http-request.adoc[]

=== HTTP response
include::{snippets}/posts-controller-test/patch-post/http-response.adoc[]

=== Path parameters
include::{snippets}/posts-controller-test/delete-post/path-parameters.adoc[]

=== request fields
include::{snippets}/posts-controller-test/patch-post/request-fields.adoc[]

=== request body
include::{snippets}/posts-controller-test/patch-post/request-body.adoc[]


== 삭제[DELETE][/v1/posts/{id}]

=== Curl request
include::{snippets}/posts-controller-test/delete-post/curl-request.adoc[]

=== HTTP request
include::{snippets}/posts-controller-test/delete-post/http-request.adoc[]

=== HTTP response
include::{snippets}/posts-controller-test/delete-post/http-response.adoc[]

=== Path parameters
include::{snippets}/posts-controller-test/delete-post/path-parameters.adoc[]
```

## 7. Gradle bootWar 실행
Gradle bootWar 실행 후 build/asciidoc/html5 경로에 posts-docs.html 파일이 생성된다.  
```yaml
bootWar {
    dependsOn asciidoctor //spring-restdoc(7)
    from("${asciidoctor.outputDir}/html5") { //spring-restdoc(8)
        into 'static/docs'
    }
    archiveFileName = 'ROOT.war'
}
```

## 8. Spring RestDoc html 문서확인  
아래는 생성된 posts-docs.html 파일이다.
<img src="https://sisipapa.github.io/assets/images/posts/2021-04-09-restdoc-result.PNG" >  

## https://sisipapa.github.io/blog/2021/04/09/Springboot-Document-%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC2(Spring-Restdoc)/



