package br.gov.ust.calculator.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.gov.ust.calculator.entity.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, UUID id);

    List<Perfil> findAllByOrderByFcpAsc();

    List<Perfil> findByAtivoTrueOrderByFcpAsc();
}
