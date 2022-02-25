package com.jojiapp.springrestdocsjava.common.respones;

import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {

    private ApiResponse(T body) {
        this.body = body;
    }

    public static <T> ApiResponse<T> of(T body) {
        return new ApiResponse<>(body);
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createDate = LocalDateTime.now();
    private T body;
}
