package it.uniroma3.cashlytics.cashlytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.http.SessionCreationPolicy;
import lombok.RequiredArgsConstructor;
import it.uniroma3.cashlytics.cashlytics.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disabilita CSRF temporaneamente per risolvere il problema
            .csrf(csrf -> csrf.disable())
            
            // Configurazione autorizzazioni
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/{username}/dashboard/**").authenticated()
                .requestMatchers("/{username}/account/**").authenticated()
                .anyRequest().authenticated()
            )
            
            // Configurazione form login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(dynamicSuccessHandler())
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            
            // Configurazione logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Configurazione gestione sessioni
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .and()
                .sessionFixation().migrateSession()
            )
            
            // Gestione eccezioni per accesso negato
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/login?error=access_denied")
            );
            
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                  .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationSuccessHandler dynamicSuccessHandler() {
        return (request, response, authentication) -> {
            if (!response.isCommitted()) {
                String username = authentication.getName();
                String redirectUrl = request.getContextPath() + "/" + username + "/dashboard";
                System.out.println("Redirecting authenticated user " + username + " to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
            } else {
                System.err.println("Response already committed, cannot redirect");
            }
        };
    }
}