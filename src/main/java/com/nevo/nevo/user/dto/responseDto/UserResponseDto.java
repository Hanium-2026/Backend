package com.nevo.nevo.user.dto.responseDto;

import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.user.entity.User;

public record UserResponseDto(Long id, String email, String name, Role role) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }
}
