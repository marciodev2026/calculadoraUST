package br.gov.ust.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import br.gov.ust.calculator.config.MailProperties;
import br.gov.ust.calculator.config.ReportsProperties;
import br.gov.ust.calculator.security.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, ReportsProperties.class, MailProperties.class})
public class UstCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(UstCalculatorApplication.class, args);
    }
}
