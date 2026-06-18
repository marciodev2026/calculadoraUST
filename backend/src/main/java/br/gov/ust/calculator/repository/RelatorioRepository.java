package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.ust.calculator.entity.Relatorio;

public interface RelatorioRepository extends JpaRepository<Relatorio, UUID> {

    List<Relatorio> findBySimulacaoIdOrderByGeradoEmDesc(UUID simulacaoId);

    Optional<Relatorio> findByIdAndSimulacaoId(UUID id, UUID simulacaoId);
}
