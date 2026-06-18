package br.gov.ust.calculator.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.ConfiguracaoUstRequest;
import br.gov.ust.calculator.dto.response.ConfiguracaoUstResponse;
import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.ConfiguracaoMapper;
import br.gov.ust.calculator.repository.ConfiguracaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracaoUstService {

    private final ConfiguracaoRepository configuracaoRepository;
    private final ConfiguracaoMapper configuracaoMapper;

    @Transactional(readOnly = true)
    public ConfiguracaoUstResponse buscarAtiva() {
        return configuracaoRepository.findByAtivoTrue()
                .map(configuracaoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma configuração UST ativa encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ConfiguracaoUstResponse> listarHistorico() {
        return configuracaoRepository.findAllByOrderByVigenteDesdeDescCreatedAtDesc().stream()
                .map(configuracaoMapper::toResponse)
                .toList();
    }

    @Transactional
    public ConfiguracaoUstResponse salvar(ConfiguracaoUstRequest request) {
        configuracaoRepository.desativarTodas();

        Configuracao configuracao = Configuracao.builder()
                .valorUst(request.getValorUst())
                .cargaHorariaMes(request.getCargaHorariaMes())
                .encargosPercentual(request.getEncargosPercentual())
                .bdiPercentual(request.getBdiPercentual())
                .vigenteDesde(request.getVigenteDesde() != null ? request.getVigenteDesde() : LocalDate.now())
                .ativo(true)
                .build();

        return configuracaoMapper.toResponse(configuracaoRepository.save(configuracao));
    }
}
