package com.wannistudio.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class AccountForm {
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;
}
