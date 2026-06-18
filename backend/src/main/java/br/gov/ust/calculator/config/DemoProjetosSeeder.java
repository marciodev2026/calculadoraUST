package br.gov.ust.calculator.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import br.gov.ust.calculator.entity.Configuracao;
import br.gov.ust.calculator.entity.Perfil;
import br.gov.ust.calculator.entity.Projeto;
import br.gov.ust.calculator.entity.Simulacao;
import br.gov.ust.calculator.entity.Squad;
import br.gov.ust.calculator.entity.SquadMembro;
import br.gov.ust.calculator.entity.Usuario;
import br.gov.ust.calculator.entity.enums.ProjetoStatus;
import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import br.gov.ust.calculator.entity.enums.SimulacaoStatus;
import br.gov.ust.calculator.repository.ConfiguracaoRepository;
import br.gov.ust.calculator.repository.PerfilRepository;
import br.gov.ust.calculator.repository.SimulacaoRepository;
import br.gov.ust.calculator.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class DemoProjetosSeeder {

    private static final int SCALE = 2;

    private final SimulacaoRepository simulacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final ConfiguracaoRepository configuracaoRepository;

    @Bean
    @Order(2)
    CommandLineRunner seedDemoProjetos() {
        return args -> {
            if (simulacaoRepository.count() > 0) {
                return;
            }

            Configuracao configuracao = configuracaoRepository.findByAtivoTrue().orElse(null);
            if (configuracao == null) {
                return;
            }

            Map<String, Perfil> perfis = perfilRepository.findAll().stream()
                    .collect(Collectors.toMap(Perfil::getNome, Function.identity()));

            usuarioRepository.findByEmail("analista@ust.gov.br").ifPresent(analista ->
                    criarSimulacao(
                            analista,
                            "Simulação UST 2025 — Portal Cidadão",
                            SimulacaoStatus.FINALIZADA,
                            configuracao,
                            perfis,
                            List.of(
                                    projeto("Portal Cidadão v2", ProjetoTipo.PROJETO, 24, 40,
                                            "Desenvolvimento do novo portal de serviços ao cidadão com autenticação gov.br.",
                                            List.of(membro("Desenvolvedor Pleno", 2),
                                                    membro("Desenvolvedor Senior", 1),
                                                    membro("UX/UI", 1),
                                                    membro("Product Owner", 1))),
                                    projeto("Manutenção Sistemas Legados", ProjetoTipo.SUSTENTACAO, 52, 40,
                                            "Sustentação corretiva e adaptativa dos sistemas legados do órgão.",
                                            List.of(membro("Desenvolvedor Junior", 2),
                                                    membro("Desenvolvedor Pleno", 1),
                                                    membro("DBA", 1))),
                                    projeto("Módulo Agendamento Online", ProjetoTipo.EVOLUCAO, 16, 40,
                                            "Evolução do sistema com agendamento de atendimentos presenciais e remotos.",
                                            List.of(membro("Desenvolvedor Senior", 2),
                                                    membro("DevOps", 1),
                                                    membro("Scrum Master", 1))),
                                    projeto("Correção Integração e-SUS", ProjetoTipo.CORRECAO, 8, 40,
                                            "Correção de falhas na integração com a plataforma e-SUS.",
                                            List.of(membro("Especialista", 1),
                                                    membro("Desenvolvedor Pleno", 2)))
                            )));

            usuarioRepository.findByEmail("gestor@ust.gov.br").ifPresent(gestor ->
                    criarSimulacao(
                            gestor,
                            "Planejamento Estratégico TI 2025",
                            SimulacaoStatus.RASCUNHO,
                            configuracao,
                            perfis,
                            List.of(
                                    projeto("Sistema Gestão de Contratos", ProjetoTipo.PROJETO, 20, 40,
                                            "Novo sistema para gestão do ciclo de vida de contratos administrativos.",
                                            List.of(membro("Arquiteto", 1),
                                                    membro("Desenvolvedor Senior", 2),
                                                    membro("Gerente de Projeto", 1),
                                                    membro("Desenvolvedor Junior", 3))),
                                    projeto("API Dados Abertos", ProjetoTipo.EVOLUCAO, 12, 40,
                                            "Exposição de conjuntos de dados institucionais via API REST.",
                                            List.of(membro("DevOps", 1),
                                                    membro("Desenvolvedor Pleno", 2),
                                                    membro("Especialista", 1))),
                                    projeto("Suporte Infraestrutura Cloud", ProjetoTipo.SUSTENTACAO, 40, 40,
                                            "Operação e monitoramento da infraestrutura em nuvem governamental.",
                                            List.of(membro("DevOps", 2),
                                                    membro("DBA", 1),
                                                    membro("Desenvolvedor Pleno", 1)))
                            )));
        };
    }

    private void criarSimulacao(
            Usuario usuario,
            String titulo,
            SimulacaoStatus status,
            Configuracao configuracao,
            Map<String, Perfil> perfis,
            List<ProjetoSpec> projetos
    ) {
        Simulacao simulacao = simulacaoRepository.save(Simulacao.builder()
                .usuario(usuario)
                .nomeCompleto(usuario.getNomeCompleto())
                .email(usuario.getEmail())
                .orgao(usuario.getOrgao())
                .departamento(usuario.getDepartamento())
                .telefone("(61) 3333-0000")
                .dataSimulacao(LocalDate.now().minusDays(status == SimulacaoStatus.FINALIZADA ? 15 : 3))
                .status(status)
                .build());

        for (ProjetoSpec spec : projetos) {
            Projeto projeto = Projeto.builder()
                    .simulacao(simulacao)
                    .nome(spec.nome())
                    .tipo(spec.tipo())
                    .semanas(spec.semanas())
                    .horasSemanais(spec.horasSemanais())
                    .descricao(spec.descricao())
                    .status(ProjetoStatus.ATIVO)
                    .build();

            Squad squad = Squad.builder().projeto(projeto).build();
            projeto.setSquad(squad);

            for (MembroSpec membroSpec : spec.membros()) {
                Perfil perfil = perfis.get(membroSpec.perfilNome());
                if (perfil == null) {
                    continue;
                }
                squad.getMembros().add(SquadMembro.builder()
                        .squad(squad)
                        .perfil(perfil)
                        .quantidade(membroSpec.quantidade())
                        .fcpAplicado(perfil.getFcp())
                        .build());
            }

            recalcularSquad(squad, projeto, configuracao);
            simulacao.getProjetos().add(projeto);
        }

        simulacaoRepository.save(simulacao);
    }

    private void recalcularSquad(Squad squad, Projeto projeto, Configuracao configuracao) {
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

    private static ProjetoSpec projeto(
            String nome,
            ProjetoTipo tipo,
            int semanas,
            int horasSemanais,
            String descricao,
            List<MembroSpec> membros
    ) {
        return new ProjetoSpec(nome, tipo, semanas, horasSemanais, descricao, membros);
    }

    private static MembroSpec membro(String nomePerfil, int quantidade) {
        return new MembroSpec(nomePerfil, quantidade);
    }

    private record ProjetoSpec(
            String nome,
            ProjetoTipo tipo,
            int semanas,
            int horasSemanais,
            String descricao,
            List<MembroSpec> membros
    ) {}

    private record MembroSpec(String perfilNome, int quantidade) {}
}
