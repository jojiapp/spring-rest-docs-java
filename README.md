# [Spring] Spring Rest Docs

> `API`를 개발하게 되면 해당 `API`의 요청 및 응답 값들에 대한 스펙 정의를 해서 문서화를 꼭 해야합니다.  
> 이렇게 문서화를 해야 `Client`에서 해당 `API`가 어떤 기능을 하는지 이해하고 사용할 수 있기 때문입니다.  
> 하지만, 문서화는 굉장히 반복적이고 귀찮은 일입니다. (프로그래머는 참지 않아)
>
> `Spring Rest Docs`는 이런 문서화를 테스트 코드를 작성하면 자동으로 만들어 주는 `Spring 공식 문서자동화 라이브러리` 입니다.

## Gradle 설정

`Spring Rest Docs`를 사용하기 위해서는 의존성을 추가한 뒤, `build`시 처리할 로직을 추가로 작성해야합니다.

### 전체 코드

```groovy
plugins {
    id 'org.springframework.boot' version '2.6.3'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'org.asciidoctor.jvm.convert' version '3.3.2' // 추가
    id 'java'
}

group = 'com.jojiapp'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
    asciidoctorExtensions // 추가
}

repositories {
    mavenCentral()
}

ext {
    set('snippetsDir', file('build/generated-snippets'))
    set('documentDir', file('src/main/resources/static/docs')) // 추가
    set('bootJarDocumentPath', 'static/docs') // 추가
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-hateoas'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    annotationProcessor 'org.projectlombok:lombok'
    asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor' // 추가
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc' // 추가
}

jar {
    enabled false
}

clean {
    delete documentDir
}

test {
    outputs.dir snippetsDir
    useJUnitPlatform()
}

asciidoctor {
    doFirst {
        delete documentDir
    }

    configurations('asciidoctorExtensions')
    inputs.dir snippetsDir
    dependsOn test
}

bootJar {
    dependsOn asciidoctor
    from("${asciidoctor.outputDir}") {
        into bootJarDocumentPath
    }
}

task copyDocument(type: Copy) {
    dependsOn asciidoctor
    from file("${asciidoctor.outputDir}")
    into documentDir
}

build {
    dependsOn copyDocument
}
```

- `Gradle7.x` 버전 부터 `org.asciidoctor.convert` 플러그인은 사용할 수 없는 관계로 `org.asciidoctor.jvm.convert` 라이브러리를 사용합니다.

### Configurations

```groovy
configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
    asciidoctorExtensions
}
```

- `extendsFrom annotationProcessor ~`: `lombok` 같은 라이브러리를 사용하기 위함 입니다.
- `asciidoctorExtensions`: 추후 `operation`과 `{snippets}`을 사용하기 위한 설정 과정입니다.

### Ext

```groovy
ext {
    set('snippetsDir', file('build/generated-snippets')) // 추가
    set('documentDir', file('src/main/resources/static/docs')) // 추가
    set('bootJarDocumentPath', 'static/docs') // 추가
}
```

- `snippetsDir`: `Snippets`이 생성 될 위치 입니다.
- `documentDir`': 문서가 복사 될 위치 입니다.
    - 로컬에서 문서를 확인하기 위한 용도
- `bootJarDocumentPath`: `jar`파일로 패키징 시 문서를 복사할 위치 입니다.
    - 배포 시, 문서를 볼 수 있도록 하기 위한 용도

> `Snippets`은 테스트 코드를 통해 생성된 문서 조각입니다. `Snippets`을 조합해 문서를 만들 수 있습니다.

### Dependencies

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-hateoas'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    annotationProcessor 'org.projectlombok:lombok'
    asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor' // 추가
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc' // 추가
}
```

- `annotationProcessor`: `lombok`을 사용하기 위해 추가합니다.
- `asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor'`
    - `configurations` 설정과 한 묶음으로, 해당 라이브러리를 추가해야 `operation`과 `{snippets}`을 사용할 수 있습니다.
- `testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'`
    - 테스트 코드를 작성하면 문서 조각을 생성해시켜주는 라이브러리입니다.
- `implementation 'org.springframework.boot:spring-boot-starter-hateoas'`
    - `HATEOAS`도 간단하게 적용해보기 위해 추가하였습니다.

### Tasks

#### Jar

```groovy
jar {
    enabled false
}
```

