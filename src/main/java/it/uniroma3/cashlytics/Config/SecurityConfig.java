package it.uniroma3.cashlytics.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import it.uniroma3.cashlytics.Service.CustomUserDetailsService;
import it.uniroma3.cashlytics.Service.OAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private OAuth2UserService oauth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/{username}/dashboard/**").authenticated()
                .requestMatchers("/{username}/account/**").authenticated()
                .anyRequest().authenticated())

                // Configurazione form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(dynamicSuccessHandler())
                        .failureUrl("/login?error=true")
                        .usernameParameter("email") // Cambiato da username a email
                        .passwordParameter("password")
                        .permitAll())

                // Configurazione OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService))
                        .successHandler(dynamicSuccessHandler())
                        .failureUrl("/login?error=oauth2"))

                // Configurazione logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll())

                // Configurazione gestione sessioni
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationSuccessHandler dynamicSuccessHandler() {
        return (request, response, authentication) -> {
            if (!response.isCommitted()) {
                String username;
                
                // Gestione per OAuth2 e form login
                if (authentication.getPrincipal() instanceof it.uniroma3.cashlytics.Model.CustomOAuth2User) {
                    username = ((it.uniroma3.cashlytics.Model.CustomOAuth2User) authentication.getPrincipal())
                            .getUser().getUsername();
                } else if (authentication.getPrincipal() instanceof it.uniroma3.cashlytics.Model.UserPrincipal) {
                    username = ((it.uniroma3.cashlytics.Model.UserPrincipal) authentication.getPrincipal())
                            .getUser().getUsername();
                } else {
                    // Fallback
                    username = authentication.getName();
                }
                
                String redirectUrl = request.getContextPath() + "/" + username + "/dashboard";
                System.out.println("Redirecting authenticated user " + username + " to: " + redirectUrl);
                response.sendRedirect(redirectUrl);
            } else {
                System.err.println("Response already committed, cannot redirect");
            }
        };
    }
}