package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardProjetoItem {

    private UUID projetoId;
    private String projetoNome;
    private ProjetoTipo tipo;
    private String simulacaoNome;
    private BigDecimal totalUst;
    private BigDecimal valorTotal;
}
