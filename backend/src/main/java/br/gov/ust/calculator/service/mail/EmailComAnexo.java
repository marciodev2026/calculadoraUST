package br.gov.ust.calculator.service.mail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailComAnexo {

    private final String destinatario;
    private final String assunto;
    private final String corpoHtml;
    private final String nomeAnexo;
    private final byte[] anexo;
    private final String mimeAnexo;
    private final byte[] logoInline;
    private final String logoMime;
}
