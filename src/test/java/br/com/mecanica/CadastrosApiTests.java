package br.com.mecanica;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:cadastros-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CadastrosApiTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void limparBancoDeTeste() {
        for (var tabela : new String[]{"ordem_servico_servicos", "ordem_servico_produtos", "ordem_servico",
                "veiculo", "cliente", "usuario", "pessoa", "peca", "insumo", "produto", "servico", "orcamento"}) {
            jdbc.update("DELETE FROM " + tabela);
        }
    }

    static Stream<String> rotas() {
        return Stream.of("clientes", "veiculos", "servicos", "pecas", "insumos");
    }

    private long criar(String rota, String body) throws Exception {
        var result = mvc.perform(post("/" + rota).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String dados(String rota, boolean atualizado, long clienteId) {
        var nome = atualizado ? "Atualizado" : "Cadastro";
        return switch (rota) {
            case "clientes" -> "{\"nome\":\"" + nome + "\",\"cpf\":\"52998224725\",\"email\":\"dev@example.com\"}";
            case "veiculos" -> "{\"placa\":\"abc1d23\",\"modelo\":\"" + nome + "\",\"marca\":\"Fiat\",\"ano\":2020,\"clienteId\":" + clienteId + "}";
            case "servicos" -> "{\"nome\":\"" + nome + "\",\"valor\":150.00}";
            case "pecas" -> "{\"nome\":\"" + nome + "\",\"valor\":25.50,\"quantidadeEstoque\":10,\"fabricante\":\"Fabricante\"}";
            case "insumos" -> "{\"nome\":\"" + nome + "\",\"valor\":12.50,\"quantidadeEstoque\":5,\"unidadeMedida\":\"L\"}";
            default -> throw new IllegalArgumentException();
        };
    }

    @ParameterizedTest
    @MethodSource("rotas")
    void cicloCompletoDeCadastro(String rota) throws Exception {
        long clienteId = rota.equals("veiculos") ? criar("clientes", dados("clientes", false, 0)) : 0;
        long id = criar(rota, dados(rota, false, clienteId));
        mvc.perform(get("/" + rota + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/" + rota)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put("/" + rota + "/" + id).contentType(MediaType.APPLICATION_JSON).content(dados(rota, true, clienteId)))
                .andExpect(status().isOk()).andExpect(jsonPath(rota.equals("veiculos") ? "$.modelo" : "$.nome").value("Atualizado"));
        if (rota.equals("veiculos")) {
            mvc.perform(get("/veiculos/" + id)).andExpect(jsonPath("$.placa").value("ABC1D23"))
                    .andExpect(jsonPath("$.clienteId").value(clienteId)).andExpect(jsonPath("$.cliente").doesNotExist());
        }
        mvc.perform(delete("/" + rota + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/" + rota + "/" + id)).andExpect(status().isNotFound());
        // JOINED: a exclusao da especializacao deve remover tambem a linha da tabela base.
        if (rota.equals("clientes")) assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM pessoa", Integer.class));
        if (rota.equals("pecas") || rota.equals("insumos")) assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM produto", Integer.class));
    }

    @ParameterizedTest
    @MethodSource("rotas")
    void recursoInexistenteEEntradaInvalida(String rota) throws Exception {
        mvc.perform(get("/" + rota + "/999999")).andExpect(status().isNotFound());
        mvc.perform(delete("/" + rota + "/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/" + rota + "/999999").contentType(MediaType.APPLICATION_JSON).content(dados(rota, true, 999999)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/" + rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").isMap());
        mvc.perform(post("/" + rota).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/" + rota + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void duplicidadeEVinculoDeCliente() throws Exception {
        long cliente = criar("clientes", dados("clientes", false, 0));
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(dados("clientes", false, 0)))
                .andExpect(status().isConflict());
        long veiculo = criar("veiculos", dados("veiculos", false, cliente));
        mvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(dados("veiculos", false, cliente)))
                .andExpect(status().isConflict());
        mvc.perform(delete("/clientes/" + cliente)).andExpect(status().isConflict());
        mvc.perform(get("/clientes/" + cliente)).andExpect(status().isOk());
        mvc.perform(delete("/veiculos/" + veiculo)).andExpect(status().isNoContent());
        mvc.perform(delete("/clientes/" + cliente)).andExpect(status().isNoContent());
    }

    @Test
    void validaCpfPlacaEstoqueEValor() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Cliente\",\"cpf\":\"11111111111\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cpf").exists());
        mvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(dados("veiculos", false, 999999)))
                .andExpect(status().isNotFound());
        for (String rota : new String[]{"pecas", "insumos"}) {
            mvc.perform(post("/" + rota).contentType(MediaType.APPLICATION_JSON)
                    .content(dados(rota, false, 0).replace("25.50", "-1.00").replace("12.50", "-1.00")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valor").exists());
            mvc.perform(post("/" + rota).contentType(MediaType.APPLICATION_JSON)
                    .content(dados(rota, false, 0).replace("Estoque\":10", "Estoque\":-1").replace("Estoque\":5", "Estoque\":-1")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.quantidadeEstoque").exists());
        }
        mvc.perform(post("/servicos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"   \",\"valor\":1.001}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.valor").exists());
        mvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
                .content(dados("veiculos", false, 1).replace("abc1d23", "invalida")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.placa").exists());
    }
}
