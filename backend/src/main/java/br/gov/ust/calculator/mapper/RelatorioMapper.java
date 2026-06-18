package br.gov.ust.calculator.mapper;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.RelatorioResponse;
import br.gov.ust.calculator.entity.Relatorio;

@Component
public class RelatorioMapper {

    public RelatorioResponse toResponse(Relatorio relatorio) {
        return RelatorioResponse.builder()
                .id(relatorio.getId())
                .simulacaoId(relatorio.getSimulacao().getId())
                .tipo(relatorio.getTipo())
                .nomeArquivo(relatorio.getNomeArquivo())
                .tamanhoBytes(relatorio.getTamanhoBytes())
                .geradoEm(relatorio.getGeradoEm())
                .build();
    }
}
