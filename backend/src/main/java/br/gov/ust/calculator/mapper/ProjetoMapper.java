package br.gov.ust.calculator.mapper;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.ProjetoResponse;
import br.gov.ust.calculator.entity.Projeto;

@Component
public class ProjetoMapper {

    public ProjetoResponse toResponse(Projeto projeto) {
        return ProjetoResponse.builder()
                .id(projeto.getId())
                .simulacaoId(projeto.getSimulacao().getId())
                .nome(projeto.getNome())
                .tipo(projeto.getTipo())
                .semanas(projeto.getSemanas())
                .horasSemanais(projeto.getHorasSemanais())
                .descricao(projeto.getDescricao())
                .status(projeto.getStatus())
                .createdAt(projeto.getCreatedAt())
                .updatedAt(projeto.getUpdatedAt())
                .build();
    }
}
