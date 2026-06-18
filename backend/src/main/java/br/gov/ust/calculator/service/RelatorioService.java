package br.gov.ust.calculator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.config.ReportsProperties;
import br.gov.ust.calculator.dto.request.RelatorioEmailRequest;
import br.gov.ust.calculator.dto.response.RelatorioEnvioResponse;
import br.gov.ust.calculator.dto.response.RelatorioPreviewResponse;
import br.gov.ust.calculator.dto.response.RelatorioResponse;
import br.gov.ust.calculator.entity.ConfiguracaoInstitucional;
import br.gov.ust.calculator.entity.Relatorio;
import br.gov.ust.calculator.entity.RelatorioEnvio;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.enums.RelatorioEnvioStatus;
import br.gov.ust.calculator.entity.enums.RelatorioTipo;
import br.gov.ust.calculator.exception.BusinessException;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.RelatorioEnvioMapper;
import br.gov.ust.calculator.mapper.RelatorioMapper;
import br.gov.ust.calculator.mapper.RelatorioPreviewMapper;
import br.gov.ust.calculator.repository.RelatorioEnvioRepository;
import br.gov.ust.calculator.repository.RelatorioRepository;
import br.gov.ust.calculator.security.UserPrincipal;
import br.gov.ust.calculator.service.mail.EmailComAnexo;
import br.gov.ust.calculator.service.mail.EmailService;
import br.gov.ust.calculator.service.mail.RelatorioEmailComposer;
import br.gov.ust.calculator.service.relatorio.RelatorioConteudo;
import br.gov.ust.calculator.service.relatorio.RelatorioDataService;
import br.gov.ust.calculator.service.relatorio.RelatorioExcelGenerator;
import br.gov.ust.calculator.service.relatorio.RelatorioExportResult;
import br.gov.ust.calculator.service.relatorio.RelatorioPdfGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final RelatorioRepository relatorioRepository;
    private final RelatorioEnvioRepository relatorioEnvioRepository;
    private final RelatorioMapper relatorioMapper;
    private final RelatorioEnvioMapper relatorioEnvioMapper;
    private final RelatorioPreviewMapper relatorioPreviewMapper;
    private final RelatorioDataService relatorioDataService;
    private final RelatorioPdfGenerator pdfGenerator;
    private final RelatorioExcelGenerator excelGenerator;
    private final SimulacaoService simulacaoService;
    private final ConfiguracaoInstitucionalService configuracaoInstitucionalService;
    private final ReportsProperties reportsProperties;
    private final EmailService emailService;
    private final RelatorioEmailComposer relatorioEmailComposer;

    @Transactional(readOnly = true)
    public RelatorioPreviewResponse carregarPreview(UUID simulacaoId) {
        simulacaoService.obterComAcesso(simulacaoId);
        RelatorioConteudo conteudo = relatorioDataService.carregar(simulacaoId);
        ConfiguracaoInstitucional institucional = configuracaoInstitucionalService.obterEntidade();
        return relatorioPreviewMapper.toPreview(conteudo, institucional);
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listarPorSimulacao(UUID simulacaoId) {
        simulacaoService.obterComAcesso(simulacaoId);
        return relatorioRepository.findBySimulacaoIdOrderByGeradoEmDesc(simulacaoId).stream()
                .map(relatorioMapper::toResponse)
                .toList();
    }

    @Transactional
    public RelatorioExportResult gerarExportacao(UUID simulacaoId, RelatorioTipo tipo) {
        Simulacao simulacao = simulacaoService.obterComAcesso(simulacaoId);
        simulacaoService.validarGeracaoRelatorio(simulacao);

        RelatorioConteudo conteudo = enriquecerComBranding(relatorioDataService.carregar(simulacaoId));
        byte[] bytes = tipo == RelatorioTipo.PDF
                ? pdfGenerator.gerar(conteudo)
                : excelGenerator.gerar(conteudo);

        String extensao = tipo == RelatorioTipo.PDF ? "pdf" : "xlsx";
        String nomeArquivo = "relatorio-" + tipo.name().toLowerCase() + "-"
                + simulacaoId.toString().substring(0, 8) + "-"
                + FILE_TS.format(LocalDateTime.now()) + "." + extensao;

        Path destino = salvarArquivo(simulacaoId, nomeArquivo, bytes);

        Relatorio relatorio = relatorioRepository.save(Relatorio.builder()
                .simulacao(simulacao)
                .tipo(tipo)
                .nomeArquivo(nomeArquivo)
                .caminhoArquivo(destino.toString())
                .tamanhoBytes((long) bytes.length)
                .geradoEm(LocalDateTime.now())
                .createdBy(getUsuarioLogado().getId())
                .build());

        return RelatorioExportResult.builder()
                .relatorioId(relatorio.getId())
                .nomeArquivo(nomeArquivo)
                .conteudo(bytes)
                .tipo(tipo)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RelatorioEnvioResponse> listarEnviosPorSimulacao(UUID simulacaoId) {
        simulacaoService.obterComAcesso(simulacaoId);
        return relatorioEnvioRepository.findBySimulacaoIdOrderByEnviadoEmDesc(simulacaoId).stream()
                .map(relatorioEnvioMapper::toResponse)
                .toList();
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public RelatorioEnvioResponse enviarPorEmail(UUID simulacaoId, RelatorioEmailRequest request) {
        Simulacao simulacao = simulacaoService.obterComAcesso(simulacaoId);
        simulacaoService.validarGeracaoRelatorio(simulacao);

        Relatorio relatorio;
        RelatorioExportResult export;

        if (request.getRelatorioId() != null) {
            relatorio = relatorioRepository.findByIdAndSimulacaoId(request.getRelatorioId(), simulacaoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado"));
            if (relatorio.getTipo() != request.getTipo()) {
                throw new BusinessException("O tipo informado não corresponde ao arquivo selecionado");
            }
            export = obterArquivoDownload(simulacaoId, request.getRelatorioId());
        } else {
            export = gerarExportacao(simulacaoId, request.getTipo());
            relatorio = relatorioRepository.findById(export.getRelatorioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado"));
        }

        ConfiguracaoInstitucional institucional = configuracaoInstitucionalService.obterEntidade();
        RelatorioConteudo conteudo = enriquecerComBranding(relatorioDataService.carregar(simulacaoId));

        String assunto = relatorioEmailComposer.montarAssunto(simulacao, request.getTipo(), request.getAssunto());
        boolean possuiLogo = conteudo.getLogoBytes() != null && conteudo.getLogoBytes().length > 0;
        String corpoHtml = relatorioEmailComposer.montarCorpoHtml(
                conteudo,
                request.getTipo(),
                export,
                request.getMensagem(),
                possuiLogo
        );

        String logoMime = possuiLogo
                ? relatorioEmailComposer.obterMimeLogo(institucional.getLogoCaminho())
                : null;

        RelatorioEnvio envio = RelatorioEnvio.builder()
                .simulacao(simulacao)
                .relatorio(relatorio)
                .destinatario(request.getDestinatario().trim().toLowerCase())
                .assunto(assunto)
                .tipo(request.getTipo())
                .createdBy(getUsuarioLogado().getId())
                .build();

        try {
            emailService.enviarComAnexo(EmailComAnexo.builder()
                    .destinatario(envio.getDestinatario())
                    .assunto(assunto)
                    .corpoHtml(corpoHtml)
                    .nomeAnexo(export.getNomeArquivo())
                    .anexo(export.getConteudo())
                    .mimeAnexo(relatorioEmailComposer.obterMimeAnexo(request.getTipo()))
                    .logoInline(possuiLogo ? conteudo.getLogoBytes() : null)
                    .logoMime(logoMime)
                    .build());
            envio.setStatus(RelatorioEnvioStatus.ENVIADO);
        } catch (BusinessException ex) {
            envio.setStatus(RelatorioEnvioStatus.FALHA);
            envio.setMensagemErro(ex.getMessage());
            relatorioEnvioRepository.save(envio);
            throw ex;
        }

        return relatorioEnvioMapper.toResponse(relatorioEnvioRepository.save(envio));
    }

    @Transactional(readOnly = true)
    public RelatorioExportResult obterArquivoDownload(UUID simulacaoId, UUID relatorioId) {
        simulacaoService.obterComAcesso(simulacaoId);

        Relatorio relatorio = relatorioRepository.findByIdAndSimulacaoId(relatorioId, simulacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado"));

        Path path = Path.of(relatorio.getCaminhoArquivo()).toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new ResourceNotFoundException("Arquivo do relatório não encontrado");
        }

        try {
            byte[] conteudo = Files.readAllBytes(path);
            return RelatorioExportResult.builder()
                    .relatorioId(relatorio.getId())
                    .nomeArquivo(relatorio.getNomeArquivo())
                    .conteudo(conteudo)
                    .tipo(relatorio.getTipo())
                    .build();
        } catch (IOException ex) {
            throw new ResourceNotFoundException("Arquivo do relatório não encontrado");
        }
    }

    private Path salvarArquivo(UUID simulacaoId, String nomeArquivo, byte[] bytes) {
        try {
            Path diretorio = Path.of(reportsProperties.getStoragePath(), simulacaoId.toString())
                    .toAbsolutePath()
                    .normalize();
            Files.createDirectories(diretorio);
            Path destino = diretorio.resolve(nomeArquivo);
            Files.write(destino, bytes);
            return destino;
        } catch (IOException ex) {
            throw new IllegalStateException("Erro ao salvar arquivo do relatório: " + ex.getMessage(), ex);
        }
    }

    private UserPrincipal getUsuarioLogado() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private RelatorioConteudo enriquecerComBranding(RelatorioConteudo conteudo) {
        ConfiguracaoInstitucional institucional = configuracaoInstitucionalService.obterEntidade();
        byte[] logoBytes = null;
        if (institucional.getLogoCaminho() != null && !institucional.getLogoCaminho().isBlank()) {
            try {
                Path logoPath = Path.of(institucional.getLogoCaminho()).toAbsolutePath().normalize();
                if (Files.exists(logoPath)) {
                    logoBytes = Files.readAllBytes(logoPath);
                }
            } catch (IOException ignored) {
                // logo indisponível — gera relatório sem imagem
            }
        }

        return RelatorioConteudo.builder()
                .simulacao(conteudo.getSimulacao())
                .configuracao(conteudo.getConfiguracao())
                .projetos(conteudo.getProjetos())
                .totalHoras(conteudo.getTotalHoras())
                .totalUst(conteudo.getTotalUst())
                .valorTotal(conteudo.getValorTotal())
                .nomeOrganizacao(institucional.getNomeOrganizacao())
                .logoBytes(logoBytes)
                .build();
    }
}
