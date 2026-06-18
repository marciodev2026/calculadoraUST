package br.gov.ust.calculator.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.ust.calculator.entity.ConfiguracaoInstitucional;

public interface ConfiguracaoInstitucionalRepository extends JpaRepository<ConfiguracaoInstitucional, UUID> {

    Optional<ConfiguracaoInstitucional> findFirstByOrderByCreatedAtAsc();
}
