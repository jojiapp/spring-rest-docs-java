package com.jojiapp.springrestdocsjava.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojiapp.springrestdocsjava.account.api.AccountApi;
import com.jojiapp.springrestdocsjava.account.dto.request.AccountRegister;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Test
    public void 계정_생성() throws Exception {
        // given
        String api = "/api/accounts";
        AccountRegister apiRequest = AccountRegister.builder()
                .name("jojiapp")
                .age(26)
                .build();

        ApiResponse<SuccessResponse> apiResponse = ApiResponse.of(SuccessResponse.create());

        // when
        mockMvc.perform(post(api)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiRequest))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document("account/register",
                        requestHeaders(
                                headerWithName("content-Type").description(MediaType.APPLICATION_JSON),
                                headerWithName("accept").description(MediaType.APPLICATION_JSON)
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                                fieldWithPath("age").type(JsonFieldType.NUMBER).description("나이")
                        ),
                        responseFields(
                                fieldWithPath("createDate").type(JsonFieldType.STRING).description("응답 시간"),
                                fieldWithPath("body.success").type(JsonFieldType.BOOLEAN).description("성공 여부")
                        ))
                );
    }
}