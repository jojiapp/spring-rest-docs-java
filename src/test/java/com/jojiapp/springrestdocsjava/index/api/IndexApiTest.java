package com.jojiapp.springrestdocsjava.index.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojiapp.springrestdocsjava.config.SpringRestDocsConfig;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IndexApi.class)
@AutoConfigureRestDocs
@Import(SpringRestDocsConfig.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class IndexApiTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 인덱스_조회() throws Exception {
        // given
        var api = "/";

        // when
        var result = mockMvc.perform(get(api));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andDo(document("index/get",
                        links(
                                linkWithRel("self").description("요청 API 링크"),
                                linkWithRel("profile").description("요청 API 문서 링크")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("기대 응답 Body 타입")
                        ),
                        responseFields(
                                fieldWithPath("body.success").description("성공 여부").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("_links.self.href").description("요청 API 링크").type(JsonFieldType.STRING),
                                fieldWithPath("_links.profile.href").description("요청 API 문서 링크").type(JsonFieldType.STRING)
                        )
                ));
    }
}