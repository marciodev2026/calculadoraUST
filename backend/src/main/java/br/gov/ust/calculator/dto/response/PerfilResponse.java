package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerfilResponse {

    private UUID id;
    private String nome;
    private String descricao;
    private BigDecimal fcp;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
