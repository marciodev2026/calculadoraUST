package br.gov.ust.calculator.dto.request;

import br.gov.ust.calculator.entity.enums.ProjetoStatus;
import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjetoRequest {

    @NotBlank(message = "Nome do projeto é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;

    @NotNull(message = "Tipo é obrigatório")
    private ProjetoTipo tipo;

    @NotNull(message = "Semanas é obrigatório")
    @Min(value = 1, message = "Semanas deve ser no mínimo 1")
    private Integer semanas;

    @Min(value = 1, message = "Horas semanais deve ser no mínimo 1")
    private Integer horasSemanais;

    private String descricao;

    private ProjetoStatus status;
}
