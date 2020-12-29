package com.wannistudio.controller;

import com.wannistudio.domain.Account;
import com.wannistudio.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "asdfasdfasdf")
                .param("email", "email@gmail.com")
        ).andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checkedEmail"))
                .andExpect(unauthenticated())
        ;
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken_with_right_input() throws Exception {
        Account account = Account.builder()
                .email("wanni@gmail.com")
                .password("123456789")
                .nickname("wanni")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail())
        ).andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickName"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checkedEmail"))
                .andExpect(authenticated())
        ;
    }

    @DisplayName("회원 가입 화면 보이는 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/signUp"))
                .andExpect(model().attributeExists("signUpForm"))
        ;
    }

    @DisplayName("회원 가입 처리 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "wanni")
                .param("email", "aaaaaa..com")
                .param("password", "12345")
                .with(csrf())
        ).andExpect(status().isOk())
                .andExpect(view().name("account/signUp"))
                .andExpect(unauthenticated())
        ;

    }

    @DisplayName("회원 가입 처리 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "wanni")
                .param("email", "wanni@gmail.com")
                .param("password", "1234567890")
                .with(csrf())
        ).andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated())
        ;
        assertTrue(accountRepository.existsByEmail("wanni@gmail.com"));
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("회원 가입 후 저장되는 암호는 인코딩 되어있다.")
    @Test
    void validateEncodedPassword() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "wanni")
                .param("email", "wanni@gmail.com")
                .param("password", "1234567890")
                .with(csrf())
        ).andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated())
        ;

        Account account = accountRepository.findByEmail("wanni@gmail.com");
        assertNotEquals("1234567890", account.getPassword());
        assertNotNull(account.getEmailCheckToken());
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}