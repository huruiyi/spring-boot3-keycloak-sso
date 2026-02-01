package com.example.appa.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * 自定义 JwtDecoder，支持解码 Keycloak 的 logout token (typ=logout+jwt)
 */
public class LogoutTokenJwtDecoder implements JwtDecoder {

    private final JwtDecoder delegate;

    public LogoutTokenJwtDecoder(String jwkSetUri) {
        this.delegate = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwtProcessorCustomizer(this::customizeJwtProcessor)
                .build();
    }

    private void customizeJwtProcessor(JWTProcessor<SecurityContext> jwtProcessor) {
        if (jwtProcessor instanceof DefaultJWTProcessor) {
            DefaultJWTProcessor<SecurityContext> processor = (DefaultJWTProcessor<SecurityContext>) jwtProcessor;
            // 允许 JWT 和 logout+jwt 类型
            processor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
                    new JOSEObjectType("JWT"),
                    new JOSEObjectType("logout+jwt")
            ));
        }
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        return delegate.decode(token);
    }
}
