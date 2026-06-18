package br.gov.ust.calculator.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ust.calculator.dto.request.SquadRequest;
import br.gov.ust.calculator.dto.response.SquadResponse;
import br.gov.ust.calculator.service.SquadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/simulacoes/{simulacaoId}/projetos/{projetoId}/squad")
@RequiredArgsConstructor
@Tag(name = "Squads", description = "Montagem de squads e cálculo UST")
@SecurityRequirement(name = "bearerAuth")
public class SquadController {

    private final SquadService squadService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Buscar squad do projeto")
    public ResponseEntity<SquadResponse> buscar(
            @PathVariable UUID simulacaoId,
            @PathVariable UUID projetoId
    ) {
        return ResponseEntity.ok(squadService.buscarPorProjeto(simulacaoId, projetoId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Salvar composição da squad e recalcular")
    public ResponseEntity<SquadResponse> salvar(
            @PathVariable UUID simulacaoId,
            @PathVariable UUID projetoId,
            @Valid @RequestBody SquadRequest request
    ) {
        return ResponseEntity.ok(squadService.salvar(simulacaoId, projetoId, request));
    }
}
