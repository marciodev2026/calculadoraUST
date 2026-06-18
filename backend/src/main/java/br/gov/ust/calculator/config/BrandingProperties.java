package br.gov.ust.calculator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.branding")
@Getter
@Setter
public class BrandingProperties {

    private String storagePath = "./storage/branding";
}
