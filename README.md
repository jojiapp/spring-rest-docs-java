# [Spring] Spring Rest Docs

> `API`를 개발하게 되면 해당 `API`의 요청 및 응답 값들에 대한 스펙 정의를 해서 문서화를 꼭 해야합니다.  
> 이렇게 문서화를 해야 `Client`에서 해당 `API`가 어떤 기능을 하는지 이해하고 사용할 수 있기 때문입니다.  
> 하지만, 문서화는 굉장히 반복적이고 귀찮은 일입니다. (프로그래머는 참지 않아)
>
> `Spring Rest Docs`는 이런 문서화를 테스트 코드를 작성하면 자동으로 만들어 주는 `Spring 공식 문서자동화 라이브러리` 입니다.

## Gradle 설정

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

---

## Test 코드 작성



## 참고 사이트

- [Spring Rest Docs 공식 사이트](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/)
- [[Spring] Spring rest docs 적용기(gradle 7.0.2)](https://velog.io/@max9106/Spring-Spring-rest-docs%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%AC%B8%EC%84%9C%ED%99%94)
- [Configure a Gradle 7 compatible version of Asciidoctor's Gradle plugin in projects using REST Docs #676](https://github.com/spring-io/start.spring.io/issues/676)