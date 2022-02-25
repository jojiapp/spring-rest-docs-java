package com.jojiapp.springrestdocsjava.account.api;

import com.jojiapp.springrestdocsjava.account.dto.request.AccountRegister;
import com.jojiapp.springrestdocsjava.account.service.AccountService;
import com.jojiapp.springrestdocsjava.common.respones.ApiResponse;
import com.jojiapp.springrestdocsjava.common.respones.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountApi {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SuccessResponse> save(@RequestBody AccountRegister request) {
        accountService.register(request);
        return ApiResponse.of(SuccessResponse.create());
    }
}
