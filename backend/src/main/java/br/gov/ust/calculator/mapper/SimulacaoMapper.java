package br.gov.ust.calculator.mapper;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.SimulacaoResponse;
import br.gov.ust.calculator.entity.Simulacao;

@Component
public class SimulacaoMapper {

    public SimulacaoResponse toResponse(Simulacao simulacao) {
        return SimulacaoResponse.builder()
                .id(simulacao.getId())
                .usuarioId(simulacao.getUsuario().getId())
                .usuarioNome(simulacao.getUsuario().getNomeCompleto())
                .nomeCompleto(simulacao.getNomeCompleto())
                .email(simulacao.getEmail())
                .orgao(simulacao.getOrgao())
                .departamento(simulacao.getDepartamento())
                .telefone(simulacao.getTelefone())
                .dataSimulacao(simulacao.getDataSimulacao())
                .status(simulacao.getStatus())
                .createdAt(simulacao.getCreatedAt())
                .updatedAt(simulacao.getUpdatedAt())
                .build();
    }
}
