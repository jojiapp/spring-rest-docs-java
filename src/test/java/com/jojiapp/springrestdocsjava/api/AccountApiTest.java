package com.jojiapp.springrestdocsjava.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojiapp.springrestdocsjava.account.api.AccountApi;
import com.jojiapp.springrestdocsjava.account.dto.request.AccountRegister;
import com.jojiapp.springrestdocsjava.account.dto.request.AccountUpdate;
import com.jojiapp.springrestdocsjava.account.dto.response.AccountResponse;
import com.jojiapp.springrestdocsjava.account.service.AccountService;
import com.jojiapp.springrestdocsjava.common.respones.ApiResponse;
import com.jojiapp.springrestdocsjava.common.respones.SuccessResponse;
import com.jojiapp.springrestdocsjava.config.SpringRestDocsConfig;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void 계정을_성공적으로_생성하면_201상태를_받는다() throws Exception {
        // given
        var api = "/api/accounts";
        var apiRequest = new AccountRegister("jojiapp", 26);
        var apiResponse = ApiResponse.of(SuccessResponse.create());

        // when
        var result = mockMvc.perform(post(api)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apiRequest))
        );

        // then
        result.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(apiResponse)))
                .andDo(document("account/register",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 Body 타입"),
                                headerWithName(HttpHeaders.ACCEPT).description("기대 응답 Body 타입")
                        ),
                        requestFields(
                                fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
                                fieldWithPath("age").description("나이").type(JsonFieldType.NUMBER)
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 Body 타입")
                        ),
                        responseFields(
                                fieldWithPath("body.success").description("성공 여부").type(JsonFieldType.BOOLEAN)
                        ))
                );
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
                .andDo(document("account/find-all",
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


    @Test
    void 계정을_성공적으로_수정하면_204상태를_받는다() throws Exception {
        // given
        var api = "/api/accounts/{id}";
        var apiRequest = new AccountUpdate("joji", 25);

        // when
        var result = mockMvc.perform(put(api, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apiRequest))
        );

        // then
        result.andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("account/update-all",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 Body 타입")
                        ),
                        pathParameters(
                                parameterWithName("id").description("계정 고유 아이디").attributes(typeAttr(JsonFieldType.NUMBER))
                        ),
                        requestFields(
                                fieldWithPath("name").description("이름").type(JsonFieldType.STRING),
                                fieldWithPath("age").description("나이").type(JsonFieldType.NUMBER)
                        )
                ));
    }


}