- `enabled true`로 설정(기본 값)하면 `plain jar`파일을 만들어 줍니다. 현재는 필요 없으니 `false`로 설정 하였습니다.

#### Test

```groovy
test {
    outputs.dir snippetsDir
    useJUnitPlatform()
}
```

- `Test`가 완료되면 `Snippets`이 생기는데, 해당 `Snippets`이 생성될 위치를 지정합니다,

#### Asciidoctor

```groovy
asciidoctor {
    doFirst {
        delete documentDir
    }

    configurations('asciidoctorExtensions')
    inputs.dir snippetsDir
    dependsOn test
}
```

- `doFirst`: 생성된 문서를 해당 위치에 복사하고 있기 때문에(`아래 Task`에서 할 예정) `asciidoctor`이 실행 되면 먼저 해당 위치의 파일을 삭제합니다.
    - 최신 문서로 유지하기 위함입니다.
- `configurations`에 위에서 설정한 `asciidoctorExtensions`를 넣어줍니다.
    - 이렇게 함으로써, `Asciidoc`파일에서 `operation`과 `{snippets}`을 사용할 수 있게 됩니다.
- `inputs.dir snippetsDir`: `Snippets` 위치를 지정합니다.
- `dependsOn test`: 해당 `Task`실행 이전에 `test`를 먼저 실행합니다.

#### BootJar

```groovy
bootJar {
    dependsOn asciidoctor
    from("${asciidoctor.outputDir}") {
        into bootJarDocumentPath
    }
}
```

- `bootJar`는 실행 가능한 `jar`파일입니다.
- `from ~ into`: 해당 파일이 생성 되는 시점에 우리가 만든 문서를 `static/docs`위치에 복사하여 줍니다.
    - 복사하지 않으면 배포 시 문서를 볼 수 없습니다.
- `dependsOn asciidoctor`: `asciidoctor`가 실행되어야 문서가 존재하므로 `asciidoctor` 뒤에 실행되도록 합니다.

> 여러 블로그들을 보면 복사 경로가 `BOOT-INF/classes/static/docs`라고 되어있는데, 해당 경로가 아닌 `static/docs`로 해줘야합니다.

#### CopyDocument(Custom)

```groovy
task copyDocument(type: Copy) {
    dependsOn asciidoctor
    from file("${asciidoctor.outputDir}")
    into documentDir
}
```

- `copyDocument`: `bootJar`를 통해 배포 시에는 문서가 포함되도록 하였습니다만, `IntelliJ`같은 `Tool`에서 실행하면 존재하지 않는 경로(`404`)가 뜹니다.  
  그렇기 때문에 `src/main/resources/static/docs`에 문서를 복사하여 볼 수 있도록 만들어 줍니다.

#### Build

```groovy
build {
    dependsOn copyDocument
}
```

- `build`되기 이전에 `src/main/resources/static/docs`에 복사 되도록 설정합니다.

#### Clean

```groovy
clean {
    delete documentDir
}
```

- `clean`을 실행하면 `src/main/resources/static/docs` 폴더를 삭제 함으로써, `build`시 깔끔히 새로운 문서를 볼 수 있습니다.

## 문서화에 사용되는 메소드

> `타입`과 `필수값`은 `Custom`을 통해 만들수 있습니다. (`type`은 기본적용인 곳도 있음)  
> `Custom`하는 방법에 대해서는 다음장에서 자세하게 다루겠습니다.

`typeAttr` `defaultAttr` 두 메소드는 제가 만든 메소드 입니다. 아래 예시 코드에 있습니다.

### Document

- `MockMvcRestDocumentation.document`: 문서 작성은 해당 메소드 안에서 이루어줘야 합니다.
    - 첫 번째 인자로는 `snippets`이 생성될 폴더 경로를 지정해 줍니다.
    - 두 번째 인자부터 각 `요청` 및 `응답`에 대해 정의합니다. (`아래 메소드 참조`)

```java
result.andDo(document("accounts/find-all",...)
```

### Request Headers

- `HeaderDocumentation.requestHeader`: `Request Headers`를 정의합니다.
    - `HeaderDocumentation.headerWithName`: 각 `Header`에 대한 정의를 합니다.

```java
requestHeaders(
        headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 Body 타입"),
        headerWithName(HttpHeaders.ACCEPT).description("기대 응답 Body 타입")
        )
```

![request-headers 예시](./screenshot/request-headers.png)

