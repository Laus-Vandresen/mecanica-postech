package br.com.mecanica.controller.mapper;

import br.com.mecanica.api.dto.OrdemServicoPatchRequestDTOV1;
import br.com.mecanica.api.dto.OrdemServicoRequestDTOV1;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.entity.ClienteEntity;
import br.com.mecanica.entity.OrdemServicoEntity;
import br.com.mecanica.entity.ProdutoEntity;
import br.com.mecanica.entity.ServicoEntity;
import br.com.mecanica.entity.VeiculoEntity;
import br.com.mecanica.entity.mapper.OrdemServicoEntityMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoMapperTest {

    @Test
    void requestParaDominioConverteIdsParaLong() {
        var request = new OrdemServicoRequestDTOV1(1, 2)
                .descricao("Barulho").servicoIds(List.of(3, 4)).produtoIds(List.of(5));
        var ordem = OrdemServicoMapper.toDomain(request);
        assertNull(ordem.getId());
        assertEquals(1L, ordem.getClienteId());
        assertEquals(2L, ordem.getVeiculoId());
        assertEquals(List.of(3L, 4L), ordem.getServicoIds());
        assertEquals(List.of(5L), ordem.getProdutoIds());
        assertEquals("Barulho", ordem.getDescricao());
    }

    @Test
    void patchParaDominioMantemCamposNaoInformadosNulos() {
        var patch = new OrdemServicoPatchRequestDTOV1().diagnostico("ok").dataFechamento(OffsetDateTime.now());
        var ordem = OrdemServicoMapper.toDomain(patch);
        assertEquals("ok", ordem.getDiagnostico());
        assertNotNull(ordem.getDataFechamento());
        assertNull(ordem.getStatus());
        assertTrue(ordem.getServicoIds().isEmpty());
        assertNull(ordem.getValorTotal());
    }

    @Test
    void entityParaResponseExtraiIdsDasRelacoes() {
        var entity = new OrdemServicoEntity();
        entity.setId(9L);
        var cliente = new ClienteEntity();
        cliente.setId(1L);
        var veiculo = new VeiculoEntity();
        veiculo.setId(2L);
        var servico = new ServicoEntity();
        servico.setId(3L);
        var produto = new ProdutoEntity();
        produto.setId(5L);
        entity.setCliente(cliente);
        entity.setVeiculo(veiculo);
        entity.setServicos(List.of(servico));
        entity.setProdutos(List.of(produto));
        entity.setDataAbertura(LocalDateTime.of(2026, 1, 2, 3, 4));
        entity.setStatus("ABERTA");
        entity.setValorTotal(new BigDecimal("10.50"));

        var response = OrdemServicoMapper.toResponse(OrdemServicoEntityMapper.toDomain(entity));
        assertEquals(9, response.getId());
        assertEquals(1, response.getClienteId());
        assertEquals(2, response.getVeiculoId());
        assertNull(response.getOrcamentoId());
        assertEquals(List.of(3), response.getServicoIds());
        assertEquals(List.of(5), response.getProdutoIds());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4), response.getDataAbertura().toLocalDateTime());
        assertEquals(10.5, response.getValorTotal());
    }

    @Test
    void dominioParaEntityNaoPreencheRelacoes() {
        var ordem = new OrdemServico();
        ordem.setClienteId(1L);
        ordem.setServicoIds(List.of(3L));
        ordem.setDescricao("x");
        var entity = OrdemServicoEntityMapper.toEntity(ordem);
        assertNull(entity.getCliente());
        assertNull(entity.getServicos());
        assertEquals("x", entity.getDescricao());
    }
}
