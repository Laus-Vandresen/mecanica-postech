package br.com.mecanica.domain.valueObject;
import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
public record DadosClienteAprovacao(String nome, String cpf, String celular, String email) {
    public DadosClienteAprovacao {
        if (nome == null || nome.isBlank() || nome.strip().length() > 120 || cpf == null || !cpf.matches("[0-9]{11}"))
            throw new DadosOrdemInvalidosException("Nome e CPF validos sao obrigatorios");
        if ((celular != null && celular.length() > 20) || (email != null && (email.length() > 120 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))))
            throw new DadosOrdemInvalidosException("Contato invalido");
        if (cpf.chars().distinct().count() == 1) throw new DadosOrdemInvalidosException("CPF invalido");
        for (int n = 9; n <= 10; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) soma += (cpf.charAt(i) - '0') * (n + 1 - i);
            int digito = (soma * 10) % 11;
            if ((digito == 10 ? 0 : digito) != cpf.charAt(n) - '0') throw new DadosOrdemInvalidosException("CPF invalido");
        }
        nome = nome.strip();
    }
}
