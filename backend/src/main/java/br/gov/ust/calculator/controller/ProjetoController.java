package br.gov.ust.calculator.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ust.calculator.dto.request.ProjetoRequest;
import br.gov.ust.calculator.dto.response.ProjetoResponse;
import br.gov.ust.calculator.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/simulacoes/{simulacaoId}/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Projetos e sustentações vinculados à simulação")
@SecurityRequirement(name = "bearerAuth")
public class ProjetoController {

    private final ProjetoService projetoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Listar projetos da simulação")
    public ResponseEntity<List<ProjetoResponse>> listar(@PathVariable UUID simulacaoId) {
        return ResponseEntity.ok(projetoService.listarPorSimulacao(simulacaoId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Buscar projeto por ID")
    public ResponseEntity<ProjetoResponse> buscar(
            @PathVariable UUID simulacaoId,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(projetoService.buscarPorId(simulacaoId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Criar projeto")
    public ResponseEntity<ProjetoResponse> criar(
            @PathVariable UUID simulacaoId,
            @Valid @RequestBody ProjetoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.criar(simulacaoId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Atualizar projeto")
    public ResponseEntity<ProjetoResponse> atualizar(
            @PathVariable UUID simulacaoId,
            @PathVariable UUID id,
            @Valid @RequestBody ProjetoRequest request
    ) {
        return ResponseEntity.ok(projetoService.atualizar(simulacaoId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Excluir projeto")
    public ResponseEntity<Void> excluir(
            @PathVariable UUID simulacaoId,
            @PathVariable UUID id
    ) {
        projetoService.excluir(simulacaoId, id);
        return ResponseEntity.noContent().build();
    }
}
