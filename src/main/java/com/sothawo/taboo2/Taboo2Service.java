/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring-Boot Service implementation for the taboo backend service.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@RestController // contains @ResponseBody
@RequestMapping(Taboo2Service.MAPPING_TABOO2)
public class Taboo2Service {
// ------------------------------ FIELDS ------------------------------

    /** Mapping for the class, package scope for test class. */
    static final String MAPPING_TABOO2 = "/taboo2";
    /** Mapping for check call, package scope for test class. */
    static final String MAPPING_CHECK = "/check";
    /** Result of check call, package scope for test class. */
    static final String IS_RUNNING = "running";

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
}
