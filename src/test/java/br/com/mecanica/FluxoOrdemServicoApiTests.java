package br.com.mecanica;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import java.time.Instant;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:fluxo-os-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoOrdemServicoApiTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired JwtEncoder jwtEncoder;
    @BeforeEach void limpar() {
        for (var tabela : new String[]{"ordem_servico_servicos", "ordem_servico_produtos", "ordem_servico", "veiculo", "cliente", "usuario", "pessoa", "orcamento", "servico"}) jdbc.update("DELETE FROM " + tabela);
    }
    private ResultActions enviar(MockHttpServletRequestBuilder req) throws Exception {
        return mvc.perform(req.with(jwt()));
    }
    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String body) { return req.contentType(MediaType.APPLICATION_JSON).content(body); }
    private UUID id(ResultActions result) throws Exception { return UUID.fromString(JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")); }
    private UUID abrir() throws Exception {
        var veiculo = id(enviar(json(post("/v1/veiculos"), "{\"placa\":\"ABC1D23\",\"modelo\":\"Uno\",\"marca\":\"Fiat\",\"ano\":2020}")).andExpect(status().isCreated()));
        var ordem = enviar(json(post("/v1/ordem-servico"), "{\"veiculoId\":\"" + veiculo + "\",\"descricao\":\"Barulho\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.clienteId").isEmpty());
        return id(ordem);
    }
    private UUID aguardar() throws Exception {
        return aguardar(abrir());
    }
    private UUID aguardar(UUID os) throws Exception {
        enviar(post("/v1/ordem-servico/" + os + "/diagnostico/inicio")).andExpect(status().isOk());
        enviar(json(patch("/v1/ordem-servico/" + os + "/diagnostico"), "{\"diagnostico\":\"Trocar filtro\"}")).andExpect(status().isOk());
        var orcamento = UUID.randomUUID(); jdbc.update("INSERT INTO orcamento(id,valor) VALUES(?,?)", orcamento, new java.math.BigDecimal("50.00"));
        enviar(json(post("/v1/ordem-servico/" + os + "/orcamento"), "{\"orcamentoId\":\"" + orcamento + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));
        return os;
    }
    private void aprovar(UUID os) throws Exception {
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{\"cliente\":{\"nome\":\"Ana\",\"cpf\":\"52998224725\",\"email\":\"ana@example.com\"}}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clienteId").isNotEmpty());
    }
    @Test void fluxoCompletoPreservaUuidClienteEVeiculo() throws Exception {
        var os = aguardar();
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM servico", Integer.class));
        aprovar(os);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM ordem_servico o JOIN veiculo v ON o.veiculo_id=v.id AND o.cliente_id=v.cliente_id WHERE o.id=?", Integer.class, os));
        enviar(post("/v1/ordem-servico/" + os + "/execucao/inicio")).andExpect(status().isOk());
        enviar(post("/v1/ordem-servico/" + os + "/finalizacao")).andExpect(status().isConflict());
        enviar(post("/v1/ordem-servico/" + os + "/execucao/conclusao")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO")).andExpect(jsonPath("$.dataFechamento").isEmpty());
        enviar(post("/v1/ordem-servico/" + os + "/finalizacao")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FINALIZADA"));
        enviar(post("/v1/ordem-servico/" + os + "/entrega")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ENTREGUE"));
        enviar(get("/v1/ordem-servico/" + os)).andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamentoAprovado").value(true))
                .andExpect(jsonPath("$.dataConclusaoExecucao").isNotEmpty())
                .andExpect(jsonPath("$.historico").doesNotExist())
                .andExpect(jsonPath("$.marcos").doesNotExist());
    }
    @Test void recusaEncerraSemCadastrarClienteENaoPodeSerReaberta() throws Exception {
        var os = aguardar();
        enviar(json(post("/v1/ordem-servico/" + os + "/recusa"), "{\"motivo\":\"Cliente recusou no canal externo\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RECUSADA"));
        enviar(post("/v1/ordem-servico/" + os + "/execucao/inicio")).andExpect(status().isConflict());
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{\"cliente\":{\"nome\":\"Ana\",\"cpf\":\"52998224725\"}}"))
                .andExpect(status().isConflict());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM ordem_servico", Integer.class));
    }
    @Test void cancelamentoPreservaOsEExigeMotivo() throws Exception {
        var os = abrir();
        enviar(json(post("/v1/ordem-servico/" + os + "/cancelamento"), "{\"motivo\":\"  \"}")).andExpect(status().isBadRequest());
        enviar(json(post("/v1/ordem-servico/" + os + "/cancelamento"), "{\"motivo\":\"Desistencia\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM ordem_servico", Integer.class));
    }
    @Test void filtrosEPaginacaoRealmenteFiltram() throws Exception {
        var os = abrir();
        enviar(get("/v1/ordem-servico").param("status", "RECEBIDA")).andExpect(jsonPath("$.length()").value(1));
        enviar(get("/v1/ordem-servico").param("status", "FINALIZADA")).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("clienteId", UUID.randomUUID().toString())).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("pagina", "1").param("tamanho", "1")).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("status", "QUALQUER")).andExpect(status().isBadRequest());
    }
    @Test void filtrosCombinadosRespeitamClienteVeiculoEStatus() throws Exception {
        var os = aguardar();
        aprovar(os);
        var cliente = jdbc.queryForObject("SELECT cliente_id FROM ordem_servico WHERE id=?", UUID.class, os);
        var veiculo = jdbc.queryForObject("SELECT veiculo_id FROM ordem_servico WHERE id=?", UUID.class, os);
        var semCliente = id(enviar(json(post("/v1/ordem-servico"),
                "{\"veiculoId\":\"" + veiculo + "\",\"descricao\":\"Outra OS\"}")).andExpect(status().isCreated()));

        enviar(get("/v1/ordem-servico")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        enviar(get("/v1/ordem-servico").param("clienteId", cliente.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(os.toString()));
        enviar(get("/v1/ordem-servico").param("veiculoId", veiculo.toString()).param("status", "RECEBIDA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(semCliente.toString()));
        enviar(get("/v1/ordem-servico").param("clienteId", cliente.toString())
                .param("veiculoId", veiculo.toString()).param("status", "RECEBIDA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("veiculoId", UUID.randomUUID().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));

        var primeira = enviar(get("/v1/ordem-servico").param("pagina", "0").param("tamanho", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        var segunda = enviar(get("/v1/ordem-servico").param("pagina", "1").param("tamanho", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        assertNotEquals(JsonPath.<String>read(primeira.andReturn().getResponse().getContentAsString(), "$[0].id"),
                JsonPath.<String>read(segunda.andReturn().getResponse().getContentAsString(), "$[0].id"));
    }

    @Test void listagemRejeitaParametrosInvalidos() throws Exception {
        enviar(get("/v1/ordem-servico").param("fila", "QUALQUER")).andExpect(status().isBadRequest());
        enviar(get("/v1/ordem-servico").param("fila", "")).andExpect(status().isBadRequest());
        enviar(get("/v1/ordem-servico").param("pagina", "-1")).andExpect(status().isBadRequest());
        enviar(get("/v1/ordem-servico").param("tamanho", "0")).andExpect(status().isBadRequest());
        enviar(get("/v1/ordem-servico").param("tamanho", "101")).andExpect(status().isBadRequest());
        enviar(get("/v1/ordem-servico").param("clienteId", "invalido")).andExpect(status().isBadRequest());
        enviar(get("/v1/ordem-servico").param("veiculoId", "invalido")).andExpect(status().isBadRequest());
    }

    @Test void filaDiagnosticoListaSomenteOsAguardandoInicio() throws Exception {
        var os = abrir();
        enviar(get("/v1/ordem-servico").param("fila", "DIAGNOSTICO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(os.toString()));
        enviar(post("/v1/ordem-servico/" + os + "/diagnostico/inicio")).andExpect(status().isOk());
        enviar(get("/v1/ordem-servico").param("fila", "DIAGNOSTICO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test void filasExecucaoEFinalizacaoAcompanhamOFluxo() throws Exception {
        var os = aguardar();
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        aprovar(os);
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(os.toString()));
        enviar(post("/v1/ordem-servico/" + os + "/execucao/inicio")).andExpect(status().isOk());
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("fila", "FINALIZACAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(post("/v1/ordem-servico/" + os + "/execucao/conclusao")).andExpect(status().isOk());
        enviar(get("/v1/ordem-servico").param("fila", "FINALIZACAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(os.toString()));
        enviar(post("/v1/ordem-servico/" + os + "/finalizacao")).andExpect(status().isOk());
        enviar(get("/v1/ordem-servico").param("fila", "FINALIZACAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(post("/v1/ordem-servico/" + os + "/entrega")).andExpect(status().isOk());
        enviar(get("/v1/ordem-servico").param("fila", "FINALIZACAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test void filaCombinaFiltrosAntesDePaginar() throws Exception {
        var primeiraOs = aguardar();
        aprovar(primeiraOs);
        var veiculo = jdbc.queryForObject("SELECT veiculo_id FROM ordem_servico WHERE id=?", UUID.class, primeiraOs);
        var cliente = jdbc.queryForObject("SELECT cliente_id FROM ordem_servico WHERE id=?", UUID.class, primeiraOs);
        var segundaOs = id(enviar(json(post("/v1/ordem-servico"),
                "{\"veiculoId\":\"" + veiculo + "\",\"descricao\":\"Segunda OS\"}")).andExpect(status().isCreated()));
        aguardar(segundaOs);
        aprovar(segundaOs);
        var naoAprovada = id(enviar(json(post("/v1/ordem-servico"),
                "{\"veiculoId\":\"" + veiculo + "\",\"descricao\":\"Sem aprovacao\"}")).andExpect(status().isCreated()));
        aguardar(naoAprovada);
        jdbc.update("UPDATE ordem_servico SET data_abertura=TIMESTAMP '2099-01-01 00:00:00' WHERE id=?", naoAprovada);

        var primeiraPagina = enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO")
                .param("pagina", "0").param("tamanho", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        var segundaPagina = enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO")
                .param("pagina", "1").param("tamanho", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        var primeiroId = JsonPath.<String>read(primeiraPagina.andReturn().getResponse().getContentAsString(), "$[0].id");
        var segundoId = JsonPath.<String>read(segundaPagina.andReturn().getResponse().getContentAsString(), "$[0].id");
        assertNotEquals(primeiroId, segundoId);
        assertEquals(java.util.Set.of(primeiraOs.toString(), segundaOs.toString()), java.util.Set.of(primeiroId, segundoId));
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO").param("pagina", "2").param("tamanho", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO")
                .param("clienteId", cliente.toString()).param("veiculoId", veiculo.toString())
                .param("status", "AGUARDANDO_APROVACAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO").param("clienteId", UUID.randomUUID().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO").param("status", "RECEBIDA"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        enviar(json(post("/v1/ordem-servico/" + primeiraOs + "/cancelamento"), "{\"motivo\":\"Cancelada\"}"))
                .andExpect(status().isOk());
        enviar(get("/v1/ordem-servico").param("fila", "EXECUCAO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(segundaOs.toString()));
    }

    @Test void consultasDaOsExigemJwtValido() throws Exception {
        var agora = Instant.now();
        var claims = JwtClaimsSet.builder().subject("usuario-teste")
                .issuedAt(agora.minusSeconds(600)).expiresAt(agora.minusSeconds(300)).build();
        var expirado = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        for (var rota : new String[]{"/v1/ordem-servico", "/v1/ordem-servico/" + UUID.randomUUID(),
                "/v1/ordem-servico?fila=EXECUCAO", "/v1/ordem-servico/tempo-medio-finalizacao"}) {
            mvc.perform(get(rota)).andExpect(status().isUnauthorized());
            mvc.perform(get(rota).header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
            mvc.perform(get(rota).header("Authorization", "Bearer " + expirado)).andExpect(status().isUnauthorized());
        }
    }

    @Test void consultasAceitamTokenObtidoNaAutenticacaoAtual() throws Exception {
        var os = abrir();
        mvc.perform(json(post("/v1/auth/registrar"),
                "{\"nome\":\"Atendente\",\"login\":\"atendente-filas\",\"senha\":\"teste123456\"}"))
                .andExpect(status().isCreated());
        var login = mvc.perform(json(post("/v1/auth/autenticar"),
                "{\"login\":\"atendente-filas\",\"senha\":\"teste123456\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var token = JsonPath.<String>read(login, "$.accessToken");
        mvc.perform(get("/v1/ordem-servico").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/v1/ordem-servico/" + os).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(os.toString()));
        mvc.perform(get("/v1/ordem-servico").param("fila", "DIAGNOSTICO").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test void aprovacaoRejeitaCpfInvalidoComMesmaRegraDoCadastro() throws Exception {
        var os = aguardar();
        for (var cpf : new String[]{"52998224724", "11111111111"}) {
            enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"),
                    "{\"cliente\":{\"nome\":\"Ana\",\"cpf\":\"" + cpf + "\"}}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("CPF invalido"));
        }
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
        enviar(get("/v1/ordem-servico/" + os)).andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamentoAprovado").value(false)).andExpect(jsonPath("$.clienteId").isEmpty());
    }

    @Test void aprovacaoRepetidaNaoDuplicaCliente() throws Exception {
        var os = aguardar(); aprovar(os);
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{}" )).andExpect(status().isConflict());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
    }
    @Test void totalAlteradoAposEnvioBloqueiaAprovacao() throws Exception {
        var os = aguardar(); jdbc.update("UPDATE orcamento SET valor=100");
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{}" )).andExpect(status().isConflict());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
    }
    @Test void vinculoIncompativelReverteClienteCriadoEDecisao() throws Exception {
        var os = aguardar(); var cliente = UUID.randomUUID();
        jdbc.update("INSERT INTO pessoa(id,nome) VALUES(?,'Existente')", cliente);
        jdbc.update("INSERT INTO cliente(id,cpf) VALUES(?,'11144477735')", cliente);
        jdbc.update("UPDATE veiculo SET cliente_id=?", cliente);
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{\"cliente\":{\"nome\":\"Ana\",\"cpf\":\"52998224725\"}}"))
                .andExpect(status().isConflict());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
    }
    @Test void operacoesExigemApenasJwtAtual() throws Exception {
        mvc.perform(json(post("/v1/ordem-servico"), "{}")).andExpect(status().isUnauthorized());
        var os = abrir();
        mvc.perform(post("/v1/ordem-servico/" + os + "/diagnostico/inicio").with(jwt())).andExpect(status().isOk());
        enviar(post("/v1/ordem-servico/" + os + "/execucao/inicio")).andExpect(status().isConflict());
    }

    @Test void aprovacaoReaproveitaClientePeloCpfSemAlterarCadastro() throws Exception {
        var cliente = UUID.randomUUID();
        jdbc.update("INSERT INTO pessoa(id,nome) VALUES(?,'Nome original')", cliente);
        jdbc.update("INSERT INTO cliente(id,cpf) VALUES(?,'52998224725')", cliente);
        var os = aguardar(); aprovar(os);
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
        assertEquals("Nome original", jdbc.queryForObject("SELECT nome FROM pessoa WHERE id=?", String.class, cliente));
        enviar(get("/v1/ordem-servico/" + os)).andExpect(jsonPath("$.clienteId").value(cliente.toString()));
    }
    @Test void aprovacaoPermiteVincularClienteExistentePorId() throws Exception {
        var cliente = UUID.randomUUID();
        jdbc.update("INSERT INTO pessoa(id,nome) VALUES(?,'Existente')", cliente);
        jdbc.update("INSERT INTO cliente(id,cpf) VALUES(?,'52998224725')", cliente);
        var os = aguardar();
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{\"clienteId\":\"" + cliente + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clienteId").value(cliente.toString()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
    }
    @Test void aprovacaoSemClienteOuComDuasIdentificacoesEhRejeitada() throws Exception {
        var os = aguardar();
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"), "{}" )).andExpect(status().isBadRequest());
        enviar(json(post("/v1/ordem-servico/" + os + "/aprovacao"),
                "{\"clienteId\":\"" + UUID.randomUUID() + "\",\"cliente\":{\"nome\":\"Ana\",\"cpf\":\"52998224725\"}}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cliente", Integer.class));
    }
}
