package br.gov.ust.calculator.mapper;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.RelatorioEnvioResponse;
import br.gov.ust.calculator.entity.RelatorioEnvio;

@Component
public class RelatorioEnvioMapper {

    public RelatorioEnvioResponse toResponse(RelatorioEnvio envio) {
        return RelatorioEnvioResponse.builder()
                .id(envio.getId())
                .simulacaoId(envio.getSimulacao().getId())
                .relatorioId(envio.getRelatorio() != null ? envio.getRelatorio().getId() : null)
                .destinatario(envio.getDestinatario())
                .assunto(envio.getAssunto())
                .tipo(envio.getTipo())
                .status(envio.getStatus())
                .mensagemErro(envio.getMensagemErro())
                .nomeArquivo(envio.getRelatorio() != null ? envio.getRelatorio().getNomeArquivo() : null)
                .enviadoEm(envio.getEnviadoEm())
                .build();
    }
}
