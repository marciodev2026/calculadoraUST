package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {

    private long totalSimulacoes;
    private long totalProjetos;
    private long totalSquads;
    private BigDecimal totalHoras;
    private BigDecimal totalUst;
    private BigDecimal valorTotal;
    private List<DashboardProjetoItem> ustPorProjeto;
    private List<DashboardPerfilItem> distribuicaoPorPerfil;
    private List<DashboardTipoItem> valorPorTipo;
    private List<DashboardSimulacaoItem> simulacoesRecentes;
}
