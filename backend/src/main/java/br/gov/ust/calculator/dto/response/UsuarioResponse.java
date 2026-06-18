package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsuarioResponse {

    private UUID id;
    private String nomeCompleto;
    private String email;
    private String orgao;
    private String departamento;
    private String telefone;
    private UserRole role;
    private Boolean ativo;
    private LocalDateTime createdAt;
}
