package br.gov.ust.calculator.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.SquadMembroResponse;
import br.gov.ust.calculator.dto.response.SquadResponse;
import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Squad;
import br.gov.ust.calculator.entity.SquadMembro;

@Component
public class SquadMapper {

    public SquadResponse toResponse(Squad squad, Projeto projeto, Configuracao configuracao) {
        List<SquadMembroResponse> membros = squad.getMembros().stream()
                .map(this::toMembroResponse)
                .toList();

        return SquadResponse.builder()
                .id(squad.getId())
                .projetoId(projeto.getId())
                .projetoNome(projeto.getNome())
                .semanas(projeto.getSemanas())
                .horasSemanais(projeto.getHorasSemanais())
                .totalHoras(squad.getTotalHoras())
                .totalUst(squad.getTotalUst())
                .valorTotal(squad.getValorTotal())
                .valorUst(configuracao.getValorUst())
                .encargosPercentual(configuracao.getEncargosPercentual())
                .bdiPercentual(configuracao.getBdiPercentual())
                .membros(membros)
                .build();
    }

    public SquadResponse toEmptyResponse(Projeto projeto, Configuracao configuracao) {
        return SquadResponse.builder()
                .projetoId(projeto.getId())
                .projetoNome(projeto.getNome())
                .semanas(projeto.getSemanas())
                .horasSemanais(projeto.getHorasSemanais())
                .valorUst(configuracao.getValorUst())
                .encargosPercentual(configuracao.getEncargosPercentual())
                .bdiPercentual(configuracao.getBdiPercentual())
                .membros(List.of())
                .build();
    }

    private SquadMembroResponse toMembroResponse(SquadMembro membro) {
        return SquadMembroResponse.builder()
                .id(membro.getId())
                .perfilId(membro.getPerfil().getId())
                .perfilNome(membro.getPerfil().getNome())
                .quantidade(membro.getQuantidade())
                .fcpAplicado(membro.getFcpAplicado())
                .horasCalculadas(membro.getHorasCalculadas())
                .ustCalculada(membro.getUstCalculada())
                .build();
    }
}
