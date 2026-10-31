package br.com.mecanica;

import br.com.mecanica.domain.valueObject.ItemProdutoOrdemServico;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueOrdemServicoApiTests extends PostgresApiTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    private UUID cliente;
    private int placa;

    @BeforeEach
    void preparar() {
        for (var tabela : new String[]{"ordem_servico_servicos", "ordem_servico_produtos",
                "ordem_servico", "orcamento", "veiculo", "cliente", "usuario", "pessoa", "peca", "insumo", "produto", "servico"}) {
            jdbc.update("DELETE FROM " + tabela);
        }
        cliente = UUID.randomUUID();
        jdbc.update("INSERT INTO pessoa(id,nome) VALUES(?, 'Cliente')", cliente);
        jdbc.update("INSERT INTO cliente(id,cpf) VALUES(?, '11144477735')", cliente);
        placa = 0;
    }

    private ResultActions enviar(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.with(jwt()));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private UUID produto(int estoque) {
        return produto(estoque, false);
    }

    private UUID produto(int estoque, boolean insumo) {
        var id = UUID.randomUUID();
        jdbc.update("INSERT INTO produto(id,nome,valor,quantidade_estoque) VALUES(?,'Produto',10,?)", id, estoque);
        if (insumo) {
            jdbc.update("INSERT INTO insumo(id,unidade_medida) VALUES(?,'LITRO')", id);
        } else {
            jdbc.update("INSERT INTO peca(id,fabricante) VALUES(?,'Fabricante')", id);
        }
        return id;
    }

    private UUID abrir(ItemProdutoOrdemServico... itens) throws Exception {
        var veiculo = UUID.randomUUID();
        jdbc.update("INSERT INTO veiculo(id,placa,modelo) VALUES(?,?,'Modelo')", veiculo, "ABC" + String.format("%04d", ++placa));
        var produtos = List.of(itens).stream().map(item -> "{\"produtoId\":\"" + item.produtoId()
                + "\",\"quantidade\":" + item.quantidade() + "}").collect(Collectors.joining(","));
        var resultado = enviar(json(post("/v1/ordem-servico"), "{\"veiculoId\":\"" + veiculo
                + "\",\"descricao\":\"Revisao\",\"produtos\":[" + produtos + "]}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(resultado, "$.id"));
    }

    private void aguardar(UUID os) throws Exception {
        acao(os, "diagnostico/inicio").andExpect(status().isOk());
        enviar(json(patch("/v1/ordem-servico/" + os + "/diagnostico"), "{\"diagnostico\":\"Troca de produtos\"}"))
                .andExpect(status().isOk());
        var orcamento = UUID.randomUUID();
        jdbc.update("INSERT INTO orcamento(id,valor) VALUES(?,100)", orcamento);
        enviar(json(post("/v1/ordem-servico/" + os + "/orcamento"), "{\"orcamentoId\":\"" + orcamento + "\"}"))
                .andExpect(status().isOk());
    }

    private ResultActions aprovar(UUID os) throws Exception {
        return enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{\"clienteId\":\"" + cliente + "\"}"));
    }

    private ResultActions acao(UUID os, String acao) throws Exception {
        return enviar(post("/v1/ordem-servico/" + os + "/" + acao));
    }

    private ResultActions cancelar(UUID os) throws Exception {
        return enviar(json(post("/v1/ordem-servico/" + os + "/cancelamento"), "{\"motivo\":\"Cancelado pelo cliente\"}"));
    }

    private void saldo(UUID produto, int fisico) {
        assertEquals(fisico, jdbc.queryForObject("SELECT quantidade_estoque FROM produto WHERE id=?", Integer.class, produto));
    }

    private ResultActions consumir(UUID os, UUID produto, int total) throws Exception {
        return enviar(json(put("/v1/ordem-servico/" + os + "/produtos/" + produto + "/consumo"),
                "{\"quantidadeConsumida\":" + total + "}"));
    }

    private void consumo(UUID os, UUID produto, int total) {
        assertEquals(total, jdbc.queryForObject("SELECT quantidade_consumida FROM ordem_servico_produtos "
                + "WHERE ordem_servico_entity_id=? AND produtos_id=?", Integer.class, os, produto));
    }

    private UUID emExecucao(ItemProdutoOrdemServico... itens) throws Exception {
        var os = abrir(itens);
        aguardar(os);
        aprovar(os).andExpect(status().isOk());
        acao(os, "execucao/inicio").andExpect(status().isOk());
        return os;
    }

    private List<Integer> concorrentes(List<Callable<Integer>> operacoes) throws Exception {
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(operacoes.size())) {
            var resultados = operacoes.stream().map(operacao -> executor.submit(() -> {
                largada.await();
                return operacao.call();
            })).toList();
            largada.countDown();
            var status = new java.util.ArrayList<Integer>();
            for (var resultado : resultados) status.add(resultado.get(30, TimeUnit.SECONDS));
            return status;
        }
    }

    @Test
    void baixaImediataNoUsoSemEsperarConcluirAExecucao() throws Exception {
        var peca = produto(10);
        var insumo = produto(8, true);
        var os = emExecucao(new ItemProdutoOrdemServico(peca, 3), new ItemProdutoOrdemServico(insumo, 4));
        saldo(peca, 10);
        consumir(os, peca, 1).andExpect(status().isOk()).andExpect(jsonPath("$.dataConclusaoExecucao").isEmpty());
        saldo(peca, 9);
        consumo(os, peca, 1);
        consumir(os, peca, 1).andExpect(status().isOk());
        saldo(peca, 9);
        consumir(os, peca, 3).andExpect(status().isOk());
        saldo(peca, 7);
        consumir(os, insumo, 4).andExpect(status().isOk());
        saldo(insumo, 4);
        acao(os, "execucao/conclusao").andExpect(status().isOk());
        acao(os, "finalizacao").andExpect(status().isOk());
        acao(os, "entrega").andExpect(status().isOk());
        saldo(peca, 7);
        saldo(insumo, 4);
        consumo(os, peca, 3);
    }

    @Test
    void faltaDeSaldoNaoImpedeAprovacaoMasReverteConsumo() throws Exception {
        var produto = produto(0);
        var os = emExecucao(new ItemProdutoOrdemServico(produto, 3));
        consumir(os, produto, 2).andExpect(status().isUnprocessableEntity());
        saldo(produto, 0);
        consumo(os, produto, 0);
        enviar(json(post("/v1/produtos/" + produto + "/estoque"), "{\"operacao\":\"ENTRADA\",\"quantidade\":2}"))
                .andExpect(status().isOk());
        consumir(os, produto, 2).andExpect(status().isOk());
        saldo(produto, 0);
        consumo(os, produto, 2);
        acao(os, "execucao/conclusao").andExpect(status().isOk());
        saldo(produto, 0);
    }

    @Test
    void conclusaoSemUsoDeProdutoNaoFazBaixaAutomatica() throws Exception {
        var produto = produto(10);
        var os = emExecucao(new ItemProdutoOrdemServico(produto, 3));
        acao(os, "execucao/conclusao").andExpect(status().isOk());
        acao(os, "finalizacao").andExpect(status().isOk());
        acao(os, "entrega").andExpect(status().isOk());
        saldo(produto, 10);
        consumo(os, produto, 0);
    }

    @Test
    void aprovacaoRecusaECancelamentoNaoMovimentamEstoque() throws Exception {
        var produto = produto(1);
        var cancelada = abrir(new ItemProdutoOrdemServico(produto, 5));
        aguardar(cancelada);
        aprovar(cancelada).andExpect(status().isOk());
        saldo(produto, 1);
        cancelar(cancelada).andExpect(status().isOk());
        cancelar(cancelada).andExpect(status().isConflict());
        var recusada = abrir(new ItemProdutoOrdemServico(produto, 5));
        aguardar(recusada);
        enviar(json(post("/v1/ordem-servico/" + recusada + "/recusa"), "{\"motivo\":\"Recusado\"}"))
                .andExpect(status().isOk());
        saldo(produto, 1);
    }

    @Test
    void osAprovadaNaoBloqueiaSaidaManualDeEstoque() throws Exception {
        var produto = produto(5);
        var os = abrir(new ItemProdutoOrdemServico(produto, 4));
        aguardar(os);
        aprovar(os).andExpect(status().isOk());
        enviar(json(post("/v1/produtos/" + produto + "/estoque"), "{\"operacao\":\"SAIDA\",\"quantidade\":5}"))
                .andExpect(status().isOk());
        saldo(produto, 0);
        acao(os, "execucao/inicio").andExpect(status().isOk());
        consumir(os, produto, 1).andExpect(status().isUnprocessableEntity());
        consumo(os, produto, 0);
    }

    @Test
    void duasOsConcorrentesNaoDeixamEstoqueNegativo() throws Exception {
        var produto = produto(5);
        var primeira = emExecucao(new ItemProdutoOrdemServico(produto, 4));
        var segunda = emExecucao(new ItemProdutoOrdemServico(produto, 4));
        var respostas = concorrentes(List.of(
                () -> consumir(primeira, produto, 4).andReturn().getResponse().getStatus(),
                () -> consumir(segunda, produto, 4).andReturn().getResponse().getStatus()));
        assertEquals(List.of(200, 422), respostas.stream().sorted().toList());
        saldo(produto, 1);
        assertEquals(4, jdbc.queryForObject("SELECT SUM(quantidade_consumida) FROM ordem_servico_produtos WHERE produtos_id=?", Integer.class, produto));
    }

    @Test
    void repeticaoConcorrenteDoMesmoTotalDescontaUmaVez() throws Exception {
        var produto = produto(10);
        var os = emExecucao(new ItemProdutoOrdemServico(produto, 3));
        var respostas = concorrentes(List.of(
                () -> consumir(os, produto, 3).andReturn().getResponse().getStatus(),
                () -> consumir(os, produto, 3).andReturn().getResponse().getStatus()));
        assertEquals(List.of(200, 200), respostas);
        saldo(produto, 7);
        consumo(os, produto, 3);
    }

    @Test
    void totaisConcorrentesNaoPerdemConsumoJaRegistrado() throws Exception {
        var produto = produto(10);
        var os = emExecucao(new ItemProdutoOrdemServico(produto, 4));
        var respostas = concorrentes(List.of(
                () -> consumir(os, produto, 2).andReturn().getResponse().getStatus(),
                () -> consumir(os, produto, 4).andReturn().getResponse().getStatus()));
        assertTrue(respostas.stream().allMatch(status -> status == 200 || status == 409));
        saldo(produto, 6);
        consumo(os, produto, 4);
    }

    @Test
    void totalNaoPodeDiminuirNemSuperarQuantidadeDaOs() throws Exception {
        var produto = produto(10);
        var os = emExecucao(new ItemProdutoOrdemServico(produto, 3));
        consumir(os, produto, 2).andExpect(status().isOk())
                .andExpect(jsonPath("$.produtos[0].quantidadeConsumida").value(2));
        consumir(os, produto, 1).andExpect(status().isConflict());
        consumir(os, produto, 4).andExpect(status().isConflict());
        saldo(produto, 8);
        consumo(os, produto, 2);
    }

    @Test
    void consumoSomenteDuranteExecucaoAberta() throws Exception {
        var produto = produto(10);
        var os = abrir(new ItemProdutoOrdemServico(produto, 3));
        consumir(os, produto, 1).andExpect(status().isConflict());
        aguardar(os);
        aprovar(os).andExpect(status().isOk());
        consumir(os, produto, 1).andExpect(status().isConflict());
        acao(os, "execucao/inicio").andExpect(status().isOk());
        consumir(os, produto, 1).andExpect(status().isOk());
        cancelar(os).andExpect(status().isConflict());
        acao(os, "execucao/conclusao").andExpect(status().isOk());
        consumir(os, produto, 2).andExpect(status().isConflict());
        saldo(produto, 9);
        consumo(os, produto, 1);
    }

    @Test
    void consumoExigeJwtEQuantidadeValida() throws Exception {
        var produto = produto(10);
        var os = emExecucao(new ItemProdutoOrdemServico(produto, 3));
        var rota = "/v1/ordem-servico/" + os + "/produtos/" + produto + "/consumo";
        mvc.perform(json(put(rota), "{\"quantidadeConsumida\":1}")).andExpect(status().isUnauthorized());
        for (var body : new String[]{"{}", "{\"quantidadeConsumida\":0}", "{\"quantidadeConsumida\":-1}"}) {
            enviar(json(put(rota), body)).andExpect(status().isBadRequest());
        }
        consumir(os, UUID.randomUUID(), 1).andExpect(status().isBadRequest());
        saldo(produto, 10);
        consumo(os, produto, 0);
    }

    @Test
    void diagnosticoAlteraQuantidadeSemBaixaEValidaItens() throws Exception {
        var produto = produto(10);
        var os = abrir(new ItemProdutoOrdemServico(produto, 1));
        acao(os, "diagnostico/inicio").andExpect(status().isOk());
        enviar(json(patch("/v1/ordem-servico/" + os + "/diagnostico"),
                "{\"diagnostico\":\"Quatro unidades\",\"produtos\":[{\"produtoId\":\"" + produto + "\",\"quantidade\":4}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.produtos[0].quantidade").value(4))
                .andExpect(jsonPath("$.produtos[0].quantidadeConsumida").value(0));
        for (var itens : new String[]{
                "[{\"produtoId\":\"" + produto + "\",\"quantidade\":0}]",
                "[{\"produtoId\":\"" + produto + "\",\"quantidade\":-1}]",
                "[{\"produtoId\":\"" + produto + "\"}]",
                "[{\"produtoId\":\"" + produto + "\",\"quantidade\":1},{\"produtoId\":\"" + produto + "\",\"quantidade\":2}]",
                "[null]"}) {
            enviar(json(patch("/v1/ordem-servico/" + os + "/diagnostico"),
                    "{\"diagnostico\":\"Teste\",\"produtos\":" + itens + "}"))
                    .andExpect(status().isBadRequest());
        }
        saldo(produto, 10);
    }
}
