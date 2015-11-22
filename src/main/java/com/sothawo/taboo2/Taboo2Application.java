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
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableConfigurationProperties
public class Taboo2Application {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class. */
    private final static Logger log = LoggerFactory.getLogger(Taboo2Application.class);

    /** the application's environment. */
    @Autowired
    private Environment env;

    /** the taboo2 configuration. */
    @Autowired
    private Taboo2Configuration taboo2Configuration;

    // initialize logging and install Bridge from JUL to SLF4J
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter crlf = new CommonsRequestLoggingFilter();
        crlf.setIncludeClientInfo(true);
        crlf.setIncludeQueryString(true);
        crlf.setIncludePayload(true);
        return crlf;
    }
// -------------------------- OTHER METHODS --------------------------

    @PostConstruct
    public void postConstruct() {
        log.debug("Java vendor: {}", env.getProperty("java.vendor", "unknown"));
        log.debug("Java version: {}", env.getProperty("java.version", "unknown"));
        log.debug("taboo2 version: {}", taboo2Configuration.getVersion());
    }

// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        SpringApplication.run(Taboo2Application.class, args);
    }
}
