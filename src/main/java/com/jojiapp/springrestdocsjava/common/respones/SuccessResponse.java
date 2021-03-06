package com.jojiapp.springrestdocsjava.common.respones;

import lombok.Getter;

@Getter
public class SuccessResponse {
    private SuccessResponse() {}

    public static SuccessResponse success() {
        return new SuccessResponse();
    }

    private Boolean success = true;
}
