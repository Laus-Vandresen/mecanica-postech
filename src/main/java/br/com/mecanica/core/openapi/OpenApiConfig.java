package br.com.mecanica.core.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Mecânica")
                        .description("REST API do projeto da Mecânica")
                        .version("1.0.0"))
                .servers(List.of(new Server().url("/mecanica")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer customizarDocumentacao() {
        return openApi -> {
            ordenarTags(openApi);
            ordenarPaths(openApi);
            ordenarSchemas(openApi);
            definirExemplosUsuarioRequest(openApi);
        };
    }

    private void ordenarTags(OpenAPI openApi) {
        if (openApi.getTags() != null) {
            openApi.getTags().sort(Comparator.comparing(Tag::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    private void ordenarPaths(OpenAPI openApi) {
        if (openApi.getPaths() != null) {
            var pathsOrdenados = new Paths();
            new TreeMap<>(openApi.getPaths()).forEach(pathsOrdenados::addPathItem);
            pathsOrdenados.setExtensions(openApi.getPaths().getExtensions());
            openApi.setPaths(pathsOrdenados);
        }
    }

    private void ordenarSchemas(OpenAPI openApi) {
        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            var schemasOrdenados = new TreeMap<String, Schema>(String.CASE_INSENSITIVE_ORDER);
            schemasOrdenados.putAll(openApi.getComponents().getSchemas());
            openApi.getComponents().setSchemas(schemasOrdenados);
        }
    }

    private void definirExemplosUsuarioRequest(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        var usuarioRequest = openApi.getComponents().getSchemas().get("UsuarioRequestDTOV1");
        if (usuarioRequest == null || usuarioRequest.getProperties() == null) {
            return;
        }
        var nome = usuarioRequest.getProperties().get("nome");
        if (nome instanceof Schema<?> schemaNome) {
            schemaNome.setExample("string");
        }
        var login = usuarioRequest.getProperties().get("login");
        if (login instanceof Schema<?> schemaLogin) {
            schemaLogin.setExample("string");
        }
    }
}
