package com.ecotrack.ecotrack_api.config;

import com.ecotrack.ecotrack_api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/empresas/**").hasAnyRole("ADMIN", "GERADORA", "TRANSPORTADORA", "RECEPTORA")
                        .requestMatchers(HttpMethod.POST, "/empresas/**").hasAnyRole("ADMIN", "GERADORA", "TRANSPORTADORA", "RECEPTORA")
                        .requestMatchers(HttpMethod.DELETE, "/empresas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/lotes/**").hasAnyRole("ADMIN", "GERADORA", "TRANSPORTADORA", "RECEPTORA")
                        .requestMatchers(HttpMethod.POST, "/lotes/**").hasAnyRole("ADMIN", "GERADORA")
                        .requestMatchers(HttpMethod.PATCH, "/lotes/**").hasAnyRole("ADMIN", "TRANSPORTADORA", "GERADORA")
                        .requestMatchers(HttpMethod.GET, "/transportes/**").hasAnyRole("ADMIN", "GERADORA", "TRANSPORTADORA", "RECEPTORA")
                        .requestMatchers(HttpMethod.POST, "/transportes/**").hasAnyRole("ADMIN", "GERADORA")
                        .requestMatchers(HttpMethod.PATCH, "/transportes/*/recebimento-final").hasAnyRole("ADMIN", "RECEPTORA")
                        .requestMatchers(HttpMethod.PATCH, "/transportes/*/recebimento-final/recusar").hasAnyRole("ADMIN", "RECEPTORA")
                        .requestMatchers(HttpMethod.PATCH, "/transportes/**").hasAnyRole("ADMIN", "TRANSPORTADORA")
                        .requestMatchers(HttpMethod.GET, "/dashboard/**").hasAnyRole("ADMIN", "GERADORA", "TRANSPORTADORA", "RECEPTORA")
                        .requestMatchers(HttpMethod.GET, "/relatorios/**").hasAnyRole("ADMIN", "GERADORA", "TRANSPORTADORA", "RECEPTORA")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/me").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "https://ecotrack.vercel.app", "https://ecotrack-khaki.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
