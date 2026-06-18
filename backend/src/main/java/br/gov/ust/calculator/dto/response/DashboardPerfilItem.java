package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardPerfilItem {

    private UUID perfilId;
    private String perfilNome;
    private Integer quantidadeProfissionais;
    private BigDecimal totalHoras;
    private BigDecimal totalUst;
}
