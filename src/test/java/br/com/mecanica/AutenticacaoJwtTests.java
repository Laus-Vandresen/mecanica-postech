package br.com.mecanica;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

@SpringBootTest
@ActiveProfiles("test")
class AutenticacaoJwtTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(context).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    void swaggerListaAutenticacaoPrimeiro() throws Exception {
        mvc().perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0].name").value("autenticacaoV1"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/v1/pecas'].get.security[0].bearerAuth").exists());
    }

    @Test
    void tokenInvalidoRetornaErrorResponseDto() throws Exception {
        mvc().perform(get("/v1/pecas").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.type").value("/erro-de-autenticacao"))
                .andExpect(jsonPath("$.title").value("Não autorizado"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.userMessage").value("Credenciais ausentes ou inválidas."));
    }

    @Test
    void fluxoCompletoDeAutenticacao() throws Exception {
        var mvc = mvc();
        mvc.perform(get("/v1/pecas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.type").value("/erro-de-autenticacao"))
                .andExpect(jsonPath("$.title").value("Não autorizado"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.userMessage").exists());

        var cadastro = "{\"nome\":\"Ana\",\"login\":\"ana\",\"senha\":\"segredo123\"}";
        mvc.perform(post("/v1/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(cadastro))
                .andExpect(status().isCreated());
        mvc.perform(post("/v1/auth/registrar").contentType(MediaType.APPLICATION_JSON).content(cadastro))
                .andExpect(status().isConflict());

        mvc.perform(post("/v1/auth/autenticar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"ana\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.type").value("/erro-de-autenticacao"))
                .andExpect(jsonPath("$.userMessage").value("Login ou senha invalidos"));

        var resposta = mvc.perform(post("/v1/auth/autenticar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"ana\",\"senha\":\"segredo123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        var token = new ObjectMapper().readTree(resposta).get("accessToken").asText();

        mvc.perform(get("/v1/pecas").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/v1/pecas").header("Authorization", "Bearer invalido")).andExpect(status().isUnauthorized());
    }
}
