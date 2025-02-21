package xyz.jiniux.aap.security;

import com.okta.spring.boot.oauth.Okta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF as we're using JWT tokens
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(requests -> requests
                        // Public endpoints
                        .requestMatchers(HttpMethod.GET, "/books").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/{isbn}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/authors").permitAll()
                        .requestMatchers(HttpMethod.GET, "/authors/{authorId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publishers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/publishers/{publisherId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/book-format-preview-images/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/shipping/action/calculate-price").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Secure all other endpoints
                        .anyRequest().authenticated()
                )
                .cors(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(makePermissionsConverter())))
                ;

        Okta.configureResourceServer401ResponseBody(http);
        return http.build();
    }

    private JwtAuthenticationConverter makePermissionsConverter() {
        final var jwtAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtAuthoritiesConverter.setAuthoritiesClaimName("scope");
        jwtAuthoritiesConverter.setAuthorityPrefix("");

        final var jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwtAuthoritiesConverter);

        return jwtAuthConverter;
    }
}