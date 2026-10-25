package br.com.mecanica.controller.mapper;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.valueObject.*;
import br.com.mecanica.entity.*;
import br.com.mecanica.entity.mapper.OrdemServicoEntityMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
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
}
