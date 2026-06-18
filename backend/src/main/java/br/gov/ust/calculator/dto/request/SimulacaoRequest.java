package br.gov.ust.calculator.dto.request;

import java.time.LocalDate;

import br.gov.ust.calculator.entity.enums.SimulacaoStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulacaoRequest {

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "Órgão é obrigatório")
    @Size(max = 200, message = "Órgão deve ter no máximo 200 caracteres")
    private String orgao;

    @NotBlank(message = "Departamento é obrigatório")
    @Size(max = 200, message = "Departamento deve ter no máximo 200 caracteres")
    private String departamento;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @NotNull(message = "Data da simulação é obrigatória")
    private LocalDate dataSimulacao;

    private SimulacaoStatus status;
}
