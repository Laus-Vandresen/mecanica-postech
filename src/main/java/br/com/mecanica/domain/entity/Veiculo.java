package br.com.mecanica.domain.entity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Veiculo {

    private UUID id;
    private String placa;
    private String modelo;
    private String marca;
    private Integer ano;
    private UUID clienteId;

}
