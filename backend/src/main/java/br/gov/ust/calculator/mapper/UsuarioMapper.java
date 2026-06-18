package br.gov.ust.calculator.mapper;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.UsuarioResponse;
import br.gov.ust.calculator.entity.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nomeCompleto(usuario.getNomeCompleto())
                .email(usuario.getEmail())
                .orgao(usuario.getOrgao())
                .departamento(usuario.getDepartamento())
                .telefone(usuario.getTelefone())
                .role(usuario.getRole())
                .ativo(usuario.getAtivo())
                .createdAt(usuario.getCreatedAt())
                .build();
    }
}
