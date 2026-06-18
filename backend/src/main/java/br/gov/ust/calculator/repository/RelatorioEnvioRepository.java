package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.ust.calculator.entity.RelatorioEnvio;

public interface RelatorioEnvioRepository extends JpaRepository<RelatorioEnvio, UUID> {

    List<RelatorioEnvio> findBySimulacaoIdOrderByEnviadoEmDesc(UUID simulacaoId);
}