### Path Parameters

- `RequestDocumentation pathParameters`': `Path Parameters`를 정의합니다.
    - `RequestDocumentation.parameterWithName`: 각 `Path Parameter`에 대해 정의합니다.

![path-parameters 예시](./screenshot/path-parameters.png)

### Request Parameters

- `RequestDocumentation.requestParameters`: `Request Parameters`를 정의합니다.
    - `RequestDocumentation.parameterWithName`: 각 `Parameter`에 대해 정의합니다.

```java
requestParameters(
        parameterWithName("page").description("페이지 번호 (0부터 시작)").attributes(typeAttr(JsonFieldType.NUMBER)).optional(),
        parameterWithName("size").description("개수").attributes(typeAttr(JsonFieldType.NUMBER)).attributes(defaultAttr(20)).optional(),
        parameterWithName("sort").description("정렬 {fieldName,asc|desc}").attributes(typeAttr(JsonFieldType.STRING)).optional()
        ),
```

![request-parameters 예시](./screenshot/request-parameters.png)

### Request Fields

- `PayloadDocumentation.requestFields`: `Request Fields`를 정의합니다.
    - `PayloadDocumentation.fieldWithPath`: 각 `Field`에 대해 정의합니다.

```java
requestFields(
        fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
        fieldWithPath("age").description("나이").type(JsonFieldType.NUMBER)
        )
```

![request-fields 예시](./screenshot/request-fields.png)

### Response Headers

- `HeaderDocumentation.responseHeaders`: `Response Headers`를 정의합니다.
    - `HeaderDocumentation.headerWithName`: 각 `Response Header`를 정의합니다.

```java
responseHeaders(
        headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 Body 타입")
        ),
```

![response-headers 예시](./screenshot/response-headers.png)

### Response Fields

- `PayloadDocumentation.responseFields`: `Response Fields`를 정의합니다.
    - `PayloadDocumentation.fieldWithPath`: 각 `Field`를 정의합니다.

![response-fields 예시](./screenshot/response-fields.png)

### Links

`HATEOAS`를 적용 중이라면, `Links`에 대해서도 작성할 수 있습니다.

- `HypermediaDocumentation.links`: `Links`를 정의합니다.
    - `HypermediaDocumentation.linkWithRel`: 각 `Link`에 대해 정의합니다.

```java
links(
        linkWithRel("self").description("요청 API 링크"),
        linkWithRel("profile").description("요청 API 문서 링크")
        ),
```

![](./screenshot/links.png)

> `Links`의 경우 이렇게 정의 하더라도, `Response Fields` 부분에서 한 번 더 정의해야 합니다. (불편)

## Test 작성 예시

`Test`코드를 작성해야 해당 `Test 코드`를 기반으로 `snippets`을 생성해줍니다.

`Rest API`는 작성되어 있다고 가정하고 하겠습니다.

### Rest Docs Config

기본적으로 `Json 응답 값`이 한줄로 보이기 때문에, `Json` 객체로 보기 이쁘게 하는 설정을 해줍니다.

```java

@TestConfiguration
public class SpringRestDocsConfig {

    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        return (it) -> {
            it.operationPreprocessors()
                    .withRequestDefaults(Preprocessors.prettyPrint())
                    .withResponseDefaults(Preprocessors.prettyPrint());
        };
    }
}

```

