package br.gov.ust.calculator.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.SimulacaoStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulacaoResponse {

    private UUID id;
    private UUID usuarioId;
    private String usuarioNome;
    private String nomeCompleto;
    private String email;
    private String orgao;
    private String departamento;
    private String telefone;
    private LocalDate dataSimulacao;
    private SimulacaoStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
