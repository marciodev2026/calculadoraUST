package br.gov.ust.calculator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.response.DashboardPerfilItem;
import br.gov.ust.calculator.dto.response.DashboardProjetoItem;
import br.gov.ust.calculator.dto.response.DashboardResponse;
import br.gov.ust.calculator.dto.response.DashboardSimulacaoItem;
import br.gov.ust.calculator.dto.response.DashboardTipoItem;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.Squad;
import br.gov.ust.calculator.entity.SquadMembro;
import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import br.gov.ust.calculator.entity.enums.UserRole;
import br.gov.ust.calculator.repository.ProjetoRepository;
import br.gov.ust.calculator.repository.SimulacaoRepository;
import br.gov.ust.calculator.repository.SquadRepository;
import br.gov.ust.calculator.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SimulacaoRepository simulacaoRepository;
    private final ProjetoRepository projetoRepository;
    private final SquadRepository squadRepository;

    @Transactional(readOnly = true)
    public DashboardResponse obterIndicadores() {
        UserPrincipal principal = getUsuarioLogado();
        UUID usuarioFiltro = principal.getRole() == UserRole.ANALISTA
                || principal.getRole() == UserRole.GESTOR
                ? principal.getId()
                : null;

        List<Simulacao> simulacoes = usuarioFiltro == null
                ? simulacaoRepository.findAllByOrderByDataSimulacaoDescCreatedAtDesc()
                : simulacaoRepository.findByUsuarioIdOrderByDataSimulacaoDescCreatedAtDesc(usuarioFiltro);

        List<Squad> squads = squadRepository.findAllForDashboard(usuarioFiltro);

        BigDecimal totalHoras = BigDecimal.ZERO;
        BigDecimal totalUst = BigDecimal.ZERO;
        BigDecimal valorTotal = BigDecimal.ZERO;

        List<DashboardProjetoItem> ustPorProjeto = new ArrayList<>();
        Map<UUID, PerfilAcumulador> perfilMap = new HashMap<>();
        Map<ProjetoTipo, TipoAcumulador> tipoMap = new HashMap<>();
        Map<UUID, SimulacaoAcumulador> simulacaoMap = new HashMap<>();

        for (Simulacao simulacao : simulacoes) {
            simulacaoMap.put(simulacao.getId(), new SimulacaoAcumulador(simulacao));
        }

        long totalProjetos = 0;
        for (Projeto projeto : projetoRepository.findAllAcessiveis(usuarioFiltro)) {
            totalProjetos++;
            SimulacaoAcumulador simAcc = simulacaoMap.get(projeto.getSimulacao().getId());
            if (simAcc != null) {
                simAcc.projetos++;
            }
        }

        for (Squad squad : squads) {
            Projeto projeto = squad.getProjeto();
            Simulacao simulacao = projeto.getSimulacao();

            BigDecimal squadHoras = zeroIfNull(squad.getTotalHoras());
            BigDecimal squadUst = zeroIfNull(squad.getTotalUst());
            BigDecimal squadValor = zeroIfNull(squad.getValorTotal());

            totalHoras = totalHoras.add(squadHoras);
            totalUst = totalUst.add(squadUst);
            valorTotal = valorTotal.add(squadValor);

            ustPorProjeto.add(DashboardProjetoItem.builder()
                    .projetoId(projeto.getId())
                    .projetoNome(projeto.getNome())
                    .tipo(projeto.getTipo())
                    .simulacaoNome(simulacao.getNomeCompleto())
                    .totalUst(squadUst)
                    .valorTotal(squadValor)
                    .build());

            TipoAcumulador tipoAcc = tipoMap.computeIfAbsent(projeto.getTipo(), TipoAcumulador::new);
            tipoAcc.projetos++;
            tipoAcc.totalUst = tipoAcc.totalUst.add(squadUst);
            tipoAcc.valorTotal = tipoAcc.valorTotal.add(squadValor);

            SimulacaoAcumulador simAcc = simulacaoMap.get(simulacao.getId());
            if (simAcc != null) {
                simAcc.totalUst = simAcc.totalUst.add(squadUst);
                simAcc.valorTotal = simAcc.valorTotal.add(squadValor);
            }

            for (SquadMembro membro : squad.getMembros()) {
                UUID perfilId = membro.getPerfil().getId();
                PerfilAcumulador perfilAcc = perfilMap.computeIfAbsent(perfilId, id -> new PerfilAcumulador(
                        id, membro.getPerfil().getNome()
                ));
                perfilAcc.quantidade += membro.getQuantidade();
                perfilAcc.totalHoras = perfilAcc.totalHoras.add(zeroIfNull(membro.getHorasCalculadas()));
                perfilAcc.totalUst = perfilAcc.totalUst.add(zeroIfNull(membro.getUstCalculada()));
            }
        }

        ustPorProjeto.sort(Comparator.comparing(DashboardProjetoItem::getTotalUst).reversed());

        List<DashboardPerfilItem> distribuicaoPorPerfil = perfilMap.values().stream()
                .map(acc -> DashboardPerfilItem.builder()
                        .perfilId(acc.perfilId)
                        .perfilNome(acc.perfilNome)
                        .quantidadeProfissionais(acc.quantidade)
                        .totalHoras(acc.totalHoras)
                        .totalUst(acc.totalUst)
                        .build())
                .sorted(Comparator.comparing(DashboardPerfilItem::getTotalUst).reversed())
                .toList();

        List<DashboardTipoItem> valorPorTipo = tipoMap.values().stream()
                .map(acc -> DashboardTipoItem.builder()
                        .tipo(acc.tipo)
                        .quantidadeProjetos(acc.projetos)
                        .totalUst(acc.totalUst)
                        .valorTotal(acc.valorTotal)
                        .build())
                .sorted(Comparator.comparing(DashboardTipoItem::getValorTotal).reversed())
                .toList();

        List<DashboardSimulacaoItem> simulacoesRecentes = simulacaoMap.values().stream()
                .map(SimulacaoAcumulador::toItem)
                .limit(10)
                .toList();

        return DashboardResponse.builder()
                .totalSimulacoes(simulacaoRepository.countAcessiveis(usuarioFiltro))
                .totalProjetos(totalProjetos)
                .totalSquads(squads.size())
                .totalHoras(totalHoras)
                .totalUst(totalUst)
                .valorTotal(valorTotal)
                .ustPorProjeto(ustPorProjeto)
                .distribuicaoPorPerfil(distribuicaoPorPerfil)
                .valorPorTipo(valorPorTipo)
                .simulacoesRecentes(simulacoesRecentes)
                .build();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private UserPrincipal getUsuarioLogado() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private static class PerfilAcumulador {
        private final UUID perfilId;
        private final String perfilNome;
        private int quantidade;
        private BigDecimal totalHoras = BigDecimal.ZERO;
        private BigDecimal totalUst = BigDecimal.ZERO;

        private PerfilAcumulador(UUID perfilId, String perfilNome) {
            this.perfilId = perfilId;
            this.perfilNome = perfilNome;
        }
    }

    private static class TipoAcumulador {
        private final ProjetoTipo tipo;
        private long projetos;
        private BigDecimal totalUst = BigDecimal.ZERO;
        private BigDecimal valorTotal = BigDecimal.ZERO;

        private TipoAcumulador(ProjetoTipo tipo) {
            this.tipo = tipo;
        }
    }

    private static class SimulacaoAcumulador {
        private final Simulacao simulacao;
        private long projetos;
        private BigDecimal totalUst = BigDecimal.ZERO;
        private BigDecimal valorTotal = BigDecimal.ZERO;

        private SimulacaoAcumulador(Simulacao simulacao) {
            this.simulacao = simulacao;
        }

        private DashboardSimulacaoItem toItem() {
            return DashboardSimulacaoItem.builder()
                    .id(simulacao.getId())
                    .nomeCompleto(simulacao.getNomeCompleto())
                    .orgao(simulacao.getOrgao())
                    .dataSimulacao(simulacao.getDataSimulacao())
                    .status(simulacao.getStatus())
                    .totalProjetos(projetos)
                    .totalUst(totalUst)
                    .valorTotal(valorTotal)
                    .build();
        }
    }
}