```java

@WebMvcTest(AccountApi.class)
@AutoConfigureRestDocs
@Import(SpringRestDocsConfig.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AccountApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private Attributes.Attribute defaultAttr(int value) {
        return new Attributes.Attribute("defaults", value);
    }

    private Attributes.Attribute typeAttr(JsonFieldType type) {
        return new Attributes.Attribute("types", type);
    }

    @Test
    void 계정을_정상적으로_전체조회하면_200상태를_받는다() throws Exception {
        // given
        var api = "/api/accounts";

        int page = 1;
        int size = 5;

        var params = new LinkedMultiValueMap<String, String>();
        params.add("page", Integer.toString(page));
        params.add("size", Integer.toString(size));
        params.add("sort", "id,asc");

        var accountResponses = List.of(new AccountResponse(1L, "jojiapp", 26));
        var apiResponse = ApiResponse.of(accountResponses);

        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        given(accountService.findAll(pageable)).willReturn(accountResponses);

        // when
        var result = mockMvc.perform(get(api)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .params(params)
        );

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(apiResponse)))
                .andDo(document("accounts/find-all",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 Body 타입"),
                                headerWithName(HttpHeaders.ACCEPT).description("응답 Body 타입")
                        ),
                        requestParameters(
                                parameterWithName("page").description("페이지 번호 (0부터 시작)").attributes(typeAttr(JsonFieldType.NUMBER)).optional(),
                                parameterWithName("size").description("개수").attributes(typeAttr(JsonFieldType.NUMBER)).attributes(defaultAttr(20)).optional(),
                                parameterWithName("sort").description("정렬 {fieldName,asc|desc}").attributes(typeAttr(JsonFieldType.STRING)).optional()
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 Body 타입")
                        ),
                        responseFields(
                                fieldWithPath("body[0].id").description("계정 고유 아이디").type(JsonFieldType.NUMBER),
                                fieldWithPath("body[0].name").description("이름").type(JsonFieldType.STRING),
                                fieldWithPath("body[0].age").description("나이").type(JsonFieldType.NUMBER)
                        )
                ));
    }
}
```

- `@WebMvcTest(AccountApi.class)`: 해당 `Controller`만 간단하게 테스트 하기 위해서 `@WebMvcTest`를 사용했습니다.
- `@AutoConfigureRestDocs`: `Rest Docs`와 관련된 설정을 자동으로 해주는 어노테이션입니다. 추가하지 않을 시, 문서작성이 되지 않으므로 꼭 추가합니다.
- `@Import(SpringRestDocsConfig.class)`: 위에서 만든 설정파일을 추가합니다.
- `@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)`: 테스트 메소드명의 구분자 `_`를 공백으로 치환시켜 주는 어노테이션입니다.
- `MockMvc`: 가짜 요청을 보내기 위한 객체 입니다.
    - `mockMvc.perform()` 메소드를 통해 `API`요청을 보낼 수 있습니다.
    - `HTTP Method`는 `MockMvcRequestBuilders`클래스와 `RestDocumentationRequestBuilders`로 나뉘는데, 테스트 결과 둘 다 사용 가능합니다. (
      저는 `RestDocumentationRequestBuilders`를 사용했습니다.)

> 테스트를 실행 시켜보면 `build/generated-snippets/accounts/find-all` 위치에 `snippets`이 생기는것을 확인할 수 있습니다.

## Asciidoc 작성

이제 `Test`를 통해 `snippets`을 생성했으니, 해당 `snippets`을 가지고 어떻게 문서를 작성하는지 알아보겠습니다.

### 파일 생성

`Gradle`의 경우 문서화를 작업할 파일은 꼭 `src/docs/asciidoc` 하위에 `.adoc` 확장자로 만들어야 합니다. (`main`, `test` 폴더와 동일 레벨)

### Snippets 적용 방법

`snippets`은 `build/generated-snippets` 하위에 생성됩니다. 그렇기 때문에, 해당 `snippets`을 사용하려면 해당 경로를 지정해야 합니다.

`Gradle` 설정에서 아래와 같이 적용했다면 `snippets`와 `operation`을 사용할 수 있습니다.

```groovy
asciidoctor {
    ...
    configurations('asciidoctorExtensions')
    ...
}
```

> `{snippets}`은 `build/generated-snippets` 경로를 잡아줍니다.  
> `operation`은 생성된 `snippets`(위의 snippets와 다름)을 보다 편리하게 사용할 수 있도록 해줍니다.

#### include 사용

`build/generated-snippets` 폴더에서 원하는 파일 명을 작성해 주면 됩니다.

```asciidoc
=== Curl Request

include::{snippets}/index/get/curl-request.adoc[]

=== HTTP Request

include::{snippets}/index/get/http-request.adoc[]

=== Response Body

include::{snippets}/index/get/response-body.adoc[]

=== Response Fields

include::{snippets}/index/get/response-fields.adoc[]
```

- 장점은 각 `snippets`에 대해 제목을 적을수 있습니다.
- 단점은 모든 `snippets`에 대해 다 적어줘야 합니다.

#### operation 사용

```asciidoc
operation::index/get[]
```

위와 같이 작성 시, 해당 폴더 아래에 있는 모든 `snippets`을 모두 문서에 추가합니다.  
문제는 불필요한 `snippets`까지 모두 추가 됩니다.

```asciidoc
operation::index/get[snippets='curl-request,http-request,response-body,response-fields']
```

