package br.com.mecanica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ItemProdutoOrdemServicoEntity {

    @ManyToOne
    @JoinColumn(name = "produtos_id", nullable = false)
    private ProdutoEntity produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private int quantidadeConsumida;
}
