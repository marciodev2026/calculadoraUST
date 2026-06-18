package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SquadResponse {

    private UUID id;
    private UUID projetoId;
    private String projetoNome;
    private Integer semanas;
    private Integer horasSemanais;
    private BigDecimal totalHoras;
    private BigDecimal totalUst;
    private BigDecimal valorTotal;
    private BigDecimal valorUst;
    private BigDecimal encargosPercentual;
    private BigDecimal bdiPercentual;
    private List<SquadMembroResponse> membros;
}