위와 같이 작성 시, 원하는 `snippets`만 문서에 추가할 수 있습니다.

> `operation`의 단점은 제목을 직접 지정할 수 없다는 것입니다. 하지만, `templates`을 `Custom`하여 기본 설정 값을 변경할 수 있습니다.

### Templates Custom 방법

`optional`이나 `default`와 같은 값들은 기본적으로는 문서에 나타나지 않습니다. 해당 값들이 문서에 나타나게 하기 위해서는 필드값을 `Custom`해야 합니다.

#### Custom 파일 생성

`Custom` 하기 위한 파일은 꼭 `src/test/resources/org/springframework/restdocs/templates` 위치에 만들어야 합니다.

파일 명은 `snippets`이름과 동일하나 확장자는 `.snippet`로 만들어야 합니다.

```
links.snippet
path-parameters.snippet
request-fields.snippet
request-headers.snippet
request-parameters.snippet
response-fields.snippet
response.headers.snippet
```

#### 필드 값

```asciidoc
==== Request Parameters

|===
|필드명|설명|타입|기본값|필수값

{{#parameters}}
|{{#tableCellContent}}{{name}}{{/tableCellContent}}
|{{#tableCellContent}}{{description}}{{/tableCellContent}}
|{{#tableCellContent}}{{#types}}{{types}}{{/types}}{{/tableCellContent}}
|{{#tableCellContent}}{{#defaults}}{{defaults}}{{/defaults}}{{/tableCellContent}}
|{{#tableCellContent}}{{^optional}}true{{/optional}}{{/tableCellContent}}
{{/parameters}}
|===
```

- `|필드명|설명|타입|기본값|필수값`: 테이블 컬럼 제목
- `{{#parameters}}`: 변수는 `{{}}`안에 작성하며, 앞에 `#`을 붙여 사용합니다
    - `parameters`의 경우 각 파일마다 상이하며, 해당 값은 `Test`에서 작성했던 `requestParameters` 부분에서 뒷 부분의 값을 적으면 됩니다.
    - `request-fields`의 경우 `requestFields`이므로, `fields`를 넣으면 됩니다.
- `{{#tableCellContent}}`: 테이블 셀을 의미합니다.

```java
private Attributes.Attribute defaultAttr(int value){
        return new Attributes.Attribute("defaults",value);
        }

private Attributes.Attribute typeAttr(JsonFieldType type){
        return new Attributes.Attribute("types",type);
        }
```

```java
parameterWithName().description().attributes(typeAttr()).optional()
```

- `{{name}}`: `parameterWithName`처럼 맨 뒤의 값이 변수 명이 됩니다.
    - 변수 임에도 `#`이 없는 이유는 `parameters`로 감싸져있고, 해당 값은 `parameters`에 이미 내장 되어 있는 값이기 때문입니다.
- `{{description}}`: `name`과 동일
- `{{#types}}{{types}}{{/types}}`: `types` 이름은 제가 지은것입니다.
    - 해당 필드는 직접 만든것이기 때문에 바로 값을 적을 순 없고, 별도로 감싸서 처리해야 합니다.
- `{{#defaults}}{{defaults}}{{/defaults}}`: `types`와 동일
- `{{^optional}}true{{/optional}}`: `^`는 반대라는 의미로, `optional`이 없으면 `true`값이 나오게 됩니다.

### 문서 예시

#### 기본 설정

```asciidoc
= REST API Guide
프리라이프;
:doctype: book
:icons: font
:source-highlighter: highlightjs
:toc: left
:toclevels: 4
:sectlinks:
```

#### 개요

