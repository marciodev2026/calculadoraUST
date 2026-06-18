package br.gov.ust.calculator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfiguracaoInstitucionalRequest {

    @NotBlank(message = "Nome do órgão é obrigatório")
    @Size(max = 200, message = "Nome do órgão deve ter no máximo 200 caracteres")
    private String nomeOrganizacao;
}
