package br.com.mecanica.domain.valueObject;

import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DadosClienteAprovacaoTest {

    @Test
    void normalizaEspacosDoNomeEPreservaContatosOpcionais() {
        var dados = new DadosClienteAprovacao("  Ana Maria  ", "52998224725", "11999999999", "ana@example.com");

        assertEquals("Ana Maria", dados.nome());
        assertEquals("52998224725", dados.cpf());
        assertEquals("11999999999", dados.celular());
        assertEquals("ana@example.com", dados.email());
    }

    @Test
    void exigeNomeNaoVazioEComTamanhoMaximo() {
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao(null, "52998224725", null, null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("  ", "52998224725", null, null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("a".repeat(121), "52998224725", null, null));
        assertDoesNotThrow(() -> new DadosClienteAprovacao("a".repeat(120), "52998224725", null, null));
    }

    @Test
    void exigeCpfComOnzeDigitosEValidacaoDosDigitosVerificadores() {
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", null, null, null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "5299822472", null, null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "5299822472A", null, null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "11111111111", null, null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "52998224724", null, null));
        assertDoesNotThrow(() -> new DadosClienteAprovacao("Ana", "52998224725", null, null));
    }

    @Test
    void validaTamanhoDoCelularEFormatoDoEmailQuandoInformados() {
        assertDoesNotThrow(() -> new DadosClienteAprovacao("Ana", "52998224725", null, null));
        assertDoesNotThrow(() -> new DadosClienteAprovacao("Ana", "52998224725", "1".repeat(20), "a@b.co"));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "52998224725", "1".repeat(21), null));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "52998224725", null, "sem-arroba"));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "52998224725", null, "a b@c.co"));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "52998224725", null, "a@b"));
        assertThrows(DadosOrdemInvalidosException.class,
                () -> new DadosClienteAprovacao("Ana", "52998224725", null, "a".repeat(116) + "@b.co"));
    }
}
