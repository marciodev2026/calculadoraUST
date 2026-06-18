package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.gov.ust.calculator.entity.Projeto;

public interface ProjetoRepository extends JpaRepository<Projeto, UUID> {

    @EntityGraph(attributePaths = "simulacao")
    List<Projeto> findBySimulacaoIdOrderByNomeAsc(UUID simulacaoId);

    @EntityGraph(attributePaths = "simulacao")
    Optional<Projeto> findByIdAndSimulacaoId(UUID id, UUID simulacaoId);

    @Query("""
            SELECT COUNT(p) FROM Projeto p
            JOIN p.simulacao s
            WHERE (:usuarioId IS NULL OR s.usuario.id = :usuarioId)
            """)
    long countAcessiveis(@Param("usuarioId") UUID usuarioId);

    @EntityGraph(attributePaths = "simulacao")
    @Query("""
            SELECT p FROM Projeto p
            JOIN p.simulacao s
            WHERE (:usuarioId IS NULL OR s.usuario.id = :usuarioId)
            """)
    List<Projeto> findAllAcessiveis(@Param("usuarioId") UUID usuarioId);
}
