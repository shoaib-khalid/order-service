package com.kalsym.order.service.config;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author 7cu
 */
//@Configuration
//@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private SessionAuthenticationEntryPoint sessionAuthenticationEntryPoint;

    @Autowired
    private Environment env;

    //@Override
//    protected void configured(HttpSecurity httpSecurity) throws Exception {
//
//        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Configuring web security...");
//        boolean enableAPIsecurity = Boolean.parseBoolean(env.getProperty("web.security.enabled", "true"));
//
//        if (!enableAPIsecurity) {
//            //httpSecurity.formLogin().disable();
//            //httpSecurity.httpBasic().disable();
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "web.security.enabled=false");
//        } else {
//            httpSecurity
//                    .authorizeRequests()
//                    .anyRequest()
//                    .authenticated()
//                    .and()
//                    .httpBasic();
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "web.security.enabled=true");
//        }
//    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
                String logprefix = "handleEvent";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Configuring web AuthenticationManagerBuilder security...");
        boolean enableAPIsecurity = Boolean.parseBoolean(env.getProperty("web.security.enabled", "true"));
        if (enableAPIsecurity) {
            PasswordEncoder encoder
                    = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            auth
                    .inMemoryAuthentication()
                    .withUser("admin")
                    .password(encoder.encode("admin"))
                    .roles("USER", "ADMIN");
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "web.security.enabled=true");
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "web.security.enabled=false");
        }

    }

    //@Override
    protected void configured(HttpSecurity httpSecurity) throws Exception {
        String logprefix = "configured";

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Configuring web HttpSecurity security...");

        boolean enableAPIsecurity = Boolean.parseBoolean(env.getProperty("web.security.enabled", "true"));

        if (enableAPIsecurity) {
            httpSecurity
                    .authorizeRequests()
                    .antMatchers("*/*")
                    .permitAll()
                    .anyRequest().authenticated().and()
                    .exceptionHandling().authenticationEntryPoint(sessionAuthenticationEntryPoint).and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .cors().and().csrf().disable();;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "web.security.enabled=true");
        } else {
            httpSecurity
                    .authorizeRequests()
                    .antMatchers("*/*")
                    .permitAll();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "web.security.enabled=false");
        }
//                .anyRequest()
//                .authenticated()
//                .and()
//                .httpBasic();

//        httpSecurity.csrf().disable()
//                .authorizeRequests()
//                .antMatchers(
//                        "/clients/authenticate",
//                        "/clients/register",
//                        "/customers/authenticate",
//                        "/customers/register",
//                        "/administrators/authenticate",
//                        "/administrators/register",
//                        "/error",
//                        "/v2/api-docs",
//                        "/configuration/ui",
//                        "/swagger-resources/**",
//                        "/configuration/security",
//                        "/swagger-ui.html",
//                        "/webjars/**").denyAll()
//                .anyRequest().authenticated().and()
//                .exceptionHandling().authenticationEntryPoint(sessionAuthenticationEntryPoint).and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                .cors().and().csrf().disable();;
    }
}
