package br.com.mecanica.domain.exception;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException() {
        super("CPF invalido");
    }
}
