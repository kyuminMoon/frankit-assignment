package com.tistory.kmmoon.frankit.presentation.controller.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ErrorViewController {
    /**
     * 에러화면
     */

    @GetMapping("/error")
    public String defaultError() {
        log.info("에러 발생");
        return "error/default";
    }
}
