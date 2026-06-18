package br.gov.ust.calculator.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.SimulacaoRequest;
import br.gov.ust.calculator.dto.response.SimulacaoResponse;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.Usuario;
import br.gov.ust.calculator.entity.enums.SimulacaoStatus;
import br.gov.ust.calculator.entity.enums.UserRole;
import br.gov.ust.calculator.exception.BusinessException;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.SimulacaoMapper;
import br.gov.ust.calculator.repository.SimulacaoRepository;
import br.gov.ust.calculator.repository.UsuarioRepository;
import br.gov.ust.calculator.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulacaoService {

    private final SimulacaoRepository simulacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SimulacaoMapper simulacaoMapper;

    @Transactional(readOnly = true)
    public List<SimulacaoResponse> listar() {
        UserPrincipal principal = getUsuarioLogado();
        List<Simulacao> simulacoes = switch (principal.getRole()) {
            case ADMIN, CONSULTA -> simulacaoRepository.findAllByOrderByDataSimulacaoDescCreatedAtDesc();
            case GESTOR, ANALISTA -> simulacaoRepository.findByUsuarioIdOrderByDataSimulacaoDescCreatedAtDesc(principal.getId());
        };

        return simulacoes.stream().map(simulacaoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SimulacaoResponse buscarPorId(UUID id) {
        return simulacaoMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public SimulacaoResponse criar(SimulacaoRequest request) {
        Usuario usuario = buscarUsuarioLogado();

        Simulacao simulacao = Simulacao.builder()
                .usuario(usuario)
                .nomeCompleto(request.getNomeCompleto())
                .email(request.getEmail())
                .orgao(request.getOrgao())
                .departamento(request.getDepartamento())
                .telefone(request.getTelefone())
                .dataSimulacao(request.getDataSimulacao())
                .status(request.getStatus() != null ? request.getStatus() : SimulacaoStatus.RASCUNHO)
                .build();

        return simulacaoMapper.toResponse(simulacaoRepository.save(simulacao));
    }

    @Transactional
    public SimulacaoResponse atualizar(UUID id, SimulacaoRequest request) {
        Simulacao simulacao = buscarEntidade(id);
        validarPermissaoEscrita(simulacao);

        if (simulacao.getStatus() == SimulacaoStatus.FINALIZADA) {
            throw new BusinessException("Simulação finalizada não pode ser alterada");
        }

        simulacao.setNomeCompleto(request.getNomeCompleto());
        simulacao.setEmail(request.getEmail());
        simulacao.setOrgao(request.getOrgao());
        simulacao.setDepartamento(request.getDepartamento());
        simulacao.setTelefone(request.getTelefone());
        simulacao.setDataSimulacao(request.getDataSimulacao());
        if (request.getStatus() != null) {
            simulacao.setStatus(request.getStatus());
        }

        return simulacaoMapper.toResponse(simulacaoRepository.save(simulacao));
    }

    @Transactional
    public void excluir(UUID id) {
        Simulacao simulacao = buscarEntidade(id);
        validarPermissaoEscrita(simulacao);

        if (simulacao.getStatus() == SimulacaoStatus.FINALIZADA) {
            throw new BusinessException("Simulação finalizada não pode ser excluída");
        }

        simulacaoRepository.delete(simulacao);
    }

    @Transactional(readOnly = true)
    public Simulacao obterComAcesso(UUID id) {
        return buscarEntidade(id);
    }

    public void validarEscrita(Simulacao simulacao) {
        validarPermissaoEscrita(simulacao);
        if (simulacao.getStatus() == SimulacaoStatus.FINALIZADA) {
            throw new BusinessException("Simulação finalizada não permite alterações");
        }
    }

    public void validarGeracaoRelatorio(Simulacao simulacao) {
        validarPermissaoEscrita(simulacao);
    }

    private Simulacao buscarEntidade(UUID id) {
        Simulacao simulacao = simulacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulação não encontrada"));

        UserPrincipal principal = getUsuarioLogado();
        if (principal.getRole() != UserRole.ADMIN
                && principal.getRole() != UserRole.CONSULTA
                && !simulacao.getUsuario().getId().equals(principal.getId())) {
            throw new ResourceNotFoundException("Simulação não encontrada");
        }

        return simulacao;
    }

    private void validarPermissaoEscrita(Simulacao simulacao) {
        UserPrincipal principal = getUsuarioLogado();
        if (principal.getRole() == UserRole.CONSULTA) {
            throw new BusinessException("Perfil consulta possui apenas visualização");
        }
        if (principal.getRole() != UserRole.ADMIN
                && !simulacao.getUsuario().getId().equals(principal.getId())) {
            throw new BusinessException("Você não tem permissão para alterar esta simulação");
        }
    }

    private UserPrincipal getUsuarioLogado() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Usuario buscarUsuarioLogado() {
        UserPrincipal principal = getUsuarioLogado();
        return usuarioRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
