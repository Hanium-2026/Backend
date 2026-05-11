package com.nevo.nevo.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDto(
        @NotBlank(message = "이름은 비어있을 수 없습니다.")
        String name) {
}
