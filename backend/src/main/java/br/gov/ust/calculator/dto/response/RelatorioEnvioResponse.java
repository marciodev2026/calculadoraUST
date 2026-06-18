package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.RelatorioEnvioStatus;
import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelatorioEnvioResponse {

    private UUID id;
    private UUID simulacaoId;
    private UUID relatorioId;
    private String destinatario;
    private String assunto;
    private RelatorioTipo tipo;
    private RelatorioEnvioStatus status;
    private String mensagemErro;
    private String nomeArquivo;
    private LocalDateTime enviadoEm;
}
