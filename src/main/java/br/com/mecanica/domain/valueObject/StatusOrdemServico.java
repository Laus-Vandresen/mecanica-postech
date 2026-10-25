package br.com.mecanica.domain.valueObject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusOrdemServico {

    RECEBIDA("Recebida"),
    EM_DIAGNOSTICO("Em diagnóstico"),
    AGUARDANDO_APROVACAO("Aguardando aprovação"),
    EM_EXECUCAO("Em execução"),
    FINALIZADA("Finalizada"),
    ENTREGUE("Entregue"),
    RECUSADA("Recusada"),
    CANCELADA("Cancelada");

    private final String descricao;
}
