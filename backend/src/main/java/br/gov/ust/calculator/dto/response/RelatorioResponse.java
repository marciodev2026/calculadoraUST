package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelatorioResponse {

    private UUID id;
    private UUID simulacaoId;
    private RelatorioTipo tipo;
    private String nomeArquivo;
    private Long tamanhoBytes;
    private LocalDateTime geradoEm;
}
