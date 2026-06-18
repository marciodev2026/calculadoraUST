package br.gov.ust.calculator.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SquadRequest {

    @NotNull(message = "Lista de membros é obrigatória")
    @Valid
    private List<SquadMembroRequest> membros = new ArrayList<>();
}
