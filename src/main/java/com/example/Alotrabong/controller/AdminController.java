package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ChangePasswordFormDTO;
import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.dto.UserProfileFormDTO;
import com.example.Alotrabong.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("title", "User Management");
        return "admin/users";
    }

    @GetMapping("/menu")
    public String menuManagement(Model model) {
        model.addAttribute("title", "Menu Management");
        return "admin/menu";
    }

    @GetMapping("/branches")
    public String branchManagement(Model model) {
        model.addAttribute("title", "Branch Management");
        return "admin/branches";
    }

    @GetMapping("/branch-commissions")
    public String branchCommissions(Model model) {
        model.addAttribute("title", "Branch Commissions Management");
        return "admin/branch-commissions";
    }

    @GetMapping("/revenue-reports")
    public String revenueReports(Model model) {
        model.addAttribute("title", "Revenue Reports");
        return "admin/revenue-reports";
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        model.addAttribute("title", "Settings");
        try {
            // Load current user profile for admin settings
            if (authentication != null && authentication.getName() != null) {
                UserDTO profile = userService.getUserByEmail(authentication.getName());
                model.addAttribute("profile", profile);
            }
        } catch (Exception e) {
            // Ignore if profile loading fails
        }
        return "admin/settings";
    }

    @PostMapping("/settings/profile/update")
    public String updateProfile(@Valid UserProfileFormDTO form,
                                BindingResult br,
                                RedirectAttributes ra,
                                Authentication authentication) {
        try {
            if (authentication != null && authentication.getName() != null) {
                UserDTO profile = userService.getUserByEmail(authentication.getName());
                userService.updateProfile(profile.getUserId(), form);
                ra.addFlashAttribute("saved", "Cập nhật hồ sơ thành công.");
            }
        } catch (com.example.Alotrabong.exception.BadRequestException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật. Thử lại sau.");
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/settings/profile/change-password")
    public String changePassword(@Valid ChangePasswordFormDTO pwdForm,
                                 BindingResult br,
                                 RedirectAttributes ra,
                                 Authentication authentication) {
        try {
            if (authentication != null && authentication.getName() != null) {
                UserDTO profile = userService.getUserByEmail(authentication.getName());
                if (!pwdForm.getNewPassword().equals(pwdForm.getConfirmPassword())) {
                    ra.addFlashAttribute("pwdError", "Xác nhận mật khẩu không khớp.");
                    return "redirect:/admin/settings";
                }
                userService.changePassword(profile.getUserId(), pwdForm.getCurrentPassword(), pwdForm.getNewPassword());
                ra.addFlashAttribute("pwdSaved", "Đổi mật khẩu thành công.");
            }
        } catch (com.example.Alotrabong.exception.BadRequestException e) {
            ra.addFlashAttribute("pwdError", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("pwdError", "Không thể đổi mật khẩu lúc này. Thử lại sau.");
        }
        return "redirect:/admin/settings";
    }

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("title", "Promotion Management");
        return "admin/promotions";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {
        model.addAttribute("title", "Shipping Rates Management");
        return "admin/shipping";
    }
}
