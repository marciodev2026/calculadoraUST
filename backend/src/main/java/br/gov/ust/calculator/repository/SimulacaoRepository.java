package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.gov.ust.calculator.entity.Simulacao;

public interface SimulacaoRepository extends JpaRepository<Simulacao, UUID> {

    @EntityGraph(attributePaths = "usuario")
    List<Simulacao> findAllByOrderByDataSimulacaoDescCreatedAtDesc();

    @EntityGraph(attributePaths = "usuario")
    List<Simulacao> findByUsuarioIdOrderByDataSimulacaoDescCreatedAtDesc(UUID usuarioId);

    @Query("""
            SELECT COUNT(s) FROM Simulacao s
            WHERE (:usuarioId IS NULL OR s.usuario.id = :usuarioId)
            """)
    long countAcessiveis(@Param("usuarioId") UUID usuarioId);
}
