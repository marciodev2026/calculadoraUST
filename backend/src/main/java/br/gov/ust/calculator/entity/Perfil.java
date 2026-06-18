package br.gov.ust.calculator.entity;

import java.math.BigDecimal;
import java.util.UUID;

import br.gov.ust.calculator.entity.base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal fcp;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
