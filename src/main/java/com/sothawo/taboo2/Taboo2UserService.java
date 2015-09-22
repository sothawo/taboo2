/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service to provide User details.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Component
public class Taboo2UserService implements UserDetailsService {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class */
    private final static Logger log = LoggerFactory.getLogger(Taboo2UserService.class);


    /** configuration object. */
    @Autowired
    Taboo2Configuration taboo2Configuration;

    // todo: remove this implementation
    /** dummy implementation for the beginning */
    private final Map<String, String> userPasswords = new HashMap<>();

    /** Map with known users. Key is the username, password ist hashed */
    private final Map<String, User> knownUsers = new HashMap<>();

    {
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface UserDetailsService ---------------------

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser;
        synchronized (knownUsers) {
            optionalUser = Optional.ofNullable(knownUsers.get(username));
            if (!optionalUser.isPresent()) {
                // reload the data from users file
                log.debug("loading user data");

                Optional.ofNullable(taboo2Configuration.getUsers())
                        .ifPresent(filename -> {
                            log.debug("user file: {}", filename);
                            try {
                                Files.lines(Paths.get(filename))
                                        .filter(line -> !line.isEmpty())
                                        .filter(line -> !line.startsWith("#"))
                                        .forEach(line -> {
                                            String[] fields = line.split(":");
                                            if (fields.length == 3) {
                                                String user = fields[0];
                                                String hashedPassword = fields[1];
                                                String[] roles = fields[2].split(",");
                                                if (roles.length < 1) {
                                                    roles = new String []{"undef"};
                                                }
                                                List<GrantedAuthority> authorities = new ArrayList<>();
                                                for (String role : roles) {
                                                    authorities.add(new SimpleGrantedAuthority(role));
                                                }
                                                knownUsers.put(user, new User(user, hashedPassword, authorities));
                                            }
                                        });
                                log.debug("loaded {} user(s)", knownUsers.size());
                            } catch (IOException e) {
                                log.debug("reading file", e);
                            }
                        });

                // search again after reload
                optionalUser = Optional.ofNullable(knownUsers.get(username));
            }
        }
        return optionalUser.orElseThrow(() -> new UsernameNotFoundException(username));
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
