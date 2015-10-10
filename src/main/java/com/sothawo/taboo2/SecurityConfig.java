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
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security configuration.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Configuration
@EnableWebMvcSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class */
    private final static Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /** the configuration object */
    @Autowired
    SecurityProperties securityProperties;

    /** the service to provide the user data */
    @Autowired
    Taboo2UserService userService;

// -------------------------- OTHER METHODS --------------------------

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        boolean requireSsl = securityProperties.isRequireSsl();
        boolean basicEnabled = securityProperties.getBasic().isEnabled();
        log.debug("configuring http, requires ssl: {}, basic authentication: {}", requireSsl, basicEnabled);
        if (requireSsl) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
        if (basicEnabled) {
            // authentication for the taboo2 service only, the app itself doesn't need use it to display it's own login
            // form.
            http.authorizeRequests()
                    .antMatchers("/taboo2/**").authenticated()
                    .anyRequest().permitAll();
        }
        http.httpBasic().realmName("taboo2");
        http.csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    }
}
