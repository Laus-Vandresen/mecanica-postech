package br.com.mecanica.controller.mapper;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.exception.DadosOrdemInvalidosException;
import br.com.mecanica.domain.valueObject.*;
import br.com.mecanica.entity.*;
import br.com.mecanica.entity.mapper.OrdemServicoEntityMapper;
import br.com.mecanica.api.dto.ClienteAprovacaoDTOV1;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class OrdemServicoMapperTest {
    @Test
    void mapeamentoPreservaOsSemClienteComUuid() {
        var entity = new OrdemServicoEntity(); var veiculo = new VeiculoEntity(); veiculo.setId(UUID.randomUUID());
        entity.setId(UUID.randomUUID()); entity.setVeiculo(veiculo); entity.setStatus("RECEBIDA");
        entity.setDataAbertura(LocalDateTime.of(2026, 1, 2, 3, 4)); entity.setDescricao("Teste"); entity.setValorTotal(BigDecimal.ZERO);
        var response = OrdemServicoMapper.toResponse(OrdemServicoEntityMapper.toDomain(entity));
        assertNull(response.getClienteId()); assertNull(response.getOrcamentoId()); assertEquals(entity.getId(), response.getId());
        assertEquals(veiculo.getId(), response.getVeiculoId()); assertEquals("RECEBIDA", response.getStatus());
        assertEquals(entity.getDataAbertura(), response.getDataAbertura().toLocalDateTime());
    }
    @Test
    void respostaExibeSomenteDadosDoFluxo() {
        var agora = LocalDateTime.now();
        var ordem = OrdemServico.abrir(UUID.randomUUID(), "Teste", List.of(), List.of(), agora);
        ordem.iniciarDiagnostico();
        var response = OrdemServicoMapper.toResponse(ordem);
        assertEquals("EM_DIAGNOSTICO", response.getStatus());
        assertFalse(response.getOrcamentoAprovado());
        assertNull(response.getDataConclusaoExecucao());
    }

    @Test
    void mapeiaDadosDoClienteOpcionalEPreservaCampos() {
        assertNull(OrdemServicoMapper.toDomain(null));
        var dto = new ClienteAprovacaoDTOV1("Ana", "52998224725");
        dto.setCelular("11999999999");
        dto.setEmail("ana@example.com");

        assertEquals(new DadosClienteAprovacao("Ana", "52998224725", "11999999999", "ana@example.com"),
                OrdemServicoMapper.toDomain(dto));
    }

    @Test
    void converteStatusEFilaConhecidosERejeitaValoresDesconhecidos() {
        assertNull(OrdemServicoMapper.toStatus(null));
        assertNull(OrdemServicoMapper.toFila(null));
        assertEquals(StatusOrdemServico.RECUSADA, OrdemServicoMapper.toStatus("RECUSADA"));
        assertEquals(FilaOrdemServico.DIAGNOSTICO, OrdemServicoMapper.toFila("DIAGNOSTICO"));
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServicoMapper.toStatus("INVALIDO"));
        assertThrows(DadosOrdemInvalidosException.class, () -> OrdemServicoMapper.toFila("INVALIDA"));
    }

    @Test
    void mapeiaListasVaziasETempoMedioEmSegundosENotacaoLegivel() {
        assertTrue(OrdemServicoMapper.toResponse(List.of()).isEmpty());
        var tempo = OrdemServicoMapper.toResponse(new TempoMedioFinalizacao(3, Duration.ofMinutes(90)));
        assertEquals(3, tempo.getQuantidadeOrdens());
        assertEquals(5400L, tempo.getTempoMedioSegundos());
        assertEquals("PT1H30M", tempo.getTempoMedio());
    }
}
