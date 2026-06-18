package br.gov.ust.calculator.dto.request;

import br.gov.ust.calculator.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest {

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    private String senha;

    private String orgao;

    private String departamento;

    private String telefone;

    @NotNull(message = "Perfil de acesso é obrigatório")
    private UserRole role;

    private Boolean ativo;
}
