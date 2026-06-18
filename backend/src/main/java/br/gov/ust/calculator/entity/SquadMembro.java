package br.gov.ust.calculator.entity;

import java.math.BigDecimal;
import java.util.UUID;

import br.gov.ust.calculator.entity.base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "squad_membros",
    uniqueConstraints = @UniqueConstraint(columnNames = {"squad_id", "perfil_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SquadMembro extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "fcp_aplicado", nullable = false, precision = 4, scale = 2)
    private BigDecimal fcpAplicado;

    @Column(name = "horas_calculadas", precision = 12, scale = 2)
    private BigDecimal horasCalculadas;

    @Column(name = "ust_calculada", precision = 12, scale = 2)
    private BigDecimal ustCalculada;
}
