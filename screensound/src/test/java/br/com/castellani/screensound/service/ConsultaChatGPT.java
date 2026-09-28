package br.com.castellani.screensound.service;


import com.jayway.jsonpath.internal.filter.ValueNodes;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConsultaChatGPT {

    public static String obterInformacao(String texto){
        String apiKey = System.getenv("OPENAI_APIKEY");
        // Montamos o JSON da requisição na mão usando Text Blocks do Java
        // Escapamos as aspas do texto do usuário para não quebrar o JSON
        String jsonBody = """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [
                        {"role": "system", "content": "Você é um assistente útil e responde de forma resumida."},
                        {"role": "user", "content": "Traduza ou traga informações sobre: %s"}
                    ]
                }
                """.formatted(texto.replace("\"", "\\\""));

        try {
            // Cria o cliente HTTP nativo do Java
            HttpClient client = HttpClient.newHttpClient();

            // Monta a requisição para a URL oficial da OpenAI
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey) // Passa o Token
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Dispara a requisição e guarda a resposta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Usamos o Jackson padrão (que já vem no Spring) para ler o JSON e extrair só o texto
            ObjectMapper mapper = new ObjectMapper();
            ValueNodes.JsonNode rootNode = mapper.readTree(response.body());

            // Navega pelo JSON de resposta da OpenAI: choices[0] -> message -> content
            return rootNode.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            System.out.println("⚠️ Falha ao consultar o ChatGPT: " + e.getMessage());
            return "Informação original: " + texto;
        }
    }
}