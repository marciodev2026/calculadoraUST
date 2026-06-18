package br.gov.ust.calculator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private boolean enabled = true;

    private String from = "UST Gov <noreply@ust.gov.br>";

    private String mailpitUiUrl = "http://localhost:8025";
}
