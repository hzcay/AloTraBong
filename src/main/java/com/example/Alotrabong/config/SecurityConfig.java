package com.example.Alotrabong.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**", "/ws/**"))
                .cors(Customizer.withDefaults())
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth

                        // ===== STATIC / ASSETS PUBLIC =====
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**", // 👈 thêm dòng này
                                "/uploads/**", // 👈 nếu bạn serve ảnh review từ /uploads/...
                                "/webjars/**",
                                "/h2-console/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/docs/**",
                                "/test/**",
                                "/ws/**")
                        .permitAll()

                        // ===== PUBLIC PAGES =====
                        .requestMatchers(
                                "/", "/index",
                                "/login",
                                "/auth",
                                "/register",
                                "/forgot-password")
                        .permitAll()

                        // ===== PUBLIC 'USER FACING' PAGES =====
                        .requestMatchers(
                                "/user", "/user/",
                                "/user/home", "/user/home/**",
                                "/user/product/**",
                                "/user/branches", "/user/branch/list",
                                "/user/coupon/list",
                                "/user/favorite/list")
                        .permitAll()

                        // ===== PUBLIC API =====
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/items/**",
                                "/api/categories/**",
                                "/api/branches/**")
                        .permitAll()

                        // ===== AUTH REQUIRED =====
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/branch/**").hasAnyRole("ADMIN", "BRANCH_MANAGER")
                        .requestMatchers("/shipper/**").hasAnyRole("ADMIN", "SHIPPER")

                        .requestMatchers("/user/**").hasRole("USER")

                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/**",
                                "/api/users/profile")
                        .hasRole("USER")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/auth")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/auth?error"))
                .logout(logout -> logout.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}