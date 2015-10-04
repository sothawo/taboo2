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

    /** version of the application */
    private String version = "unknown";

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
