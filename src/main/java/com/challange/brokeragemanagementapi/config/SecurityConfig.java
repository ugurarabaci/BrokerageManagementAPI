package com.challange.brokeragemanagementapi.config;

import com.challange.brokeragemanagementapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.user.name}")
    private String adminUsername;

    @Value("${spring.security.user.password}")
    private String adminPassword;
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // CSRF korumasını devre dışı bırak
                .headers().frameOptions().disable() // H2 konsolunun iframe'de çalışabilmesi için frameOptions'u devre dışı bırak
                .and()
                .authorizeHttpRequests() // authorizeRequests yerine authorizeHttpRequests kullanıyoruz
                // H2 konsoluna erişimi izin veriyoruz
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/orders/**").authenticated() // USER rolü olan kullanıcılar da erişebilir
                .requestMatchers("/api/assets/**").authenticated().and()
                .httpBasic(); // Basit HTTP temel yetkilendirmesi
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        UserDetails user1 = User.builder()
                .username("johndoe")
                .password(passwordEncoder().encode("password123"))
                .roles("USER")
                .build();

        UserDetails user2 = User.builder()
                .username("janesmith")
                .password(passwordEncoder().encode("password456"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user1, user2);
    }


}
