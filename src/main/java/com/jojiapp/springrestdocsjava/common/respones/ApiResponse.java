package com.jojiapp.springrestdocsjava.common.respones;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private ApiResponse(T body) {
        this.body = body;
    }

    public static <T> ApiResponse<T> of(T body) {
        return new ApiResponse<>(body);
    }

    private T body;
}
