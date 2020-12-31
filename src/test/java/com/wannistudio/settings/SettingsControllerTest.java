package com.wannistudio.settings;

import com.wannistudio.WithAccount;
import com.wannistudio.domain.Account;
import com.wannistudio.repository.AccountRepository;
import com.wannistudio.service.AccountService;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("wanni")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        String bio = "짧은 소개 수정하는 경우";
        mockMvc.perform(get(SettingsController.SETTING_PROFILE_VIEW_URL)
        ).andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
        ;
    }

    @WithAccount("wanni")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개 수정하는 경우";
        mockMvc.perform(post(SettingsController.SETTING_PROFILE_VIEW_URL)
                .param("bio", bio)
                .with(csrf())
       ).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTING_PROFILE_VIEW_URL))
                .andExpect(flash().attributeExists("message"));
        Account wanni = accountRepository.findByNickname("wanni");
        assertEquals(bio, wanni.getBio());
    }

    @WithAccount("wanni")
    @DisplayName("프로필 수정하기 - 입력값 오류")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우긴 소개 수정하는 경우";
        mockMvc.perform(post(SettingsController.SETTING_PROFILE_VIEW_URL)
                .param("bio", bio)
                .with(csrf())
        ).andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTING_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors())
        ;

        Account wanni = accountRepository.findByNickname("wanni");
        assertNull(wanni.getBio());
    }
}