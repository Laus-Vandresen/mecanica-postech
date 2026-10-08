package br.com.mecanica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class ClienteEntity extends PessoaEntity {

    @Column(length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;
}
