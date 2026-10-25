package br.com.mecanica.domain.valueObject;

public enum OperacaoEstoque {
    ENTRADA,
    SAIDA;

    public int aplicarSinal(int quantidade) {
        return this == ENTRADA ? quantidade : -quantidade;
    }
}
