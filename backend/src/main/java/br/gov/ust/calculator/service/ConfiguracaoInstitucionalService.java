package br.gov.ust.calculator.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.gov.ust.calculator.config.BrandingProperties;
import br.gov.ust.calculator.dto.request.ConfiguracaoInstitucionalRequest;
import br.gov.ust.calculator.dto.response.ConfiguracaoInstitucionalResponse;
import br.gov.ust.calculator.entity.ConfiguracaoInstitucional;
import br.gov.ust.calculator.exception.BusinessException;
import br.gov.ust.calculator.repository.ConfiguracaoInstitucionalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracaoInstitucionalService {

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("png", "jpg", "jpeg", "webp");
    private static final long TAMANHO_MAXIMO_BYTES = 2 * 1024 * 1024;

    private final ConfiguracaoInstitucionalRepository repository;
    private final BrandingProperties brandingProperties;

    @Transactional(readOnly = true)
    public ConfiguracaoInstitucionalResponse buscar() {
        return repository.findFirstByOrderByCreatedAtAsc()
                .map(this::toResponse)
                .orElse(ConfiguracaoInstitucionalResponse.builder()
                        .nomeOrganizacao("Governo Federal")
                        .possuiLogo(false)
                        .build());
    }

    public ConfiguracaoInstitucional obterEntidade() {
        return repository.findFirstByOrderByCreatedAtAsc()
                .orElse(ConfiguracaoInstitucional.builder()
                        .nomeOrganizacao("Governo Federal")
                        .build());
    }

    @Transactional
    public ConfiguracaoInstitucionalResponse salvar(ConfiguracaoInstitucionalRequest request) {
        ConfiguracaoInstitucional config = obterOuCriar();
        config.setNomeOrganizacao(request.getNomeOrganizacao().trim());
        return toResponse(repository.save(config));
    }

    @Transactional
    public ConfiguracaoInstitucionalResponse uploadLogo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Arquivo de logo é obrigatório");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new BusinessException("Logo deve ter no máximo 2 MB");
        }

        String extensao = extrairExtensao(arquivo.getOriginalFilename());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new BusinessException("Formato inválido. Use PNG, JPG ou WEBP");
        }

        ConfiguracaoInstitucional config = obterOuCriar();
        removerLogoAnterior(config);

        try {
            Path diretorio = Path.of(brandingProperties.getStoragePath()).toAbsolutePath().normalize();
            Files.createDirectories(diretorio);
            Path destino = diretorio.resolve("logo." + extensao);
            try (InputStream input = arquivo.getInputStream()) {
                Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            config.setLogoCaminho(destino.toString());
            return toResponse(repository.save(config));
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível salvar o logo: " + ex.getMessage());
        }
    }

    @Transactional
    public ConfiguracaoInstitucionalResponse removerLogo() {
        ConfiguracaoInstitucional config = obterOuCriar();
        removerLogoAnterior(config);
        config.setLogoCaminho(null);
        return toResponse(repository.save(config));
    }

    @Transactional(readOnly = true)
    public Resource carregarLogo() {
        ConfiguracaoInstitucional config = obterOuCriar();
        if (config.getLogoCaminho() == null || config.getLogoCaminho().isBlank()) {
            throw new BusinessException("Logo não configurado");
        }

        try {
            Path path = Path.of(config.getLogoCaminho());
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("Arquivo de logo não encontrado");
            }
            return resource;
        } catch (IOException ex) {
            throw new BusinessException("Arquivo de logo não encontrado");
        }
    }

    @Transactional(readOnly = true)
    public String obterTipoConteudoLogo() {
        ConfiguracaoInstitucional config = obterOuCriar();
        if (config.getLogoCaminho() == null) {
            return "image/png";
        }
        String nome = Path.of(config.getLogoCaminho()).getFileName().toString().toLowerCase();
        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (nome.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png";
    }

    private ConfiguracaoInstitucional obterOuCriar() {
        return repository.findFirstByOrderByCreatedAtAsc()
                .orElseGet(() -> repository.save(ConfiguracaoInstitucional.builder()
                        .nomeOrganizacao("Governo Federal")
                        .build()));
    }

    private void removerLogoAnterior(ConfiguracaoInstitucional config) {
        if (config.getLogoCaminho() == null || config.getLogoCaminho().isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(config.getLogoCaminho()));
        } catch (IOException ignored) {
            // ignora falha ao remover arquivo antigo
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();
    }

    private ConfiguracaoInstitucionalResponse toResponse(ConfiguracaoInstitucional config) {
        boolean possuiLogo = config.getLogoCaminho() != null
                && !config.getLogoCaminho().isBlank()
                && Files.exists(Path.of(config.getLogoCaminho()));

        return ConfiguracaoInstitucionalResponse.builder()
                .id(config.getId())
                .nomeOrganizacao(config.getNomeOrganizacao())
                .possuiLogo(possuiLogo)
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
