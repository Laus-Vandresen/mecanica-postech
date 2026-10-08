package br.com.mecanica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insumo")
@Getter
@Setter
@NoArgsConstructor
public class InsumoEntity extends ProdutoEntity {

    @Column(nullable = false, length = 20)
    private String unidadeMedida;
}
