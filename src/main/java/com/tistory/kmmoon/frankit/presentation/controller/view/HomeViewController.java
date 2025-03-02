package com.tistory.kmmoon.frankit.presentation.controller.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeViewController {

    /**
     * 로그인 페이지
     */
    @GetMapping("/home")
    public String login() {
        log.info("홈페이지 요청");
        return "home";
    }
}
