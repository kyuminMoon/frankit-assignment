package com.tistory.kmmoon.frankit.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ModifyUserRequest {
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String beforePassword;

    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    public boolean valid() {
        if(!beforePassword.isEmpty() && (beforePassword.length() < 8 || beforePassword.length() > 20)) {
            return false;
        }
        if(!beforePassword.isEmpty() && (password.length() < 8 || password.length() > 20)) {
            return false;
        }
        if(!beforePassword.isEmpty() && (name.length() < 2 || name.length() > 50)) {
            return false;
        }

        return true;
    }
}