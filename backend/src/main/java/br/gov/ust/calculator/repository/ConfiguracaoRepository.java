package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.gov.ust.calculator.entity.Configuracao;

public interface ConfiguracaoRepository extends JpaRepository<Configuracao, UUID> {

    Optional<Configuracao> findByAtivoTrue();

    List<Configuracao> findAllByOrderByVigenteDesdeDescCreatedAtDesc();

    @Modifying
    @Query("UPDATE Configuracao c SET c.ativo = false WHERE c.ativo = true")
    void desativarTodas();
}
