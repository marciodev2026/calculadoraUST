package br.gov.ust.calculator.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.gov.ust.calculator.dto.request.ConfiguracaoInstitucionalRequest;
import br.gov.ust.calculator.dto.response.ConfiguracaoInstitucionalResponse;
import br.gov.ust.calculator.service.ConfiguracaoInstitucionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/configuracoes/institucional")
@RequiredArgsConstructor
@Tag(name = "Configuração Institucional", description = "Logo e nome do órgão")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracaoInstitucionalController {

    private final ConfiguracaoInstitucionalService configuracaoInstitucionalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Buscar configuração institucional")
    public ResponseEntity<ConfiguracaoInstitucionalResponse> buscar() {
        return ResponseEntity.ok(configuracaoInstitucionalService.buscar());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Salvar nome do órgão")
    public ResponseEntity<ConfiguracaoInstitucionalResponse> salvar(
            @Valid @RequestBody ConfiguracaoInstitucionalRequest request
    ) {
        return ResponseEntity.ok(configuracaoInstitucionalService.salvar(request));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enviar logo do órgão")
    public ResponseEntity<ConfiguracaoInstitucionalResponse> uploadLogo(
            @RequestPart("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.ok(configuracaoInstitucionalService.uploadLogo(arquivo));
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remover logo do órgão")
    public ResponseEntity<ConfiguracaoInstitucionalResponse> removerLogo() {
        return ResponseEntity.ok(configuracaoInstitucionalService.removerLogo());
    }

    @GetMapping("/logo")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Obter logo do órgão")
    public ResponseEntity<Resource> obterLogo() {
        Resource resource = configuracaoInstitucionalService.carregarLogo();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(configuracaoInstitucionalService.obterTipoConteudoLogo()))
                .body(resource);
    }
}
