package org.example.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Boot Admin 安全配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminServerProperties adminServerProperties;

    public SecurityConfig(AdminServerProperties adminServerProperties) {
        this.adminServerProperties = adminServerProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = 
            new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServerProperties.getContextPath() + "/");

        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(new AntPathRequestMatcher(adminServerProperties.getContextPath() + "/assets/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(adminServerProperties.getContextPath() + "/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(adminServerProperties.getContextPath() + "/actuator/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(adminServerProperties.getContextPath() + "/login")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl(adminServerProperties.getContextPath() + "/logout")
                .logoutSuccessUrl(adminServerProperties.getContextPath() + "/login")
                .permitAll()
            )
            .httpBasic(httpBasic -> httpBasic
                .realmName("Spring Boot Admin")
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions().deny()
            );

        return http.build();
    }
} 