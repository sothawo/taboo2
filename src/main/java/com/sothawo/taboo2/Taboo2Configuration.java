/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class which is configured by 'taboo2' entries in the application configuration..
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Component
@ConfigurationProperties(prefix = "taboo2")
public class Taboo2Configuration {
// ------------------------------ FIELDS ------------------------------

    /** Info from the configuration, should be set in each application.properties file */
    private String info = "undefined";

    /** Name of the file with user/password information */
    private String users = null;

// --------------------- GETTER / SETTER METHODS ---------------------

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }
}
