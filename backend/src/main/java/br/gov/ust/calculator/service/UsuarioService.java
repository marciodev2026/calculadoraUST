package br.gov.ust.calculator.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.UsuarioRequest;
import br.gov.ust.calculator.dto.response.UsuarioResponse;
import br.gov.ust.calculator.entity.Usuario;
import br.gov.ust.calculator.entity.enums.UserRole;
import br.gov.ust.calculator.exception.BusinessException;
import br.gov.ust.calculator.exception.ResourceNotFoundException;
import br.gov.ust.calculator.mapper.UsuarioMapper;
import br.gov.ust.calculator.repository.UsuarioRepository;
import br.gov.ust.calculator.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new UserPrincipal(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        UserPrincipal principal = getUsuarioLogado();
        List<Usuario> usuarios = switch (principal.getRole()) {
            case ADMIN -> usuarioRepository.findAll();
            case GESTOR -> usuarioRepository.findByRoleOrderByNomeCompletoAsc(UserRole.ANALISTA);
            default -> throw new BusinessException("Sem permissão para listar usuários");
        };

        return usuarios.stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        Usuario usuario = buscarEntidade(id);
        validarAcessoLeitura(usuario);
        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        validarPermissaoCriacao(request.getRole());

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado");
        }
        if (request.getSenha() == null || request.getSenha().isBlank()) {
            throw new BusinessException("Senha é obrigatória para novo usuário");
        }

        Usuario usuario = Usuario.builder()
                .nomeCompleto(request.getNomeCompleto())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .orgao(request.getOrgao())
                .departamento(request.getDepartamento())
                .telefone(request.getTelefone())
                .role(request.getRole())
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .build();

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    private void validarPermissaoCriacao(UserRole role) {
        UserPrincipal principal = getUsuarioLogado();
        switch (principal.getRole()) {
            case ADMIN -> {
                // administrador pode criar qualquer perfil
            }
            case GESTOR -> {
                if (role != UserRole.ANALISTA) {
                    throw new BusinessException("Gestor pode cadastrar apenas usuários analistas");
                }
            }
            default -> throw new BusinessException("Sem permissão para criar usuários");
        }
    }

    private void validarAcessoLeitura(Usuario usuario) {
        UserPrincipal principal = getUsuarioLogado();
        if (principal.getRole() == UserRole.GESTOR && usuario.getRole() != UserRole.ANALISTA) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
    }

    private Usuario buscarEntidade(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private UserPrincipal getUsuarioLogado() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
