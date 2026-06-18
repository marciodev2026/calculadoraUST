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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ust.calculator.dto.request.PerfilRequest;
import br.gov.ust.calculator.dto.response.PerfilResponse;
import br.gov.ust.calculator.service.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
@Tag(name = "Perfis Profissionais", description = "Gestão de perfis e FCP")
@SecurityRequirement(name = "bearerAuth")
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Listar perfis profissionais")
    public ResponseEntity<List<PerfilResponse>> listar(
            @RequestParam(defaultValue = "false") boolean apenasAtivos
    ) {
        List<PerfilResponse> perfis = apenasAtivos
                ? perfilService.listarAtivos()
                : perfilService.listarTodos();
        return ResponseEntity.ok(perfis);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Buscar perfil por ID")
    public ResponseEntity<PerfilResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(perfilService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar perfil profissional")
    public ResponseEntity<PerfilResponse> criar(@Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar perfil profissional")
    public ResponseEntity<PerfilResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PerfilRequest request
    ) {
        return ResponseEntity.ok(perfilService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir perfil profissional")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        perfilService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
