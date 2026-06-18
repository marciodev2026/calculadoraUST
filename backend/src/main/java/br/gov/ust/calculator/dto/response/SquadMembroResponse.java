package br.gov.ust.calculator.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SquadMembroResponse {

    private UUID id;
    private UUID perfilId;
    private String perfilNome;
    private Integer quantidade;
    private BigDecimal fcpAplicado;
    private BigDecimal horasCalculadas;
    private BigDecimal ustCalculada;
}
