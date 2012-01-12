ServiceUrlChecker CAS server filter servlet
-
A servlet filter to use with CAS server 3.1 allowing to determine which url can be authorized as service url

## Why use ServiceUrlChecker?

When an organization hosts many web apps under same domainthey can use CAS without explicitly permission, it is sufficient to use a CAS client and set the service url correctly.

This problem is solved handling a white-list of authorized service urls.

Suppose your domain is mydomain.edu, CAS is configured on http://cas.mydomain.edu

Two apps are mapped on

- http://mydomain.edu/app1
- http://mydomain.edu/app2

You want only app1 can use the CAS but app2 receive an error if it tries to login using CAS, you simply need to add app1 url on ServiceUrlChecker.

## Filter Installation

The CAS server must be modified to use the filter

### Add filter to web.xml

Modify the file cas.war\WEB-INF\web.xml and add the filter

    <!--
        Configuration is read from the service_urls.properties file present on classpath
        Generally it is present on cas.war\WEB-INF\classes directory
    -->
	<filter>
		<filter-name>ServiceUrl Checker</filter-name>
		<filter-class>it.unipa.cuc.cas.filter.ServiceUrlCheckerFilter</filter-class>
	</filter>
	
	<filter-mapping>
		<filter-name>ServiceUrl Checker</filter-name>
		<url-pattern>/login</url-pattern>
	</filter-mapping>

## Enable log

Add to end of cas.war/WEB-INF/classes/log4j.properties

log4j.logger.it.unipa.cuc.cas=DEBUG

## Add allowed url

Modify the file cas.war\WEB-INF\classes\service_urls.properties adding one url per file with the syntax

valid.service.url=http://localhost/mywebapp/home.seam
valid.service.url=http://mywebsite/.*
