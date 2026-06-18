package br.gov.ust.calculator.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.ProjetoRequest;
import br.gov.ust.calculator.dto.response.ProjetoResponse;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.enums.ProjetoStatus;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.ProjetoMapper;
import br.gov.ust.calculator.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMapper projetoMapper;
    private final SimulacaoService simulacaoService;

    @Transactional(readOnly = true)
    public List<ProjetoResponse> listarPorSimulacao(UUID simulacaoId) {
        simulacaoService.obterComAcesso(simulacaoId);
        return projetoRepository.findBySimulacaoIdOrderByNomeAsc(simulacaoId).stream()
                .map(projetoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscarPorId(UUID simulacaoId, UUID projetoId) {
        return projetoMapper.toResponse(buscarEntidade(simulacaoId, projetoId));
    }

    @Transactional(readOnly = true)
    public Projeto obterEntidadeComAcesso(UUID simulacaoId, UUID projetoId) {
        simulacaoService.obterComAcesso(simulacaoId);
        return buscarEntidade(simulacaoId, projetoId);
    }

    @Transactional
    public ProjetoResponse criar(UUID simulacaoId, ProjetoRequest request) {
        Simulacao simulacao = simulacaoService.obterComAcesso(simulacaoId);
        simulacaoService.validarEscrita(simulacao);

        Projeto projeto = Projeto.builder()
                .simulacao(simulacao)
                .nome(request.getNome())
                .tipo(request.getTipo())
                .semanas(request.getSemanas())
                .horasSemanais(request.getHorasSemanais() != null ? request.getHorasSemanais() : 40)
                .descricao(request.getDescricao())
                .status(request.getStatus() != null ? request.getStatus() : ProjetoStatus.ATIVO)
                .build();

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public ProjetoResponse atualizar(UUID simulacaoId, UUID projetoId, ProjetoRequest request) {
        Simulacao simulacao = simulacaoService.obterComAcesso(simulacaoId);
        simulacaoService.validarEscrita(simulacao);

        Projeto projeto = buscarEntidade(simulacaoId, projetoId);

        projeto.setNome(request.getNome());
        projeto.setTipo(request.getTipo());
        projeto.setSemanas(request.getSemanas());
        projeto.setHorasSemanais(request.getHorasSemanais() != null ? request.getHorasSemanais() : 40);
        projeto.setDescricao(request.getDescricao());
        if (request.getStatus() != null) {
            projeto.setStatus(request.getStatus());
        }

        return projetoMapper.toResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public void excluir(UUID simulacaoId, UUID projetoId) {
        Simulacao simulacao = simulacaoService.obterComAcesso(simulacaoId);
        simulacaoService.validarEscrita(simulacao);

        Projeto projeto = buscarEntidade(simulacaoId, projetoId);
        projetoRepository.delete(projeto);
    }

    private Projeto buscarEntidade(UUID simulacaoId, UUID projetoId) {
        return projetoRepository.findByIdAndSimulacaoId(projetoId, simulacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
    }
}
