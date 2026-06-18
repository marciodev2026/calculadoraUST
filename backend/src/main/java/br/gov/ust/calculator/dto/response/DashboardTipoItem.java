package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;

import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardTipoItem {

    private ProjetoTipo tipo;
    private long quantidadeProjetos;
    private BigDecimal totalUst;
    private BigDecimal valorTotal;
}
