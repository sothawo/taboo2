/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to provide User details.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class Taboo2UserService implements UserDetailsService {
    /** Logger for the class */
    private final static Logger log = LoggerFactory.getLogger(Taboo2UserService.class);

    /** Encode for passwords */
    private final PasswordEncoder pwEncoder;

    public Taboo2UserService(BCryptPasswordEncoder pwEncoder) {
        this.pwEncoder = pwEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        String encodedPassword = pwEncoder.encode(username);
        log.debug("dummy loading for user {} with encrypted password {}", username, encodedPassword);
        return new User(username, encodedPassword, authorities);
    }
}
