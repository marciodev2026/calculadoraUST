package br.gov.ust.calculator.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.entity.ConfiguracaoInstitucional;
import br.gov.ust.calculator.entity.Perfil;
import br.gov.ust.calculator.entity.Usuario;
import br.gov.ust.calculator.entity.enums.UserRole;
import br.gov.ust.calculator.repository.ConfiguracaoInstitucionalRepository;
import br.gov.ust.calculator.repository.ConfiguracaoRepository;
import br.gov.ust.calculator.repository.PerfilRepository;
import br.gov.ust.calculator.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalDataSeeder {

    private static final String SENHA_GESTOR_HASH =
            "$2y$10$wTbJU2DY4D.meoregp0PeeDvcjFkFGt95hB.jNjQatqA8DJrdGgEa";

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final ConfiguracaoRepository configuracaoRepository;
    private final ConfiguracaoInstitucionalRepository configuracaoInstitucionalRepository;

    @Bean
    CommandLineRunner seedLocalData() {
        return args -> {
            if (usuarioRepository.count() == 0) {
                criarDadosIniciais();
            }
            sincronizarSenhasDemonstracao();
        };
    }

    private void sincronizarSenhasDemonstracao() {
        Map<String, String> senhasEsperadas = Map.of(
                "gestor@ust.gov.br", SENHA_GESTOR_HASH
        );

        senhasEsperadas.forEach((email, hash) ->
                usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                    if (!hash.equals(usuario.getSenhaHash())) {
                        usuario.setSenhaHash(hash);
                        usuarioRepository.save(usuario);
                    }
                }));
    }

    private void criarDadosIniciais() {
            Usuario admin = usuarioRepository.save(Usuario.builder()
                    .nomeCompleto("Administrador do Sistema")
                    .email("admin@ust.gov.br")
                    .senhaHash("$2y$10$hESOE5u8Szv8arF42eSuluQZHf5ip3RbkNgW3/b.rjTV3ESIo.1PG")
                    .orgao("Governo Federal")
                    .departamento("TI")
                    .role(UserRole.ADMIN)
                    .ativo(true)
                    .build());

            usuarioRepository.save(Usuario.builder()
                    .nomeCompleto("Gestor Demonstração")
                    .email("gestor@ust.gov.br")
                    .senhaHash(SENHA_GESTOR_HASH)
                    .orgao("Governo Federal")
                    .departamento("Gestão de Projetos")
                    .role(UserRole.GESTOR)
                    .ativo(true)
                    .build());

            usuarioRepository.save(Usuario.builder()
                    .nomeCompleto("Analista Demonstração")
                    .email("analista@ust.gov.br")
                    .senhaHash("$2y$10$GyvaVe/OYfYRwq5XmZV0x.oY2fZzccn9xo5mRGjewokMU9d578n76")
                    .orgao("Exército Brasileiro")
                    .departamento("Centro de Desenvolvimento")
                    .role(UserRole.ANALISTA)
                    .ativo(true)
                    .build());

            String[][] perfis = {
                    {"Desenvolvedor Junior", "Desenvolvedor com experiência inicial", "1.00"},
                    {"Desenvolvedor Pleno", "Desenvolvedor com experiência intermediária", "1.30"},
                    {"Desenvolvedor Senior", "Desenvolvedor com experiência avançada", "1.80"},
                    {"Especialista", "Especialista técnico em área específica", "2.20"},
                    {"Arquiteto", "Arquiteto de software/soluções", "2.50"},
                    {"Scrum Master", "Facilitador ágil de equipes", "2.00"},
                    {"Product Owner", "Responsável pelo produto e backlog", "2.10"},
                    {"Gerente de Projeto", "Gestão e coordenação de projetos", "2.80"},
                    {"DBA", "Administrador de banco de dados", "2.00"},
                    {"DevOps", "Operações e integração contínua", "2.20"},
                    {"UX/UI", "Design de experiência e interface", "1.80"}
            };

            for (String[] p : perfis) {
                perfilRepository.save(Perfil.builder()
                        .nome(p[0])
                        .descricao(p[1])
                        .fcp(new BigDecimal(p[2]))
                        .ativo(true)
                        .build());
            }

            configuracaoRepository.save(Configuracao.builder()
                    .valorUst(new BigDecimal("180.00"))
                    .cargaHorariaMes(160)
                    .encargosPercentual(new BigDecimal("75.00"))
                    .bdiPercentual(new BigDecimal("25.00"))
                    .vigenteDesde(LocalDate.now())
                    .ativo(true)
                    .build());

            configuracaoInstitucionalRepository.save(ConfiguracaoInstitucional.builder()
                    .nomeOrganizacao("Governo Federal")
                    .build());
    }
}
