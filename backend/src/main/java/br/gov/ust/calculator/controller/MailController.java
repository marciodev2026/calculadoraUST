package br.gov.ust.calculator.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ust.calculator.config.MailProperties;
import br.gov.ust.calculator.dto.response.MailConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Tag(name = "E-mail", description = "Configuração de envio de e-mail (Mailpit)")
@SecurityRequirement(name = "bearerAuth")
public class MailController {

    private final MailProperties mailProperties;

    @GetMapping("/config")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Configuração do serviço de e-mail")
    public MailConfigResponse obterConfiguracao() {
        return MailConfigResponse.builder()
                .enabled(mailProperties.isEnabled())
                .mailpitUiUrl(mailProperties.getMailpitUiUrl())
                .build();
    }
}
