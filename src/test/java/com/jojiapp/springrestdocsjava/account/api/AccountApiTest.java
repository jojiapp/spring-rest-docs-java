package com.jojiapp.springrestdocsjava.account.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
    void ?????????_???????????????_????????????_201?????????_?????????() throws Exception {
        // given
        var api = "/api/accounts";
        var apiRequest = new AccountRegister("jojiapp", 26);
        var apiResponse = ApiResponse.of(SuccessResponse.success());

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
                .andDo(document("accounts/register",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("?????? Body ??????"),
                                headerWithName(HttpHeaders.ACCEPT).description("?????? ?????? Body ??????")
                        ),
                        requestFields(
                                fieldWithPath("name").description("??????").type(JsonFieldType.STRING),
                                fieldWithPath("age").description("??????").type(JsonFieldType.NUMBER)
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("?????? Body ??????")
                        ),
                        responseFields(
                                fieldWithPath("body.success").description("?????? ??????").type(JsonFieldType.BOOLEAN)
                        ))
                );
    }

    @Test
    void ?????????_???????????????_??????????????????_200?????????_?????????() throws Exception {
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
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("?????? Body ??????"),
                                headerWithName(HttpHeaders.ACCEPT).description("?????? Body ??????")
                        ),
                        requestParameters(
                                parameterWithName("page").description("????????? ?????? (0?????? ??????)").attributes(typeAttr(JsonFieldType.NUMBER)).optional(),
                                parameterWithName("size").description("??????").attributes(typeAttr(JsonFieldType.NUMBER)).attributes(defaultAttr(20)).optional(),
                                parameterWithName("sort").description("?????? {fieldName,asc|desc}").attributes(typeAttr(JsonFieldType.STRING)).optional()
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("?????? Body ??????")
                        ),
                        responseFields(
                                fieldWithPath("body[0].id").description("?????? ?????? ?????????").type(JsonFieldType.NUMBER),
                                fieldWithPath("body[0].name").description("??????").type(JsonFieldType.STRING),
                                fieldWithPath("body[0].age").description("??????").type(JsonFieldType.NUMBER)
                        )
                ));
    }


    @Test
    void ?????????_???????????????_????????????_204?????????_?????????() throws Exception {
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
                .andDo(document("accounts/update-all",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("?????? Body ??????")
                        ),
                        pathParameters(
                                parameterWithName("id").description("?????? ?????? ?????????").attributes(typeAttr(JsonFieldType.NUMBER))
                        ),
                        requestFields(
                                fieldWithPath("name").description("??????").type(JsonFieldType.STRING),
                                fieldWithPath("age").description("??????").type(JsonFieldType.NUMBER)
                        )
                ));
    }

    @Test
    void ?????????_???????????????_????????????_204?????????_?????????() throws Exception {
        // given
        var api = "/api/accounts/{id}";

        // when
        var result = mockMvc.perform(delete(api, 1));

        // then
        result.andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document("accounts/remove",
                        pathParameters(
                                parameterWithName("id").description("?????? ?????? ?????????").attributes(typeAttr(JsonFieldType.NUMBER))
                        )
                ));
    }
}