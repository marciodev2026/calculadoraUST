package br.gov.ust.calculator.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.gov.ust.calculator.dto.request.RelatorioEmailRequest;
import br.gov.ust.calculator.dto.response.RelatorioEnvioResponse;
import br.gov.ust.calculator.dto.response.RelatorioPreviewResponse;
import br.gov.ust.calculator.dto.response.RelatorioResponse;
import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import br.gov.ust.calculator.service.RelatorioService;
import br.gov.ust.calculator.service.relatorio.RelatorioExportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/simulacoes/{simulacaoId}/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Geração e download de relatórios PDF e Excel")
@SecurityRequirement(name = "bearerAuth")
public class RelatorioController {

    private static final String EXCEL_MEDIA_TYPE_VALUE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(EXCEL_MEDIA_TYPE_VALUE);

    private final RelatorioService relatorioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Listar relatórios da simulação")
    public ResponseEntity<List<RelatorioResponse>> listar(@PathVariable UUID simulacaoId) {
        return ResponseEntity.ok(relatorioService.listarPorSimulacao(simulacaoId));
    }

    @GetMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Visualizar dados do relatório em tela")
    public ResponseEntity<RelatorioPreviewResponse> preview(@PathVariable UUID simulacaoId) {
        return ResponseEntity.ok(relatorioService.carregarPreview(simulacaoId));
    }

    @PostMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Gerar e baixar relatório executivo PDF")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable UUID simulacaoId) {
        return exportar(relatorioService.gerarExportacao(simulacaoId, RelatorioTipo.PDF));
    }

    @PostMapping(value = "/excel", produces = EXCEL_MEDIA_TYPE_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Gerar e baixar relatório técnico Excel")
    public ResponseEntity<byte[]> gerarExcel(@PathVariable UUID simulacaoId) {
        return exportar(relatorioService.gerarExportacao(simulacaoId, RelatorioTipo.EXCEL));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Download do relatório gerado")
    public ResponseEntity<byte[]> download(
            @PathVariable UUID simulacaoId,
            @PathVariable UUID id
    ) {
        return exportar(relatorioService.obterArquivoDownload(simulacaoId, id));
    }

    @GetMapping("/envios")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA', 'CONSULTA')")
    @Operation(summary = "Listar envios de relatório por e-mail")
    public ResponseEntity<List<RelatorioEnvioResponse>> listarEnvios(@PathVariable UUID simulacaoId) {
        return ResponseEntity.ok(relatorioService.listarEnviosPorSimulacao(simulacaoId));
    }

    @PostMapping("/email")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'ANALISTA')")
    @Operation(summary = "Enviar relatório por e-mail")
    public ResponseEntity<RelatorioEnvioResponse> enviarEmail(
            @PathVariable UUID simulacaoId,
            @Valid @RequestBody RelatorioEmailRequest request
    ) {
        return ResponseEntity.ok(relatorioService.enviarPorEmail(simulacaoId, request));
    }

    private ResponseEntity<byte[]> exportar(RelatorioExportResult export) {
        MediaType mediaType = export.getTipo() == RelatorioTipo.PDF
                ? MediaType.APPLICATION_PDF
                : EXCEL_MEDIA_TYPE;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(export.getNomeArquivo()))
                .contentLength(export.getConteudo().length)
                .body(export.getConteudo());
    }

    private String contentDisposition(String nomeArquivo) {
        return "attachment; filename=\"" + nomeArquivo + "\"";
    }
}
