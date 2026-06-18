package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.gov.ust.calculator.entity.Squad;

public interface SquadRepository extends JpaRepository<Squad, UUID> {

    @EntityGraph(attributePaths = {"membros", "membros.perfil", "projeto", "projeto.simulacao"})
    Optional<Squad> findByProjetoId(UUID projetoId);

    @EntityGraph(attributePaths = {"membros", "membros.perfil", "projeto", "projeto.simulacao"})
    @Query("""
            SELECT s FROM Squad s
            JOIN s.projeto p
            JOIN p.simulacao sim
            WHERE (:usuarioId IS NULL OR sim.usuario.id = :usuarioId)
            ORDER BY p.nome ASC
            """)
    List<Squad> findAllForDashboard(@Param("usuarioId") UUID usuarioId);
}