```asciidoc
[[overview]]
= 개요

[[overview-http-verbs]]
== HTTP 동사

본 REST API에서 사용하는 HTTP 동사(verbs)는 가능한한 표준 HTTP와 REST 규약을 따릅니다.

|===
| 동사 | 용례

| `GET`
| 리소스를 가져올 때 사용

| `POST`
| 새 리소스를 만들 때 사용

| `PUT`
| 기존 리소스를 수정할 때 사용

| `PATCH`
| 기존 리소스의 일부를 수정할 때 사용

| `DELETE`
| 기존 리소스를 삭제할 떄 사용
|===

[[overview-http-status-codes]]
== HTTP 상태 코드

본 REST API에서 사용하는 HTTP 상태 코드는 가능한한 표준 HTTP와 REST 규약을 따릅니다.

|===
| 상태 코드 | 용례

| `200 OK`
| 요청을 성공적으로 처리함

| `201 Created`
| 새 리소스를 성공적으로 생성함. 응답의 `Location` 헤더에 해당 리소스의 URI가 담겨있다.

| `204 No Content`
| 기존 리소스를 성공적으로 수정함.

| `400 Bad Request`
| 잘못된 요청을 보낸 경우. 응답 본문에 더 오류에 대한 정보가 담겨있다.

| `404 Not Found`
| 요청한 리소스가 없음.
|===

[[overview-hypermedia]]
== 하이퍼미디어

본 REST API는 하이퍼미디어와 사용하며 응답에 담겨있는 리소스는 다른 리소스에 대한 링크를 가지고 있다.
응답은 http://stateless.co/hal_specification.html[Hypertext Application from resource to resource. Language (HAL)] 형식을 따른다.
링크는 `_links`라는 키로 제공한다.
본 API의 사용자(클라이언트)는 URI를 직접 생성하지 않아야 하며, 리소스에서 제공하는 링크를 사용해야 한다.
```

#### 리소스

```asciidoc
[[resources]]
= 리소스

[[resources-index]]
== 인덱스

인덱스는 서비스 진입점을 제공한다.

[[resources-index-access]]
=== 인덱스 조회

`GET` 요청을 사용하여 인덱스에 접근할 수 있다.

operation::index[snippets='response-body,http-response,links']

[[resources-events]]
== 이벤트

이벤트 리소스는 이벤트를 만들거나 조회할 때 사용한다.

[[resources-events-list]]
=== 이벤트 목록 조회

`GET` 요청을 사용하여 서비스의 모든 이벤트를 조회할 수 있다.

operation::get-events[snippets='response-fields,curl-request,http-response,links']

[[resources-events-create]]
=== 이벤트 생성

`POST` 요청을 사용해서 새 이벤트를 만들 수 있다.

operation::create-event[snippets='request-fields,curl-request,http-request,request-headers,http-response,response-headers,response-fields,links']

[[resources-events-get]]
=== 이벤트 조회

`Get` 요청을 사용해서 기존 이벤트 하나를 조회할 수 있다.

operation::get-event[snippets='request-fields,curl-request,http-response,links']

[[resources-events-update]]
=== 이벤트 수정

`PUT` 요청을 사용해서 기존 이벤트를 수정할 수 있다.

operation::update-event[snippets='request-fields,curl-request,http-response,links']
```

---

## 참고 사이트

- [Spring Rest Docs 공식 사이트](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/)
- [[Spring] Spring rest docs 적용기(gradle 7.0.2)](https://velog.io/@max9106/Spring-Spring-rest-docs%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%AC%B8%EC%84%9C%ED%99%94)
- [Configure a Gradle 7 compatible version of Asciidoctor's Gradle plugin in projects using REST Docs #676](https://github.com/spring-io/start.spring.io/issues/676)
- [Asciidoc 기본 사용법](https://narusas.github.io/2018/03/21/Asciidoc-basic.html)
- [[BE/2주차] Spring Rest Docs 적용기 (3)](https://velog.io/@hydroniumion/BE2%EC%A3%BC%EC%B0%A8-Spring-Rest-Docs-%EC%A0%81%EC%9A%A9%EA%B8%B0-3#%EC%9E%90%EC%A3%BC-%EC%93%B0%EC%9D%B4%EB%8A%94-%ED%8F%AC%EB%A7%B7-%EC%98%88%EC%8B%9C%EC%97%B4%EC%96%B4%EC%84%9C-%EC%9D%BD%EC%96%B4%EB%B3%B4%EC%84%B8%EC%9A%94)
- [[스프링 기반 REST API 개발] 3-6. 스프링 REST Docs: 문서 빌드](https://freedeveloper.tistory.com/197)
- [REST API Guide](https://github.com/freespringlecture/spring-rest-api-study/blob/chap03-06_rest-docs-build/src/main/asciidoc/index.adoc)
- [Spring Rest Docs 적용해보기](https://velog.io/@tmdgh0221/Spring-Rest-Docs-%EC%A0%81%EC%9A%A9%ED%95%B4%EB%B3%B4%EA%B8%B0)
- [Mustache 템플릿 문법](https://taegon.kim/archives/4910)