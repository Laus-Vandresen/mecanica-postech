package br.com.mecanica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "peca")
@Getter
@Setter
@NoArgsConstructor
public class PecaEntity extends ProdutoEntity {

    @Column(length = 80)
    private String fabricante;
}
