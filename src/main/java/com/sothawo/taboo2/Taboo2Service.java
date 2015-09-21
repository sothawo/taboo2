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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * Spring-Boot Service implementation for the taboo backend service.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@RestController // contains @ResponseBody
@RequestMapping(Taboo2Service.MAPPING_TABOO2)
public class Taboo2Service {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class */
    private final static Logger log = LoggerFactory.getLogger(Taboo2Service.class);

    /** Mapping for the class, package scope for test class. */
    static final String MAPPING_TABOO2 = "/taboo2";

    /** Mapping for check call, package scope for test class. */
    static final String MAPPING_CHECK = "/check";

    /** Result of check call, package scope for test class. */
    static final String IS_RUNNING = "running";

    @Autowired
    private Taboo2Configuration taboo2Config;

// -------------------------- STATIC METHODS --------------------------

    static {
        log.debug("class {} loaded", Taboo2Service.class.getCanonicalName());
    }

// --------------------------- CONSTRUCTORS ---------------------------

// -------------------------- OTHER METHODS --------------------------

    /**
     * simple method that returns a fixed string when called. Used to check if the service is running.
     *
     * @return
     */
    @RequestMapping(value = MAPPING_CHECK, method = RequestMethod.GET)
    public final String check() {
        return IS_RUNNING;
    }

    @PostConstruct
    private void postConstruct(){
        log.debug("taboo2.info={}", taboo2Config.getInfo());
    }
}
