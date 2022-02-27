package com.jojiapp.springrestdocsjava.account.api;

import com.jojiapp.springrestdocsjava.account.dto.request.AccountRegister;
import com.jojiapp.springrestdocsjava.account.dto.request.AccountUpdate;
import com.jojiapp.springrestdocsjava.account.dto.response.AccountResponse;
import com.jojiapp.springrestdocsjava.account.service.AccountService;
import com.jojiapp.springrestdocsjava.common.respones.ApiResponse;
import com.jojiapp.springrestdocsjava.common.respones.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ApiResponse<List<AccountResponse>> findAll(Pageable pageable) {
        return ApiResponse.of(accountService.findAll(pageable));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateAllById(
            @PathVariable Long id,
            @RequestBody AccountUpdate request) {
        accountService.updateAllById(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeById(@PathVariable Long id) {
        accountService.removeById(id);
    }

}