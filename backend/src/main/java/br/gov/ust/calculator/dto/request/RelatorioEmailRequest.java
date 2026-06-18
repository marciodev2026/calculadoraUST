package br.gov.ust.calculator.dto.request;

import java.util.UUID;

import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelatorioEmailRequest {

    @NotBlank(message = "Destinatário é obrigatório")
    @Email(message = "E-mail do destinatário inválido")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    private String destinatario;

    @NotNull(message = "Tipo do relatório é obrigatório")
    private RelatorioTipo tipo;

    private UUID relatorioId;

    @Size(max = 200, message = "Assunto deve ter no máximo 200 caracteres")
    private String assunto;

    @Size(max = 2000, message = "Mensagem deve ter no máximo 2000 caracteres")
    private String mensagem;
}
