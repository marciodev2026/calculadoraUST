package br.gov.ust.calculator.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.ust.calculator.entity.SquadMembro;

public interface SquadMembroRepository extends JpaRepository<SquadMembro, UUID> {

    boolean existsByPerfilId(UUID perfilId);
}
