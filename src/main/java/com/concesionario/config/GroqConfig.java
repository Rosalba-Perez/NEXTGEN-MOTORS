package com.concesionario.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Configuración para compatibilidad con Groq.

 */
@Configuration
public class GroqConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            restClientBuilder.requestInterceptor(groqInterceptor());
        };
    }

    private ClientHttpRequestInterceptor groqInterceptor() {
        return (request, body, execution) -> {
            String host = request.getURI().getHost();
            
            // Solo interceptar si el destino es Groq
            if (host != null && host.contains("groq.com")) {
                String bodyString = new String(body, StandardCharsets.UTF_8);
                
                // Eliminar "extra_body":{...} o "extra_body":null
                // El regex busca la propiedad y cualquier coma que la preceda o siga para mantener JSON válido
                String cleanedBody = bodyString
                    .replaceAll(",\\s*\"extra_body\"\\s*:\\s*\\{[^}]*\\}", "")
                    .replaceAll("\"extra_body\"\\s*:\\s*\\{[^}]*\\},?", "")
                    .replaceAll(",\\s*\"extra_body\"\\s*:\\s*null", "")
                    .replaceAll("\"extra_body\"\\s*:\\s*null,?", "");

                return execution.execute(request, cleanedBody.getBytes(StandardCharsets.UTF_8));
            }
            
            return execution.execute(request, body);
        };
    }
}
