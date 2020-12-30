package com.wannistudio.domain;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class UserAccount extends User {
    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), Stream.of(new SimpleGrantedAuthority("ROLE_USER")).collect(Collectors.toList()));
        System.out.println("qweqweqweqweqwe");
        this.account = account;
    }
}
