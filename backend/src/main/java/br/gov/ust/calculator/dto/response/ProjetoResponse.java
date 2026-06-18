package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.ProjetoStatus;
import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjetoResponse {

    private UUID id;
    private UUID simulacaoId;
    private String nome;
    private ProjetoTipo tipo;
    private Integer semanas;
    private Integer horasSemanais;
    private String descricao;
    private ProjetoStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
