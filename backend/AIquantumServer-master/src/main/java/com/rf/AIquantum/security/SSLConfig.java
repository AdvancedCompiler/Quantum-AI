package com.rf.AIquantum.security;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * @author zzf
 * @description:
 * @date 2021/9/15 14:32
 */
@Configuration
@Profile({"prod", "public"})
public class SSLConfig {

    @Value("${spring.profiles.active}")
    private String profile;

    @Bean
    public TomcatServletWebServerFactory servletContainer() {

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {

            @Override
            protected void postProcessContext(Context context) {

                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
        return tomcat;
    }


    private Connector initiateHttpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setSecure(false);
        if ("prod".equals(profile)) {
            connector.setPort(8082);
            connector.setRedirectPort(8848);
        } else if ("public".equals(profile)) {
            connector.setPort(8081);
            connector.setRedirectPort(8443);
        } else {
            //TODO 待定
            connector.setPort(9528);
            connector.setRedirectPort(8849);
        }


        return connector;
    }

}
