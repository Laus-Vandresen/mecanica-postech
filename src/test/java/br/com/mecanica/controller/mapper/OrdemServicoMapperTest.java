package br.com.mecanica.controller.mapper;

import java.util.UUID;
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
    void requestParaDominioPreservaUUIDs() {
        var request = new OrdemServicoRequestDTOV1(uuid(1), uuid(2))
                .descricao("Barulho").servicoIds(List.of(uuid(3), uuid(4))).produtoIds(List.of(uuid(5)));
        var ordem = OrdemServicoMapper.toDomain(request);
        assertNull(ordem.getId());
        assertEquals(uuid(1), ordem.getClienteId());
        assertEquals(uuid(2), ordem.getVeiculoId());
        assertEquals(List.of(uuid(3), uuid(4)), ordem.getServicoIds());
        assertEquals(List.of(uuid(5)), ordem.getProdutoIds());
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
        entity.setId(uuid(9));
        var cliente = new ClienteEntity();
        cliente.setId(uuid(1));
        var veiculo = new VeiculoEntity();
        veiculo.setId(uuid(2));
        var servico = new ServicoEntity();
        servico.setId(uuid(3));
        var produto = new ProdutoEntity();
        produto.setId(uuid(5));
        entity.setCliente(cliente);
        entity.setVeiculo(veiculo);
        entity.setServicos(List.of(servico));
        entity.setProdutos(List.of(produto));
        entity.setDataAbertura(LocalDateTime.of(2026, 1, 2, 3, 4));
        entity.setStatus("ABERTA");
        entity.setValorTotal(new BigDecimal("10.50"));

        var response = OrdemServicoMapper.toResponse(OrdemServicoEntityMapper.toDomain(entity));
        assertEquals(uuid(9), response.getId());
        assertEquals(uuid(1), response.getClienteId());
        assertEquals(uuid(2), response.getVeiculoId());
        assertNull(response.getOrcamentoId());
        assertEquals(List.of(uuid(3)), response.getServicoIds());
        assertEquals(List.of(uuid(5)), response.getProdutoIds());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4), response.getDataAbertura().toLocalDateTime());
        assertEquals(10.5, response.getValorTotal());
    }

    @Test
    void dominioParaEntityNaoPreencheRelacoes() {
        var ordem = new OrdemServico();
        ordem.setClienteId(uuid(1));
        ordem.setServicoIds(List.of(uuid(3)));
        ordem.setDescricao("x");
        var entity = OrdemServicoEntityMapper.toEntity(ordem);
        assertNull(entity.getCliente());
        assertNull(entity.getServicos());
        assertEquals("x", entity.getDescricao());
    }
    private static UUID uuid(long value) {
        return new UUID(0L, value);
    }
}
