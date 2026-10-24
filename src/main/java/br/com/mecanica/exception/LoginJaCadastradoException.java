package br.com.mecanica.exception;

public class LoginJaCadastradoException extends RuntimeException {
    public LoginJaCadastradoException() {
        super("Login ja cadastrado");
    }
}
