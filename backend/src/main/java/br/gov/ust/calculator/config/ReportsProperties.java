package br.gov.ust.calculator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.reports")
public class ReportsProperties {

    private String storagePath = "./storage/relatorios";
}
