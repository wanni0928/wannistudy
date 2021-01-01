package com.wannistudio.settings;

import com.wannistudio.settings.form.NicknameForm;
import com.wannistudio.settings.form.Notifications;
import com.wannistudio.settings.form.PasswordForm;
import com.wannistudio.domain.Account;
import com.wannistudio.domain.CurrentUser;
import com.wannistudio.service.AccountService;
import com.wannistudio.settings.form.Profile;
import com.wannistudio.settings.validator.NicknameValidator;
import com.wannistudio.settings.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    static final String SETTING_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTING_PROFILE_VIEW_URL = "/settings/profile";

    static final String SETTING_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTING_PASSWORD_VIEW_URL = "/settings/password";

    static final String SETTING_NOTIFICATION_VIEW_NAME = "settings/notifications";
    static final String SETTING_NOTIFICATION_URL = "/settings/notifications";

    static final String SETTING_ACCOUNT_VIEW_NAME = "settings/account";
    static final String SETTING_ACCOUNT_URL = "/settings/account";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
//    private final PasswordFormValidator passwordFormValidator;
    private final NicknameValidator nicknameValidator;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nickNameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(SETTING_PROFILE_VIEW_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTING_PROFILE_VIEW_NAME;
    }

    @PostMapping(SETTING_PROFILE_VIEW_URL)
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTING_PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + SETTING_PROFILE_VIEW_URL;
    }

    @GetMapping(SETTING_PASSWORD_VIEW_URL)
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTING_PASSWORD_VIEW_NAME;
    }

    @PostMapping(SETTING_PASSWORD_VIEW_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTING_PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:" + SETTING_PASSWORD_VIEW_URL;
    }

    @GetMapping(SETTING_NOTIFICATION_URL)
    public String updateNotificationForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTING_NOTIFICATION_VIEW_NAME;
    }

    @PostMapping(SETTING_NOTIFICATION_URL)
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute(account);
            return SETTING_NOTIFICATION_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알람 설정을 변경했습니다.");
        return "redirect:" + SETTING_NOTIFICATION_URL;
    }

    @GetMapping(SETTING_ACCOUNT_URL)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTING_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTING_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account, @Valid NicknameForm nickNameForm, Errors errors, Model model, RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            System.out.println(errors.getAllErrors());
            model.addAttribute(account);
            return SETTING_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickName(account, nickNameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:" + SETTING_ACCOUNT_URL;
    }
}
