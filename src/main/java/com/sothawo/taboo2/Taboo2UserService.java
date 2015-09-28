/*
 Copyright 2015 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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

    /** Map with known users. Key is the username, password ist hashed */
    private final Map<String, User> knownUsers = new HashMap<>();

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
                knownUsers.clear();
                Optional.ofNullable(taboo2Configuration.getUsers())
                        .ifPresent(filename -> {
                            log.debug("user file: {}", filename);
                            try {
                                Files.lines(Paths.get(filename))
                                        .map(String::trim)
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
        // need to return a copy as Spring security erases the password in the object after verification
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException(username));
        return new User(user.getUsername(), user.getPassword(), user.getAuthorities());
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
