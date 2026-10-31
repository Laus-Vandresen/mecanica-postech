package br.com.mecanica.domain.valueObject;

import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;

public record DadosClienteAprovacao(String nome, String cpf, String celular, String email) {

    public DadosClienteAprovacao {
        if (nome == null || nome.isBlank() || nome.strip().length() > 120) {
            throw new DadosOrdemInvalidosException("Nome valido e obrigatorio");
        }
        cpf = new Cpf(cpf).valor();
        if ((celular != null && celular.length() > 20)
                || (email != null && (email.length() > 120 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")))) {
            throw new DadosOrdemInvalidosException("Contato invalido");
        }
        nome = nome.strip();
    }
}
