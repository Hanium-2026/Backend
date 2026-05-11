package com.nevo.nevo.user;

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
