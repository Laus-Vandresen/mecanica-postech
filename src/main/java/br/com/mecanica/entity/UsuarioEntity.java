package br.com.mecanica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity extends PessoaEntity {

    @Column(nullable = false, unique = true, length = 80)
    private String login;

    @Column(nullable = false, length = 255)
    private String senha;
}
