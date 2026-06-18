package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConfiguracaoUstResponse {

    private UUID id;
    private BigDecimal valorUst;
    private Integer cargaHorariaMes;
    private BigDecimal encargosPercentual;
    private BigDecimal bdiPercentual;
    private LocalDate vigenteDesde;
    private Boolean ativo;
    private BigDecimal multiplicadorFinanceiro;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
