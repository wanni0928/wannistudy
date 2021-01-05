package com.wannistudio.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wannistudio.WithAccount;
import com.wannistudio.controller.form.ZoneForm;
import com.wannistudio.domain.Account;
import com.wannistudio.domain.Tag;
import com.wannistudio.domain.Zone;
import com.wannistudio.repository.AccountRepository;
import com.wannistudio.repository.TagRepository;
import com.wannistudio.repository.ZoneRepository;
import com.wannistudio.service.AccountService;
import org.aspectj.lang.annotation.After;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static com.wannistudio.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @WithAccount("wanni")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("keesun")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account keesun = accountRepository.findByNickname("keesun");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(keesun.getZones().contains(zone));
    }

    @WithAccount("keesun")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void removeZone() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(keesun, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(keesun.getZones().contains(zone));
    }

    @WithAccount("wanni")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTING_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTING_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"))
                ;
    }

    @WithAccount("wanni")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTING_TAGS_URL + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(tagForm))
            .with(csrf())
        ).andExpect(status().isOk())
        ;
        Tag tag = tagRepository.findByTitle("newTag");
        assertNotNull(tag);
        Tag newTag = tagRepository.findByTitle("newTag");
        assertTrue(accountRepository.findByNickname("wanni").getTags().contains(newTag));
    }

    @WithAccount("wanni")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account wanni = accountRepository.findByNickname("wanni");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(wanni, newTag);

        assertTrue(wanni.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTING_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagForm))
                .with(csrf())
        ).andExpect(status().isOk())
        ;

        assertFalse(wanni.getTags().contains(newTag));
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
        String bio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account wanni = accountRepository.findByNickname("wanni");
        assertEquals(bio, wanni.getBio());
    }

    @WithAccount("wanni")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 너무나도 길게 소개를 수정하는 경우. ";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account wanni = accountRepository.findByNickname("wanni");
        assertNull(wanni.getBio());
    }

    @WithAccount("wanni")
    @DisplayName("비밀번호 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(SettingsController.SETTING_PASSWORD_VIEW_URL)
        ).andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
        ;
    }

    @WithAccount("wanni")
    @DisplayName("비밀번호 수정하기 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.SETTING_PASSWORD_VIEW_URL)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm","12345678")
                .with(csrf())
        ).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(SettingsController.SETTING_PASSWORD_VIEW_URL))
        .andExpect(flash().attributeExists("message"))
        ;
    }

    @WithAccount("wanni")
    @DisplayName("비밀번호 수정하기 - 입력값 오류")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(SettingsController.SETTING_PASSWORD_VIEW_URL)
                .param("newPassword", "123")
                .param("newPasswordConfirm", "12345678")
                .with(csrf())
        ).andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTING_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"))
        ;
    }
}