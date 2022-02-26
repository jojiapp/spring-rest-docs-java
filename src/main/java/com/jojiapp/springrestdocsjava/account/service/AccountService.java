package com.jojiapp.springrestdocsjava.account.service;

import com.jojiapp.springrestdocsjava.account.dto.request.AccountRegister;
import com.jojiapp.springrestdocsjava.account.dto.response.AccountResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    public void register(AccountRegister request) {
        // 계정 저장 로직
    }

    public List<AccountResponse> findAll(Pageable pageable) {
        return List.of();

    }
}
