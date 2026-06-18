package br.gov.ust.calculator.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfiguracaoUstRequest {

    @NotNull(message = "Valor da UST é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor da UST deve ser maior que zero")
    private BigDecimal valorUst;

    @NotNull(message = "Carga horária mensal é obrigatória")
    @Min(value = 1, message = "Carga horária deve ser no mínimo 1 hora")
    private Integer cargaHorariaMes;

    @NotNull(message = "Encargos é obrigatório")
    @DecimalMin(value = "0.00", message = "Encargos não pode ser negativo")
    private BigDecimal encargosPercentual;

    @NotNull(message = "BDI é obrigatório")
    @DecimalMin(value = "0.00", message = "BDI não pode ser negativo")
    private BigDecimal bdiPercentual;

    private LocalDate vigenteDesde;
}
