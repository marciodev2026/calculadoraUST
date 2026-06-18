package br.gov.ust.calculator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.SquadMembroRequest;
import br.gov.ust.calculator.dto.request.SquadRequest;
import br.gov.ust.calculator.dto.response.SquadResponse;
import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.entity.Perfil;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.Squad;
import br.gov.ust.calculator.entity.SquadMembro;
import br.gov.ust.calculator.exception.BusinessException;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.SquadMapper;
import br.gov.ust.calculator.repository.ConfiguracaoRepository;
import br.gov.ust.calculator.repository.PerfilRepository;
import br.gov.ust.calculator.repository.SquadRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SquadService {

    private static final int SCALE = 2;

    private final SquadRepository squadRepository;
    private final PerfilRepository perfilRepository;
    private final ConfiguracaoRepository configuracaoRepository;
    private final SquadMapper squadMapper;
    private final ProjetoService projetoService;
    private final SimulacaoService simulacaoService;

    @Transactional(readOnly = true)
    public SquadResponse buscarPorProjeto(UUID simulacaoId, UUID projetoId) {
        Projeto projeto = projetoService.obterEntidadeComAcesso(simulacaoId, projetoId);
        Configuracao configuracao = buscarConfiguracaoAtiva();

        return squadRepository.findByProjetoId(projetoId)
                .map(squad -> squadMapper.toResponse(squad, projeto, configuracao))
                .orElseGet(() -> squadMapper.toEmptyResponse(projeto, configuracao));
    }

    @Transactional
    public SquadResponse salvar(UUID simulacaoId, UUID projetoId, SquadRequest request) {
        Projeto projeto = projetoService.obterEntidadeComAcesso(simulacaoId, projetoId);
        Simulacao simulacao = projeto.getSimulacao();
        simulacaoService.validarEscrita(simulacao);

        Configuracao configuracao = buscarConfiguracaoAtiva();
        validarMembros(request);

        Squad squad = squadRepository.findByProjetoId(projetoId)
                .orElseGet(() -> Squad.builder().projeto(projeto).build());

        squad.getMembros().clear();

        for (SquadMembroRequest membroRequest : request.getMembros()) {
            Perfil perfil = perfilRepository.findById(membroRequest.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));

            if (!perfil.getAtivo()) {
                throw new BusinessException("Perfil inativo: " + perfil.getNome());
            }

            SquadMembro membro = SquadMembro.builder()
                    .squad(squad)
                    .perfil(perfil)
                    .quantidade(membroRequest.getQuantidade())
                    .fcpAplicado(perfil.getFcp())
                    .build();

            squad.getMembros().add(membro);
        }

        recalcular(squad, projeto, configuracao);

        return squadMapper.toResponse(squadRepository.save(squad), projeto, configuracao);
    }

    private void validarMembros(SquadRequest request) {
        Set<UUID> perfis = new HashSet<>();
        for (SquadMembroRequest membro : request.getMembros()) {
            if (!perfis.add(membro.getPerfilId())) {
                throw new BusinessException("Perfil duplicado na squad");
            }
        }
    }

    private void recalcular(Squad squad, Projeto projeto, Configuracao configuracao) {
        BigDecimal totalHoras = BigDecimal.ZERO;
        BigDecimal totalUst = BigDecimal.ZERO;

        BigDecimal horasSemanais = BigDecimal.valueOf(projeto.getHorasSemanais());
        BigDecimal semanas = BigDecimal.valueOf(projeto.getSemanas());

        for (SquadMembro membro : squad.getMembros()) {
            BigDecimal horas = BigDecimal.valueOf(membro.getQuantidade())
                    .multiply(horasSemanais)
                    .multiply(semanas)
                    .setScale(SCALE, RoundingMode.HALF_UP);

            BigDecimal ust = horas.multiply(membro.getFcpAplicado())
                    .setScale(SCALE, RoundingMode.HALF_UP);

            membro.setHorasCalculadas(horas);
            membro.setUstCalculada(ust);

            totalHoras = totalHoras.add(horas);
            totalUst = totalUst.add(ust);
        }

        squad.setTotalHoras(totalHoras);
        squad.setTotalUst(totalUst);
        squad.setValorTotal(calcularValorTotal(totalUst, configuracao));
    }

    private BigDecimal calcularValorTotal(BigDecimal totalUst, Configuracao configuracao) {
        BigDecimal encargos = BigDecimal.ONE.add(
                configuracao.getEncargosPercentual().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        BigDecimal bdi = BigDecimal.ONE.add(
                configuracao.getBdiPercentual().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );

        return totalUst
                .multiply(configuracao.getValorUst())
                .multiply(encargos)
                .multiply(bdi)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private Configuracao buscarConfiguracaoAtiva() {
        return configuracaoRepository.findByAtivoTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma configuração UST ativa encontrada"));
    }
}
