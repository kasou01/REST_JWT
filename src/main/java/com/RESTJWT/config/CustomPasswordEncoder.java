package com.RESTJWT.config;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomPasswordEncoder implements PasswordEncoder {
    private final DefaultPasswordService passwordService = new DefaultPasswordService();

    @Override
    public String encode(CharSequence rawPassword) {
        System.out.println(rawPassword);

        return passwordService.encryptPassword(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        System.out.println(rawPassword);
        System.out.println(encodedPassword);
        return passwordService.passwordsMatch(rawPassword,encodedPassword);
    }
}
