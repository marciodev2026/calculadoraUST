package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfiguracaoInstitucionalResponse {

    private UUID id;
    private String nomeOrganizacao;
    private boolean possuiLogo;
    private LocalDateTime updatedAt;
}
