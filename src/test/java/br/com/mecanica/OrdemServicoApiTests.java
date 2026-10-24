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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:ordem-servico-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Import(JwtMockMvcTestConfig.class)
@ActiveProfiles("test")
class OrdemServicoApiTests {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 1, 1, 8, 0);

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    private UUID clienteId;
    private UUID veiculoId;

    @BeforeEach
    void prepararBancoDeTeste() throws Exception {
        for (var tabela : new String[]{"ordem_servico_servicos", "ordem_servico_produtos", "ordem_servico",
                "veiculo", "cliente", "pessoa"}) {
            jdbc.update("DELETE FROM " + tabela);
        }
        clienteId = criar("clientes", "{\"nome\":\"Cliente\",\"cpf\":\"52998224725\",\"email\":\"dev@example.com\",\"celular\":\"11999999999\"}");
        veiculoId = criar("veiculos", "{\"placa\":\"ABC1D23\",\"modelo\":\"Uno\",\"marca\":\"Fiat\",\"ano\":2020,\"clienteId\":\"" + clienteId + "\"}");
    }

    private UUID criar(String rota, String body) throws Exception {
        var result = mvc.perform(post("/v1/" + rota).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private void inserirOrdem(LocalDateTime abertura, LocalDateTime fechamento) {
        jdbc.update("INSERT INTO ordem_servico(id, cliente_id, veiculo_id, data_abertura, data_fechamento, status, valor_total) VALUES (?, ?, ?, ?, ?, 'FINALIZADA', 0)",
                UUID.randomUUID(), clienteId, veiculoId, abertura, fechamento);
    }

    @Test
    void tempoMedioSemOrdensFinalizadasRetornaZero() throws Exception {
        inserirOrdem(INICIO, null);
        mvc.perform(get("/v1/ordem-servico/tempo-medio-finalizacao")).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeOrdens").value(0))
                .andExpect(jsonPath("$.tempoMedioSegundos").value(0))
                .andExpect(jsonPath("$.tempoMedio").value("PT0S"));
    }

    @Test
    void tempoMedioConsideraApenasOrdensComDataDeFechamento() throws Exception {
        inserirOrdem(INICIO, INICIO.plusHours(1));
        inserirOrdem(INICIO, INICIO.plusHours(3));
        inserirOrdem(INICIO, null);
        mvc.perform(get("/v1/ordem-servico/tempo-medio-finalizacao")).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeOrdens").value(2))
                .andExpect(jsonPath("$.tempoMedioSegundos").value(7200))
                .andExpect(jsonPath("$.tempoMedio").value("PT2H"));
    }
}
