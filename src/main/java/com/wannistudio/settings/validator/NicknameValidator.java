package com.wannistudio.settings.validator;

import com.wannistudio.domain.Account;
import com.wannistudio.repository.AccountRepository;
import com.wannistudio.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {
    private final AccountRepository accountRepository;


    @Override
    public boolean supports(Class<?> aClass) {
        return NicknameForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nickNameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nickNameForm.getNickname());
        if(byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.");
        }
    }
}
