package br.gov.ust.calculator.mapper;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.RelatorioPreviewResponse;
import br.gov.ust.calculator.entity.ConfiguracaoInstitucional;
import br.gov.ust.calculator.entity.SquadMembro;
import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import br.gov.ust.calculator.service.relatorio.RelatorioConteudo;

@Component
public class RelatorioPreviewMapper {

    private static final Map<ProjetoTipo, String> TIPO_LABELS = Map.of(
            ProjetoTipo.PROJETO, "Projeto",
            ProjetoTipo.SUSTENTACAO, "Sustentação",
            ProjetoTipo.EVOLUCAO, "Evolução",
            ProjetoTipo.CORRECAO, "Correção"
    );

    public RelatorioPreviewResponse toPreview(RelatorioConteudo conteudo, ConfiguracaoInstitucional institucional) {
        List<RelatorioPreviewResponse.GrupoTipoResumo> grupos = new ArrayList<>();
        Map<ProjetoTipo, List<RelatorioConteudo.ProjetoRelatorioLinha>> porTipo = new EnumMap<>(ProjetoTipo.class);

        for (RelatorioConteudo.ProjetoRelatorioLinha linha : conteudo.getProjetos()) {
            porTipo.computeIfAbsent(linha.getProjeto().getTipo(), k -> new ArrayList<>()).add(linha);
        }

        for (ProjetoTipo tipo : ProjetoTipo.values()) {
            List<RelatorioConteudo.ProjetoRelatorioLinha> linhas = porTipo.getOrDefault(tipo, List.of());
            if (linhas.isEmpty()) {
                continue;
            }

            BigDecimal subHoras = BigDecimal.ZERO;
            BigDecimal subUst = BigDecimal.ZERO;
            BigDecimal subValor = BigDecimal.ZERO;
            List<RelatorioPreviewResponse.ProjetoResumo> projetos = new ArrayList<>();

            for (RelatorioConteudo.ProjetoRelatorioLinha linha : linhas) {
                subHoras = subHoras.add(zeroIfNull(linha.getTotalHoras()));
                subUst = subUst.add(zeroIfNull(linha.getTotalUst()));
                subValor = subValor.add(zeroIfNull(linha.getValorTotal()));
                projetos.add(toProjetoResumo(linha));
            }

            grupos.add(RelatorioPreviewResponse.GrupoTipoResumo.builder()
                    .tipo(tipo)
                    .tipoLabel(TIPO_LABELS.get(tipo))
                    .subtotais(RelatorioPreviewResponse.TotaisResumo.builder()
                            .totalProjetos(projetos.size())
                            .totalHoras(subHoras)
                            .totalUst(subUst)
                            .valorTotal(subValor)
                            .build())
                    .projetos(projetos)
                    .build());
        }

        return RelatorioPreviewResponse.builder()
                .simulacao(RelatorioPreviewResponse.SimulacaoResumo.builder()
                        .id(conteudo.getSimulacao().getId())
                        .nomeCompleto(conteudo.getSimulacao().getNomeCompleto())
                        .email(conteudo.getSimulacao().getEmail())
                        .orgao(conteudo.getSimulacao().getOrgao())
                        .departamento(conteudo.getSimulacao().getDepartamento())
                        .telefone(conteudo.getSimulacao().getTelefone())
                        .dataSimulacao(conteudo.getSimulacao().getDataSimulacao())
                        .status(conteudo.getSimulacao().getStatus())
                        .build())
                .configuracao(RelatorioPreviewResponse.ConfiguracaoUstResumo.builder()
                        .valorUst(conteudo.getConfiguracao().getValorUst())
                        .cargaHorariaMes(conteudo.getConfiguracao().getCargaHorariaMes())
                        .encargosPercentual(conteudo.getConfiguracao().getEncargosPercentual())
                        .bdiPercentual(conteudo.getConfiguracao().getBdiPercentual())
                        .vigenteDesde(conteudo.getConfiguracao().getVigenteDesde())
                        .build())
                .institucional(RelatorioPreviewResponse.InstitucionalResumo.builder()
                        .nomeOrganizacao(institucional.getNomeOrganizacao())
                        .possuiLogo(possuiLogo(institucional))
                        .build())
                .totais(RelatorioPreviewResponse.TotaisResumo.builder()
                        .totalProjetos(conteudo.getProjetos().size())
                        .totalHoras(conteudo.getTotalHoras())
                        .totalUst(conteudo.getTotalUst())
                        .valorTotal(conteudo.getValorTotal())
                        .build())
                .grupos(grupos)
                .build();
    }

    private RelatorioPreviewResponse.ProjetoResumo toProjetoResumo(RelatorioConteudo.ProjetoRelatorioLinha linha) {
        List<RelatorioPreviewResponse.MembroResumo> membros = linha.getMembros().stream()
                .map(this::toMembroResumo)
                .toList();

        return RelatorioPreviewResponse.ProjetoResumo.builder()
                .id(linha.getProjeto().getId())
                .nome(linha.getProjeto().getNome())
                .tipo(linha.getProjeto().getTipo())
                .semanas(linha.getProjeto().getSemanas())
                .horasSemanais(linha.getProjeto().getHorasSemanais())
                .totalHoras(linha.getTotalHoras())
                .totalUst(linha.getTotalUst())
                .valorTotal(linha.getValorTotal())
                .membros(membros)
                .build();
    }

    private RelatorioPreviewResponse.MembroResumo toMembroResumo(SquadMembro membro) {
        String perfilNome = membro.getPerfil() != null ? membro.getPerfil().getNome() : "—";
        return RelatorioPreviewResponse.MembroResumo.builder()
                .perfilNome(perfilNome)
                .quantidade(membro.getQuantidade())
                .fcpAplicado(membro.getFcpAplicado())
                .horasCalculadas(membro.getHorasCalculadas())
                .ustCalculada(membro.getUstCalculada())
                .build();
    }

    private boolean possuiLogo(ConfiguracaoInstitucional institucional) {
        if (institucional.getLogoCaminho() == null || institucional.getLogoCaminho().isBlank()) {
            return false;
        }
        return Files.exists(Path.of(institucional.getLogoCaminho()));
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
