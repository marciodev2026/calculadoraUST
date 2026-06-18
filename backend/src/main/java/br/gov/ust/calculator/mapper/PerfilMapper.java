package br.gov.ust.calculator.mapper;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.PerfilResponse;
import br.gov.ust.calculator.entity.Perfil;

@Component
public class PerfilMapper {

    public PerfilResponse toResponse(Perfil perfil) {
        return PerfilResponse.builder()
                .id(perfil.getId())
                .nome(perfil.getNome())
                .descricao(perfil.getDescricao())
                .fcp(perfil.getFcp())
                .ativo(perfil.getAtivo())
                .createdAt(perfil.getCreatedAt())
                .updatedAt(perfil.getUpdatedAt())
                .build();
    }
}
