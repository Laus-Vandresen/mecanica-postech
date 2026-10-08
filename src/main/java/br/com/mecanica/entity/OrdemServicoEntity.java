package br.com.mecanica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
@NoArgsConstructor
public class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private VeiculoEntity veiculo;

    @OneToOne
    @JoinColumn(name = "orcamento_id")
    private OrcamentoEntity orcamento;

    @ManyToMany
    private List<ServicoEntity> servicos;

    @ManyToMany
    private List<ProdutoEntity> produtos;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @Column
    private LocalDateTime dataFechamento;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 500)
    private String descricao;

    @Column(length = 1000)
    private String diagnostico;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;
}
