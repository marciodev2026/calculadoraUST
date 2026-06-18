package br.gov.ust.calculator.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import br.gov.ust.calculator.dto.response.ConfiguracaoUstResponse;
import br.gov.ust.calculator.entity.Configuracao;

@Component
public class ConfiguracaoMapper {

    public ConfiguracaoUstResponse toResponse(Configuracao configuracao) {
        return ConfiguracaoUstResponse.builder()
                .id(configuracao.getId())
                .valorUst(configuracao.getValorUst())
                .cargaHorariaMes(configuracao.getCargaHorariaMes())
                .encargosPercentual(configuracao.getEncargosPercentual())
                .bdiPercentual(configuracao.getBdiPercentual())
                .vigenteDesde(configuracao.getVigenteDesde())
                .ativo(configuracao.getAtivo())
                .multiplicadorFinanceiro(calcularMultiplicador(configuracao))
                .createdAt(configuracao.getCreatedAt())
                .updatedAt(configuracao.getUpdatedAt())
                .build();
    }

    private BigDecimal calcularMultiplicador(Configuracao configuracao) {
        BigDecimal encargos = BigDecimal.ONE.add(
                configuracao.getEncargosPercentual().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        BigDecimal bdi = BigDecimal.ONE.add(
                configuracao.getBdiPercentual().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        return configuracao.getValorUst().multiply(encargos).multiply(bdi).setScale(2, RoundingMode.HALF_UP);
    }
}
