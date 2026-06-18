package br.gov.ust.calculator.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MailConfigResponse {

    private boolean enabled;
    private String mailpitUiUrl;
}
