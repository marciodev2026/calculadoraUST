package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import br.gov.ust.calculator.entity.enums.SimulacaoStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelatorioPreviewResponse {

    private SimulacaoResumo simulacao;
    private ConfiguracaoUstResumo configuracao;
    private InstitucionalResumo institucional;
    private TotaisResumo totais;
    private List<GrupoTipoResumo> grupos;

    @Getter
    @Builder
    public static class SimulacaoResumo {
        private UUID id;
        private String nomeCompleto;
        private String email;
        private String orgao;
        private String departamento;
        private String telefone;
        private LocalDate dataSimulacao;
        private SimulacaoStatus status;
    }

    @Getter
    @Builder
    public static class ConfiguracaoUstResumo {
        private BigDecimal valorUst;
        private Integer cargaHorariaMes;
        private BigDecimal encargosPercentual;
        private BigDecimal bdiPercentual;
        private LocalDate vigenteDesde;
    }

    @Getter
    @Builder
    public static class InstitucionalResumo {
        private String nomeOrganizacao;
        private boolean possuiLogo;
    }

    @Getter
    @Builder
    public static class TotaisResumo {
        private int totalProjetos;
        private BigDecimal totalHoras;
        private BigDecimal totalUst;
        private BigDecimal valorTotal;
    }

    @Getter
    @Builder
    public static class GrupoTipoResumo {
        private ProjetoTipo tipo;
        private String tipoLabel;
        private TotaisResumo subtotais;
        private List<ProjetoResumo> projetos;
    }

    @Getter
    @Builder
    public static class ProjetoResumo {
        private UUID id;
        private String nome;
        private ProjetoTipo tipo;
        private Integer semanas;
        private Integer horasSemanais;
        private BigDecimal totalHoras;
        private BigDecimal totalUst;
        private BigDecimal valorTotal;
        private List<MembroResumo> membros;
    }

    @Getter
    @Builder
    public static class MembroResumo {
        private String perfilNome;
        private Integer quantidade;
        private BigDecimal fcpAplicado;
        private BigDecimal horasCalculadas;
        private BigDecimal ustCalculada;
    }
}
