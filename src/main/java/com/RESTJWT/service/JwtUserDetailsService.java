package com.RESTJWT.service;


import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final Map<String, UserDetails> users = new HashMap<>();

    @PostConstruct
    public void initJwtUserDetailsService(){
        //仮データ
        User.UserBuilder userBuilder = User.builder().passwordEncoder(x -> new BCryptPasswordEncoder().encode(x));
        for(int i=0;i<50;i++){
            UserDetails user = userBuilder.username("user"+i).password("123456").roles("USER").build();
            users.put(user.getUsername(),user);
        }

        UserDetails admin = userBuilder.username("java").password("123456").roles("ADMIN").build();
        users.put(admin.getUsername(),admin);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetails user = users.get(username.toLowerCase());

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(user.getUsername(), user.getPassword(), user.isEnabled(),
                user.isAccountNonExpired(), user.isCredentialsNonExpired(),
                user.isAccountNonLocked(), user.getAuthorities());
    }
}