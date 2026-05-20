package com.nevo.nevo.user.dto;

import com.nevo.nevo.user.entity.Role;
import com.nevo.nevo.user.entity.User;

public class UserResponse {

    public record MyInfo(Long id, String phone, String name, Role role) {
        public static MyInfo from(User user) {
            return new MyInfo(
                    user.getId(),
                    user.getPhone(),
                    user.getName(),
                    user.getRole()
            );
        }
    }
}