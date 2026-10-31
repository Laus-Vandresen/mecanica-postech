package br.com.mecanica.domain.valueObject;

import br.com.mecanica.domain.exception.CpfInvalidoException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "11144477735"})
    void aceitaCpfComDigitosVerificadoresValidos(String valor) {
        assertDoesNotThrow(() -> new Cpf(valor));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "529982247250", "52998224724", "11144477734",
            "00000000000", "11111111111", "22222222222", "33333333333", "44444444444",
            "55555555555", "66666666666", "77777777777", "88888888888", "99999999999",
            "529.982.247-25", "5299822472A", " 52998224725", "52998224725 "})
    void rejeitaCpfComFormatoOuDigitosInvalidos(String valor) {
        assertThrows(CpfInvalidoException.class, () -> new Cpf(valor));
    }
}
