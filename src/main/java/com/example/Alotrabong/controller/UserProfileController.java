package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ChangePasswordFormDTO;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.dto.UserProfileFormDTO;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserService userService;

    private String getCurrentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Bạn chưa đăng nhập.");
        }
        String email = auth.getName();
        UserDTO me = userService.getUserByEmail(email);
        if (me == null || me.getUserId() == null) {
            throw new ResourceNotFoundException("Không tìm thấy tài khoản.");
        }
        return me.getUserId();
    }

    @GetMapping("/user/profile")
    public String viewProfile(Model model) {
        String userId = getCurrentUserIdOrThrow();

        UserDTO profile = userService.getProfile(userId);

        model.addAttribute("userName", profile.getFullName());
        // form hồ sơ (không có address)
        if (!model.containsAttribute("form")) {
            UserProfileFormDTO form = new UserProfileFormDTO(
                    profile.getFullName(),
                    profile.getEmail(),
                    profile.getPhone()
            );
            model.addAttribute("form", form);
        }

        // form đổi mật khẩu
        if (!model.containsAttribute("pwdForm")) {
            model.addAttribute("pwdForm", new ChangePasswordFormDTO());
        }

        model.addAttribute("profile", profile);
        return "user/profile/profile"; // templates/user/profile/profile.html
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@Valid UserProfileFormDTO form,
                                BindingResult br,
                                RedirectAttributes ra) {
        String userId = getCurrentUserIdOrThrow();

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", br);
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("error", "Vui lòng kiểm tra lại các trường nhập.");
            return "redirect:/user/profile";
        }

        try {
            // Service ko còn field address
            userService.updateProfile(userId, form);
            ra.addFlashAttribute("saved", "Cập nhật hồ sơ thành công.");
        } catch (BadRequestException e) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Update profile error", e);
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật. Thử lại sau.");
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/change-password")
    public String changePassword(@Valid ChangePasswordFormDTO pwdForm,
                                 BindingResult br,
                                 RedirectAttributes ra) {
        String userId = getCurrentUserIdOrThrow();

        // giữ lại dữ liệu form đổi mật khẩu khi fail
        ra.addFlashAttribute("pwdForm", pwdForm);

        if (br.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pwdForm", br);
            ra.addFlashAttribute("pwdError", "Dữ liệu chưa hợp lệ.");
            return "redirect:/user/profile";
        }

        if (!pwdForm.getNewPassword().equals(pwdForm.getConfirmPassword())) {
            ra.addFlashAttribute("pwdError", "Xác nhận mật khẩu không khớp.");
            return "redirect:/user/profile";
        }

        try {
            userService.changePassword(userId, pwdForm.getCurrentPassword(), pwdForm.getNewPassword());
            ra.addFlashAttribute("pwdSaved", "Đổi mật khẩu thành công.");
        } catch (BadRequestException e) {
            ra.addFlashAttribute("pwdError", e.getMessage()); // ví dụ: mật khẩu hiện tại sai
        } catch (Exception e) {
            log.error("Change password error", e);
            ra.addFlashAttribute("pwdError", "Không thể đổi mật khẩu lúc này. Thử lại sau.");
        }
        return "redirect:/user/profile";
    }
}
