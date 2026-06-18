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

import br.gov.ust.calculator.dto.request.SimulacaoRequest;
import br.gov.ust.calculator.dto.response.SimulacaoResponse;
import br.gov.ust.calculator.service.SimulacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/simulacoes")
@RequiredArgsConstructor
@Tag(name = "Simulações", description = "Cadastro e histórico de simulações UST")
@SecurityRequirement(name = "bearerAuth")
public class SimulacaoController {

    private final SimulacaoService simulacaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Listar simulações")
    public ResponseEntity<List<SimulacaoResponse>> listar() {
        return ResponseEntity.ok(simulacaoService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Buscar simulação por ID")
    public ResponseEntity<SimulacaoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(simulacaoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Criar simulação")
    public ResponseEntity<SimulacaoResponse> criar(@Valid @RequestBody SimulacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(simulacaoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Atualizar simulação")
    public ResponseEntity<SimulacaoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody SimulacaoRequest request
    ) {
        return ResponseEntity.ok(simulacaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Excluir simulação")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        simulacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
