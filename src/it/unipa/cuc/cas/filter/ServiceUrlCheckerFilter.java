package it.unipa.cuc.cas.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This filter allows to verify if received serviceUrl is authorized and
 * CAS can be used to authenticate the user
 * The example shown below checks the access to login servlet
 *  <filter>
 *       <filter-name>ServiceUrl Checker</filter-name>
 *       <filter-class>it.unipa.cuc.cas.filter.ServiceUrlCheckerFilter</filter-class>
 *        <init-param>
 *            <param-name>errorPage</param-name>
 *            <param-value></param-value>
 *        </init-param>
 *    </filter>
 *    
 *    <filter-mapping>
 *        <filter-name>ServiceUrl Checker</filter-name>
 *        <url-pattern>/login</url-pattern>
 *    </filter-mapping>
 *
 * Allowed urls must be listed inside the file service_urls.properties that
 * must be present on classpath
 * service_urls.properties example
 * valid.service.url=http://server1/home.php
 * valid.service.url=http://server2/home.seam
 * 
 * @author davide ficano
 *
 */
public class ServiceUrlCheckerFilter implements Filter {
    private PropertiesConfiguration config = null;
    private final Log log = LogFactory.getLog(getClass());

    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            config = new PropertiesConfiguration("service_urls.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;
        String serviceUrl = request.getParameter("service");
        String allowUntrustedLogin = config.getString("allow.untrusted.login", "never");
        
        if (allowUntrustedLogin.equalsIgnoreCase("always")) {
            filterChain.doFilter(request, response);
        } else if (allowUntrustedLogin.equalsIgnoreCase("warning")) {
            handleWarningLogin(httpServletRequest, httpServletResponse, filterChain,
                    serviceUrl);
        } else if (allowUntrustedLogin.equalsIgnoreCase("never")) {
            handleNeverLogin(httpServletRequest, httpServletResponse, filterChain,
                    serviceUrl);
        }
    }

    private void handleNeverLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String serviceUrl) throws IOException, ServletException {
        if (isServiceUrlValid(serviceUrl)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Invalid serviceUrl: " + serviceUrl);
            redirectMessagePage(request, response, config.getString("error.page", ""));
        }
    }

    private void handleWarningLogin(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            String serviceUrl) throws IOException, ServletException {
        if ("true".equals(request.getParameter("allowAccess"))) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isServiceUrlValid(serviceUrl)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Invalid serviceUrl: " + serviceUrl);

            String url = config.getString("warning.app.page", "");
            if (url.trim().length() != 0) {
                url += "?" + request.getQueryString();
            }
            redirectMessagePage(request, response, url);
        }
    }

    private void redirectMessagePage(HttpServletRequest request, HttpServletResponse response, String url)
        throws IOException {
        if (url == null || url.trim().length() == 0) {
            response.getWriter().print("Application non allowed, check administrator to add it");
        } else {
            response.sendRedirect(response.encodeURL(url));
        }
    }

    private boolean isServiceUrlValid(String serviceUrl) {
        if (serviceUrl != null) {
            List<String> list = config.getList("valid.service.url");
            for (String url : list) {
                // accepts regexp
                if (serviceUrl.matches(url)) {
                    return true;
                }
            }
        }
        return false;
    }
}
