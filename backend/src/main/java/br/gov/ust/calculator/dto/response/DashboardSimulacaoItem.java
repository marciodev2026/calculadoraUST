package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.SimulacaoStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSimulacaoItem {

    private UUID id;
    private String nomeCompleto;
    private String orgao;
    private LocalDate dataSimulacao;
    private SimulacaoStatus status;
    private long totalProjetos;
    private BigDecimal totalUst;
    private BigDecimal valorTotal;
}
