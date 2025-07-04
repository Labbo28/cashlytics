package it.uniroma3.cashlytics.Config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import it.uniroma3.cashlytics.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                .requestMatchers("/{username}/dashboard/**").authenticated()
                .requestMatchers("/{username}/account/**").authenticated()
                .anyRequest().authenticated())

                // Configurazione form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(dynamicSuccessHandler())
                        .failureUrl("/login?error=true")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll())

                // Configurazione logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll())

                // Configurazione gestione sessioni
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                            .sessionFixation(fixation -> fixation.migrateSession());
                    SessionManagementConfigurer<HttpSecurity>.ConcurrencyControlConfigurer concurrencyControlConfigurer = session
                            .maximumSessions(1);
                    concurrencyControlConfigurer.maxSessionsPreventsLogin(false);
                });
        // .exceptionHandling(ex -> ex.accessDeniedPage("/login?error=access_denied"));

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    AuthenticationSuccessHandler dynamicSuccessHandler() {
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
