# taboo2

REST service for tagged bookmarks.

This second version is built with Spring-Boot for the REST service and is using a Mongo-DB as backend store.

The project is configured so that it can be deployed to an OpenShift DIY cartridge.

## configuration

The application is configured with the _application.properties_ file, or if started with a specific Spring profile 
with the _application-\<profile\>.properties_ file. This is the standard Spring-Boot behaviour.

The default resources directory contains a sample file for running with no profile and a configuration for a profile
named openshift. As can be seen in the _.openshift/action_hooks/start_ file, this profile is activated when the 
application is deployed to OpenShift.

Both configuration samples contain all the entries that may be relevant for the application.

### users, passwords and roles

the configuration needs to reference a file with user data when _security.basic.enabled_ is configured to _true_.
This file has lines with the following content:

_username:hashedPassword:role1,...,roleN_

The hashedPassword can be created with the _main_ method of the Taboo2UserService class.

## Versions

### 0.3.0

version to be run on OpenShift with H2 backend

## Bookmarklet

The following bookmarklet loads the app and passes the url of the current page (set HOST andf PORT to where the 
application is installed):

javascript: (function() {
    var newLocation='http://HOST:PORT/#!?newBookmarkUrl='+encodeURIComponent(document.location.href);
    open(newLocation,'taboo2').focus();
})();

