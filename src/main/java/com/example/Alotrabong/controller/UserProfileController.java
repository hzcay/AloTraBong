package com.example.Alotrabong.controller;

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

        if (!model.containsAttribute("form")) {
            UserProfileFormDTO form = new UserProfileFormDTO(
                    profile.getFullName(),
                    profile.getEmail(),
                    profile.getAddress(),
                    profile.getPhone()
            );
            model.addAttribute("form", form);
        }

        model.addAttribute("profile", profile);
        // ĐƯỜNG DẪN VIEW ĐÚNG VỚI FILE: templates/user/profile/profile.html
        return "user/profile/profile";
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
}
