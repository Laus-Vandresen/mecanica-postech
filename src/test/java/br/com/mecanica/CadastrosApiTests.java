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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.stream.Stream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:cadastros-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Import(JwtMockMvcTestConfig.class)
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
        return Stream.of("clientes", "veiculos", "pecas", "insumos");
    }

    private UUID criar(String rota, String body) throws Exception {
        var result = mvc.perform(post("/v1/" + rota).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private String dados(String rota, boolean atualizado, UUID clienteId) {
        var nome = atualizado ? "Atualizado" : "Cadastro";
        return switch (rota) {
            case "clientes" -> "{\"nome\":\"" + nome + "\",\"cpf\":\"52998224725\",\"email\":\"dev@example.com\",\"celular\":\"11999999999\"}";
            case "veiculos" -> "{\"placa\":\"ABC1D23\",\"modelo\":\"" + nome + "\",\"marca\":\"Fiat\",\"ano\":2020,\"clienteId\":\"" + clienteId + "\"}";
            case "servicos" -> "{\"nome\":\"" + nome + "\",\"valor\":150.00}";
            case "pecas" -> "{\"nome\":\"" + nome + "\",\"valor\":25.50,\"quantidadeEstoque\":10,\"fabricante\":\"Fabricante\"}";
            case "insumos" -> "{\"nome\":\"" + nome + "\",\"valor\":12.50,\"quantidadeEstoque\":5,\"unidadeMedida\":\"L\"}";
            default -> throw new IllegalArgumentException();
        };
    }

    @ParameterizedTest
    @MethodSource("rotas")
    void cicloCompletoDeCadastro(String rota) throws Exception {
        UUID clienteId = rota.equals("veiculos") ? criar("clientes", dados("clientes", false, null)) : null;
        UUID id = criar(rota, dados(rota, false, clienteId));
        mvc.perform(get("/v1/" + rota + "/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.toString()));
        mvc.perform(get("/v1/" + rota)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        if (rota.equals("veiculos")) {
            mvc.perform(get("/v1/veiculos/" + id)).andExpect(jsonPath("$.placa").value("ABC1D23"))
                    .andExpect(jsonPath("$.clienteId").value(clienteId.toString())).andExpect(jsonPath("$.cliente").doesNotExist());
        }
        mvc.perform(delete("/v1/" + rota + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/v1/" + rota + "/" + id)).andExpect(status().isNotFound());
        if (rota.equals("clientes")) assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM pessoa", Integer.class));
        if (rota.equals("pecas") || rota.equals("insumos")) assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM produto", Integer.class));
    }

    @ParameterizedTest
    @MethodSource("rotas")
    void recursoInexistenteEEntradaInvalida(String rota) throws Exception {
        mvc.perform(get("/v1/" + rota + "/" + UUID.randomUUID())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists());
        mvc.perform(delete("/v1/" + rota + "/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(post("/v1/" + rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").isMap());
        mvc.perform(post("/v1/" + rota).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Corpo da requisicao invalido ou malformado."));
        mvc.perform(get("/v1/" + rota + "/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Parametro invalido ou em formato incorreto."));
        mvc.perform(get("/v1/" + rota + "/123")).andExpect(status().isBadRequest());
    }

    @Test
    void duplicidadeEVinculoDeCliente() throws Exception {
        UUID cliente = criar("clientes", dados("clientes", false, null));
        mvc.perform(post("/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(dados("clientes", false, null)))
                .andExpect(status().isConflict());
        UUID veiculo = criar("veiculos", dados("veiculos", false, cliente));
        mvc.perform(post("/v1/veiculos").contentType(MediaType.APPLICATION_JSON).content(dados("veiculos", false, cliente)))
                .andExpect(status().isConflict());
        mvc.perform(delete("/v1/clientes/" + cliente)).andExpect(status().isConflict());
        mvc.perform(get("/v1/clientes/" + cliente)).andExpect(status().isOk());
        mvc.perform(delete("/v1/veiculos/" + veiculo)).andExpect(status().isNoContent());
        mvc.perform(delete("/v1/clientes/" + cliente)).andExpect(status().isNoContent());
    }

    @Test
    void validaCpfPlacaEstoqueEValor() throws Exception {
        mvc.perform(post("/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Cliente\",\"cpf\":\"123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.cpf").exists());
        mvc.perform(post("/v1/veiculos").contentType(MediaType.APPLICATION_JSON).content(dados("veiculos", false, UUID.randomUUID())))
                .andExpect(status().isNotFound());
        for (String rota : new String[]{"pecas", "insumos"}) {
            mvc.perform(post("/v1/" + rota).contentType(MediaType.APPLICATION_JSON)
                    .content(dados(rota, false, null).replace("25.50", "-1.00").replace("12.50", "-1.00")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.valor").exists());
            mvc.perform(post("/v1/" + rota).contentType(MediaType.APPLICATION_JSON)
                    .content(dados(rota, false, null).replace("Estoque\":10", "Estoque\":-1").replace("Estoque\":5", "Estoque\":-1")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.quantidadeEstoque").exists());
        }
        mvc.perform(post("/v1/veiculos").contentType(MediaType.APPLICATION_JSON)
                .content(dados("veiculos", false, UUID.randomUUID()).replace("ABC1D23", "invalida")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.placa").exists());
    }
    @Test
    void cadastroRejeitaCpfComOnzeDigitosMasInvalido() throws Exception {
        for (var cpf : new String[]{"52998224724", "11111111111"}) {
            var body = dados("clientes", false, null).replace("52998224725", cpf);
            mvc.perform(post("/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("CPF invalido"));
        }
        mvc.perform(get("/v1/clientes")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rejeitaReferenciaDeClienteSemFormatoUuid() throws Exception {
        var body = dados("veiculos", false, UUID.randomUUID())
                .replaceFirst("[0-9a-f]{8}-[0-9a-f-]{27}", "123");
        mvc.perform(post("/v1/veiculos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
