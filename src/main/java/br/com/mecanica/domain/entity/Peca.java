package br.com.mecanica.domain.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Peca {

    private UUID id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private Integer quantidadeEstoque;
    private String fabricante;

}
