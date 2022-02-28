package com.jojiapp.springrestdocsjava.index.api;

import com.jojiapp.springrestdocsjava.common.respones.ApiResponse;
import com.jojiapp.springrestdocsjava.common.respones.SuccessResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class IndexApi {

    @GetMapping
    public EntityModel<ApiResponse<SuccessResponse>> index() {
        var apiResponse = ApiResponse.of(SuccessResponse.success());
        return EntityModel.of(apiResponse)
                .add(linkTo(methodOn(IndexApi.class).index()).withSelfRel())
                .add(linkTo(IndexApi.class).slash("docs/index.html#resources-index-get").withRel("profile"));
    }
}
