package br.gov.ust.calculator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ust.calculator.dto.request.ConfiguracaoUstRequest;
import br.gov.ust.calculator.dto.response.ConfiguracaoUstResponse;
import br.gov.ust.calculator.service.ConfiguracaoUstService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/configuracoes/ust")
@RequiredArgsConstructor
@Tag(name = "Configuração UST", description = "Parâmetros financeiros e de carga horária")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracaoUstController {

    private final ConfiguracaoUstService configuracaoUstService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Buscar configuração UST ativa")
    public ResponseEntity<ConfiguracaoUstResponse> buscarAtiva() {
        return ResponseEntity.ok(configuracaoUstService.buscarAtiva());
    }

    @GetMapping("/historico")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar histórico de configurações UST")
    public ResponseEntity<List<ConfiguracaoUstResponse>> listarHistorico() {
        return ResponseEntity.ok(configuracaoUstService.listarHistorico());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Salvar nova configuração UST")
    public ResponseEntity<ConfiguracaoUstResponse> salvar(@Valid @RequestBody ConfiguracaoUstRequest request) {
        return ResponseEntity.ok(configuracaoUstService.salvar(request));
    }
}
