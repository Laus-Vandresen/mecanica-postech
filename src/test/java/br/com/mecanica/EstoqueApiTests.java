package br.com.mecanica;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// UPDATE ... RETURNING e especifico do Postgres (o H2 nao suporta), entao este teste usa o banco real
@SpringBootTest
@AutoConfigureMockMvc
@Import(JwtMockMvcTestConfig.class)
@ActiveProfiles("test")
class EstoqueApiTests extends PostgresApiTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limparBancoDeTeste() {
        for (var tabela : new String[]{"ordem_servico_servicos", "ordem_servico_produtos", "ordem_servico",
                "veiculo", "cliente", "usuario", "pessoa", "peca", "insumo", "produto", "servico", "orcamento"}) {
            jdbc.update("DELETE FROM " + tabela);
        }
    }

    private UUID criarPeca(int estoque) throws Exception {
        var result = mvc.perform(post("/v1/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Filtro\",\"valor\":25.50,\"quantidadeEstoque\":" + estoque + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private int movimentar(UUID id, String operacao, int quantidade) throws Exception {
        return mvc.perform(post("/v1/produtos/" + id + "/estoque").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operacao\":\"" + operacao + "\",\"quantidade\":" + quantidade + "}"))
                .andReturn().getResponse().getStatus();
    }

    private int estoque(UUID id) {
        return jdbc.queryForObject("SELECT quantidade_estoque FROM produto WHERE id = ?", Integer.class, id);
    }

    @Test
    void entradaESaidaAtualizamEstoque() throws Exception {
        var id = criarPeca(10);
        mvc.perform(post("/v1/produtos/" + id + "/estoque").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operacao\":\"ENTRADA\",\"quantidade\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(id.toString()))
                .andExpect(jsonPath("$.quantidadeEstoque").value(15));
        mvc.perform(post("/v1/produtos/" + id + "/estoque").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operacao\":\"SAIDA\",\"quantidade\":15}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(0));
    }

    @Test
    void saidaMaiorQueEstoqueNaoAlteraSaldo() throws Exception {
        var id = criarPeca(3);
        assertEquals(422, movimentar(id, "SAIDA", 4));
        assertEquals(3, estoque(id));
    }

    @Test
    void produtoInexistenteEEntradaInvalida() throws Exception {
        assertEquals(404, movimentar(UUID.randomUUID(), "ENTRADA", 1));
        var id = criarPeca(3);
        assertEquals(400, movimentar(id, "ENTRADA", 0));
        assertEquals(400, movimentar(id, "TRANSFERENCIA", 1));
        assertEquals(3, estoque(id));
    }

    @Test
    void movimentacoesConcorrentesNaoPerdemAtualizacoes() throws Exception {
        var id = criarPeca(100);
        var threads = 40;
        var largada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(threads);
        try {
            var resultados = new ArrayList<Future<Integer>>();
            for (int i = 0; i < threads; i++) {
                var operacao = i % 2 == 0 ? "ENTRADA" : "SAIDA";
                resultados.add(executor.submit(() -> {
                    largada.await();
                    return movimentar(id, operacao, 3);
                }));
            }
            largada.countDown();
            for (var resultado : resultados) assertEquals(200, resultado.get());
        } finally {
            executor.shutdown();
        }
        assertEquals(100, estoque(id));
    }

    @Test
    void saidasConcorrentesNuncaDeixamEstoqueNegativo() throws Exception {
        var id = criarPeca(10);
        var threads = 30;
        var largada = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(threads);
        int sucessos = 0, recusas = 0;
        try {
            var resultados = new ArrayList<Future<Integer>>();
            for (int i = 0; i < threads; i++) {
                resultados.add(executor.submit(() -> {
                    largada.await();
                    return movimentar(id, "SAIDA", 1);
                }));
            }
            largada.countDown();
            for (var resultado : resultados) {
                var status = resultado.get();
                if (status == 200) sucessos++;
                else if (status == 422) recusas++;
            }
        } finally {
            executor.shutdown();
        }
        assertEquals(10, sucessos);
        assertEquals(20, recusas);
        assertEquals(0, estoque(id));
    }
}
