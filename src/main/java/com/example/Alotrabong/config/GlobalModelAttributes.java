package com.example.Alotrabong.config;

import com.example.Alotrabong.dto.UserDTO;
import com.example.Alotrabong.service.UserService;
import com.example.Alotrabong.service.CartService; // service của bạn
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Component
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserService userService;
    private final CartService cartService; // inject

    private static final String SESSION_BRANCH_KEY = "SELECTED_BRANCH_ID";

    @ModelAttribute
    public void injectHeader(Model model, Authentication auth, HttpSession session) {
        // default
        int cartCount = 0;

        if (auth != null && auth.isAuthenticated()
                && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            try {
                UserDTO me = userService.getUserByEmail(auth.getName());
                model.addAttribute("userName", me.getFullName());

                // Lấy branch (nếu có xài chọn chi nhánh)
                String branchIdOrCode = null;
                Object b = session.getAttribute(SESSION_BRANCH_KEY);
                if (b != null) branchIdOrCode = String.valueOf(b);

                // Đếm số lượng từ DB (login-only)
                cartCount = cartService.getCartItemCount(me.getUserId(), branchIdOrCode);
            } catch (Exception ignored) { /* đừng văng lỗi ra header */ }
        }

        model.addAttribute("cartCount", cartCount);
    }
}