package br.gov.ust.calculator.service.relatorio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.Squad;
import br.gov.ust.calculator.entity.SquadMembro;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.repository.ConfiguracaoRepository;
import br.gov.ust.calculator.repository.ProjetoRepository;
import br.gov.ust.calculator.repository.SquadRepository;
import br.gov.ust.calculator.service.SimulacaoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioDataService {

    private final SimulacaoService simulacaoService;
    private final ProjetoRepository projetoRepository;
    private final SquadRepository squadRepository;
    private final ConfiguracaoRepository configuracaoRepository;

    @Transactional
    public RelatorioConteudo carregar(UUID simulacaoId) {
        Simulacao simulacao = simulacaoService.obterComAcesso(simulacaoId);
        Configuracao configuracao = configuracaoRepository.findByAtivoTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma configuração UST ativa encontrada"));

        List<Projeto> projetos = projetoRepository.findBySimulacaoIdOrderByNomeAsc(simulacaoId);
        List<RelatorioConteudo.ProjetoRelatorioLinha> linhas = new ArrayList<>();

        BigDecimal totalHoras = BigDecimal.ZERO;
        BigDecimal totalUst = BigDecimal.ZERO;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (Projeto projeto : projetos) {
            Squad squad = squadRepository.findByProjetoId(projeto.getId()).orElse(null);

            BigDecimal linhaHoras = squad != null ? zeroIfNull(squad.getTotalHoras()) : BigDecimal.ZERO;
            BigDecimal linhaUst = squad != null ? zeroIfNull(squad.getTotalUst()) : BigDecimal.ZERO;
            BigDecimal linhaValor = squad != null ? zeroIfNull(squad.getValorTotal()) : BigDecimal.ZERO;

            totalHoras = totalHoras.add(linhaHoras);
            totalUst = totalUst.add(linhaUst);
            valorTotal = valorTotal.add(linhaValor);

            List<SquadMembro> membros = squad != null ? squad.getMembros() : List.of();
            if (squad != null) {
                membros.forEach(m -> {
                    if (m.getPerfil() != null) {
                        m.getPerfil().getNome();
                    }
                });
            }

            linhas.add(RelatorioConteudo.ProjetoRelatorioLinha.builder()
                    .projeto(projeto)
                    .squad(squad)
                    .membros(membros)
                    .totalHoras(linhaHoras)
                    .totalUst(linhaUst)
                    .valorTotal(linhaValor)
                    .build());
        }

        return RelatorioConteudo.builder()
                .simulacao(simulacao)
                .configuracao(configuracao)
                .projetos(linhas)
                .totalHoras(totalHoras)
                .totalUst(totalUst)
                .valorTotal(valorTotal)
                .build();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
