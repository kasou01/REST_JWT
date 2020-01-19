package com.RESTJWT.config;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {
    private final DefaultPasswordService passwordService = new DefaultPasswordService();

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordService.encryptPassword(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordService.passwordsMatch(rawPassword,encodedPassword);
    }
}
