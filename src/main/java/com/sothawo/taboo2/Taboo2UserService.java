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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to provide User details.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class Taboo2UserService implements UserDetailsService {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class */
    private final static Logger log = LoggerFactory.getLogger(Taboo2UserService.class);

    // todo: remove this implementation
    /** dummy implementation for the beginning */
    private final Map<String, String> userPasswords = new HashMap<>();
    {
        userPasswords.put("admin", "$2a$10$DvM65o0Sw5/CLUjnrou/ouAyhhrod5PtdoEVb.mi7HbujuzFOqKuW");
        userPasswords.put("answer", "$2a$10$oU1DPzo3s7jaGNsABpn7JuF5x0yhaQbcv9rSrBCVVs.6WbXZZGqgG");
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UserDetailsService ---------------------

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // get the encoded password from the dummy store
        String encodedPassword = userPasswords.get(username);
        if (null != encodedPassword) {
            log.debug("trying to authenticate user {}.", username);
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));
            return new User(username, encodedPassword, authorities);
        }
        throw new UsernameNotFoundException(username);
    }

// --------------------------- main() method ---------------------------

    /**
     * encodes the string that is given as argument.
     *
     * @param args
     *         program arguments.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.printf(new BCryptPasswordEncoder().encode(args[0]));
        }
    }
}
