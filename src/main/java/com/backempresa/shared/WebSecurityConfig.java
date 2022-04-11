package com.backempresa.shared;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()       // Comentar esta línea para los tests con sonarqube
                .addFilterAfter(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests()
                .antMatchers("/api/v0/token/**").permitAll()
                //.antMatchers(HttpMethod.GET, "/api/v0/reserva").hasAnyRole("USER","ADMIN")
                //.antMatchers(HttpMethod.POST, "/persona").hasRole("ADMIN")
                //.antMatchers(HttpMethod.PUT, "/persona").hasRole("ADMIN")
                //.antMatchers(HttpMethod.DELETE, "/persona").hasRole("ADMIN")
                .anyRequest().authenticated();

    }
}
