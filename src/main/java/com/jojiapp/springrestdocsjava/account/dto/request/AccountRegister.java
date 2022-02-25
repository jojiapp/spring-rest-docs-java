package com.jojiapp.springrestdocsjava.account.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public class AccountRegister {
    private String name;
    private Integer age;

    @Builder(access = AccessLevel.PUBLIC)
    private AccountRegister(String name, Integer age) {
        Assert.hasText(name, "이름은 필수 값입니다.");
        Assert.notNull(age, "나이는 필수 값입니다.");
        this.name = name;
        this.age = age;
    }
}
