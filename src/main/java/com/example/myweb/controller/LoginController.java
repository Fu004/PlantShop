package com.example.myweb.controller;

import com.example.myweb.model.User;
import com.example.myweb.service.SupabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private SupabaseService supabaseService;

    // Hiển thị trang login
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        JsonNode user = supabaseService.login(username, password); // Gọi từ Supabase

        if (user != null) {

            User UserDefined = new User();
            UserDefined.setUserID((long) user.get("UserID").asInt());
            UserDefined.setUserName(user.get("UserName").asText());
            UserDefined.setUserPassword(user.get("UserPassword").asText());
            UserDefined.setIsSeller(user.get("IsSeller").asBoolean());

            session.setAttribute("UserDefined", UserDefined);
            session.setAttribute("userID", UserDefined.getUserID());


            session.setAttribute("loggedInUser", user); // lưu vào session
            boolean isSeller = user.get("IsSeller").asBoolean();

            // Lưu session (tùy chọn)
//            session.setAttribute("username", username);
//            session.setAttribute("userId", user.get("UserID").asText());
//            session.setAttribute("isSeller", isSeller);

            // Chuyển hướng theo quyền
            if (isSeller) {
                return "redirect:/seller"; // → sẽ trả về seller.html
            } else {
                return "redirect:/"; // → trang index.html
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Sai tài khoản hoặc mật khẩu!");
            redirectAttributes.addFlashAttribute("messageColor", "color:red");
            return "redirect:/login";
        }
    }
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xoá toàn bộ session
        return "redirect:/login"; // Chuyển về trang đăng nhập
    }
}
