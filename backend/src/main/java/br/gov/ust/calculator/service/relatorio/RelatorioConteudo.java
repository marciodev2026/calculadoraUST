package br.gov.ust.calculator.service.relatorio;

import java.math.BigDecimal;
import java.util.List;

import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.Squad;
import br.gov.ust.calculator.entity.SquadMembro;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelatorioConteudo {

    private Simulacao simulacao;
    private Configuracao configuracao;
    private List<ProjetoRelatorioLinha> projetos;
    private BigDecimal totalHoras;
    private BigDecimal totalUst;
    private BigDecimal valorTotal;
    private String nomeOrganizacao;
    private byte[] logoBytes;

    @Getter
    @Builder
    public static class ProjetoRelatorioLinha {
        private Projeto projeto;
        private Squad squad;
        private List<SquadMembro> membros;
        private BigDecimal totalHoras;
        private BigDecimal totalUst;
        private BigDecimal valorTotal;
    }
}
