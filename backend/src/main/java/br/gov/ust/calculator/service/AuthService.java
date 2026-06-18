package br.gov.ust.calculator.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.gov.ust.calculator.dto.request.LoginRequest;
import br.gov.ust.calculator.dto.response.LoginResponse;
import br.gov.ust.calculator.dto.response.UsuarioResponse;
import br.gov.ust.calculator.mapper.UsuarioMapper;
import br.gov.ust.calculator.repository.UsuarioRepository;
import br.gov.ust.calculator.security.JwtProperties;
import br.gov.ust.calculator.security.JwtTokenProvider;
import br.gov.ust.calculator.security.UserPrincipal;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userPrincipal);

        UsuarioResponse usuario = usuarioRepository.findById(userPrincipal.getId())
                .map(usuarioMapper::toResponse)
                .orElseThrow();

        return LoginResponse.of(token, jwtProperties.getExpirationMs(), usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(UserPrincipal userPrincipal) {
        return usuarioRepository.findById(userPrincipal.getId())
                .map(usuarioMapper::toResponse)
                .orElseThrow();
    }
}
