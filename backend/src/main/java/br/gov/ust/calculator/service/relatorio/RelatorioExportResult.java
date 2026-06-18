package br.gov.ust.calculator.service.relatorio;

import java.util.UUID;

import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RelatorioExportResult {

    private final UUID relatorioId;
    private final String nomeArquivo;
    private final byte[] conteudo;
    private final RelatorioTipo tipo;
}
