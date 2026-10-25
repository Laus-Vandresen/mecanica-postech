package br.com.mecanica.domain.valueObject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilaOrdemServico {

    DIAGNOSTICO("Aguardando início do diagnóstico"),
    EXECUCAO("Aprovada e aguardando início da execução"),
    FINALIZACAO("Execução concluída e aguardando finalização");

    private final String descricao;
}
