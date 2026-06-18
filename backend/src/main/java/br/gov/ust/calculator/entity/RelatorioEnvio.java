package br.gov.ust.calculator.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.gov.ust.calculator.entity.enums.RelatorioEnvioStatus;
import br.gov.ust.calculator.entity.enums.RelatorioTipo;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "relatorio_envios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "simulacao_id", nullable = false)
    private Simulacao simulacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relatorio_id")
    private Relatorio relatorio;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(nullable = false, length = 200)
    private String assunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RelatorioTipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RelatorioEnvioStatus status = RelatorioEnvioStatus.ENVIADO;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "enviado_em", nullable = false)
    @Builder.Default
    private LocalDateTime enviadoEm = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private UUID createdBy;
}
