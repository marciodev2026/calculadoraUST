package br.gov.ust.calculator.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.PerfilRequest;
import br.gov.ust.calculator.dto.response.PerfilResponse;
import br.gov.ust.calculator.entity.Perfil;
import br.gov.ust.calculator.exception.BusinessException;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.PerfilMapper;
import br.gov.ust.calculator.repository.PerfilRepository;
import br.gov.ust.calculator.repository.SquadMembroRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final SquadMembroRepository squadMembroRepository;
    private final PerfilMapper perfilMapper;

    @Transactional(readOnly = true)
    public List<PerfilResponse> listarTodos() {
        return perfilRepository.findAllByOrderByFcpAsc().stream()
                .map(perfilMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PerfilResponse> listarAtivos() {
        return perfilRepository.findByAtivoTrueOrderByFcpAsc().stream()
                .map(perfilMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PerfilResponse buscarPorId(UUID id) {
        return perfilMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public PerfilResponse criar(PerfilRequest request) {
        if (perfilRepository.existsByNome(request.getNome())) {
            throw new BusinessException("Já existe um perfil com este nome");
        }

        Perfil perfil = Perfil.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .fcp(request.getFcp())
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .build();

        return perfilMapper.toResponse(perfilRepository.save(perfil));
    }

    @Transactional
    public PerfilResponse atualizar(UUID id, PerfilRequest request) {
        Perfil perfil = buscarEntidade(id);

        if (perfilRepository.existsByNomeAndIdNot(request.getNome(), id)) {
            throw new BusinessException("Já existe um perfil com este nome");
        }

        perfil.setNome(request.getNome());
        perfil.setDescricao(request.getDescricao());
        perfil.setFcp(request.getFcp());
        if (request.getAtivo() != null) {
            perfil.setAtivo(request.getAtivo());
        }

        return perfilMapper.toResponse(perfilRepository.save(perfil));
    }

    @Transactional
    public void excluir(UUID id) {
        Perfil perfil = buscarEntidade(id);

        if (squadMembroRepository.existsByPerfilId(id)) {
            throw new BusinessException(
                    "Perfil em uso em squads. Desative-o em vez de excluir."
            );
        }

        perfilRepository.delete(perfil);
    }

    private Perfil buscarEntidade(UUID id) {
        return perfilRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));
    }
}
