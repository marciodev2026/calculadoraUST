package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String tipo;
    private long expiracaoMs;
    private UsuarioResponse usuario;

    public static LoginResponse of(String token, long expiracaoMs, UsuarioResponse usuario) {
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiracaoMs(expiracaoMs)
                .usuario(usuario)
                .build();
    }
}
