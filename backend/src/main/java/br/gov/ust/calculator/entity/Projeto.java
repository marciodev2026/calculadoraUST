package br.gov.ust.calculator.entity;

import java.util.UUID;

import br.gov.ust.calculator.entity.base.AuditableEntity;
import br.gov.ust.calculator.entity.enums.ProjetoStatus;
import br.gov.ust.calculator.entity.enums.ProjetoTipo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projetos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Projeto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulacao_id", nullable = false)
    private Simulacao simulacao;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjetoTipo tipo;

    @Column(nullable = false)
    private Integer semanas;

    @Column(name = "horas_semanais", nullable = false)
    @Builder.Default
    private Integer horasSemanais = 40;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjetoStatus status = ProjetoStatus.ATIVO;

    @OneToOne(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Squad squad;
}
