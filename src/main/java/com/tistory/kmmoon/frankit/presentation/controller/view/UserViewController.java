package com.tistory.kmmoon.frankit.presentation.controller.view;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserViewController {

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String login() {
        log.info("로그인 페이지 요청");
        return "auth/login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/register")
    public String register() {
        log.info("회원가입 페이지 요청");
        return "auth/register";
    }

    /**
     * 사용자 목록 페이지 (관리자용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/view/users")
    public String getUserList() {
        log.info("사용자 목록 페이지 요청");
        return "user/list";
    }

    /**
     * 사용자 상세 페이지
     */
    @GetMapping("/view/users/{userId}")
    public String getUserDetail(@PathVariable Long userId, Model model) {
        log.info("사용자 상세 페이지 요청, id: {}", userId);
        model.addAttribute("userId", userId);
        return "user/detail";
    }

    /**
     * 사용자 등록 페이지 (관리자용)
     */
    @GetMapping("/view/users/new")
    public String getUserForm(Model model) {
        log.info("사용자 등록 페이지 요청");
        return "user/form";
    }

    /**
     * 사용자 수정 페이지
     */
    @GetMapping("/view/users/{userId}/edit")
    public String getUserEditForm(@PathVariable Long userId, Model model) {
        log.info("사용자 수정 페이지 요청, id: {}", userId);
        model.addAttribute("userId", userId);
        return "user/edit";
    }

    /**
     * 내 프로필 페이지
     */
    @GetMapping("/view/profile")
    public String getProfile() {
        log.info("내 프로필 페이지 요청");
        return "user/profile";
    }
}
