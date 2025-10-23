package com.example.Alotrabong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails devUser = User.withUsername("dev")
                .password(encoder.encode("dev123"))
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(devUser);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // ====================== API CHAIN (JWT) ======================
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**") // <-- QUAN TRỌNG: chỉ áp cho /api/**
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/items/**", "/api/categories/**", "/api/branches/**",
                                "/actuator/health", "/ping",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/**",
                                "/swagger-ui.html", "/swagger-ui/index.html", "/swagger-ui/")
                        .permitAll()
                        .requestMatchers("/api/cart/**", "/api/orders/**", "/api/users/profile").hasRole("USER")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ====================== WEB CHAIN (FORM LOGIN) ======================
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**") // <-- bắt phần còn lại
                // CSRF: giữ bật cho form login; ignore H2 nếu dùng
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth", "/register", "/forgot-password",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/h2-console/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/docs/**",
                                "/", "/login" // trang public
                        ).permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/auth")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth?error"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth?logout"))
                // web dùng session (form login)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        // KHÔNG add JWT filter ở chain web
        return http.build();
    }
}
