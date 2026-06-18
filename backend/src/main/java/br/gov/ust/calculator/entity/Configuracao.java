package br.gov.ust.calculator.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "configuracoes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuracao extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "valor_ust", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorUst;

    @Column(name = "carga_horaria_mes", nullable = false)
    private Integer cargaHorariaMes;

    @Column(name = "encargos_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal encargosPercentual;

    @Column(name = "bdi_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal bdiPercentual;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
