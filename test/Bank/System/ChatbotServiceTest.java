package Bank.System;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ChatbotServiceTest {
    private HttpServer server;
    private ChatbotService service;
    private volatile JsonObject request;
    private volatile int status = 200;
    private volatile String response = "{\"message\":{\"content\":\"Choose 5. Balance and Statements.\"}}";

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            request = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8)).getAsJsonObject();
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        service = new ChatbotService(URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                + "/api/chat"), HttpClient.newHttpClient(), Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void sendsEscapedQuestionsAndDecodesUnicodeReplies() throws Exception {
        response = "{\"message\":{\"content\":\"Say \\\"hello\\\"\\nمرحبا\"}}";
        assertEquals("Say \"hello\"\nمرحبا", service.ask("What is \"balance\"?\nمرحبا"));
        assertEquals("qwen3:8b", request.get("model").getAsString());
        assertFalse(request.get("think").getAsBoolean());
        assertFalse(request.get("stream").getAsBoolean());
        assertEquals("What is \"balance\"?\nمرحبا",
                request.getAsJsonArray("messages").get(1).getAsJsonObject().get("content").getAsString());
    }

    @Test
    void retainsFollowupsAndClearsHistory() throws Exception {
        service.ask("How do I check my balance?");
        service.ask("And my statement?");
        assertEquals(4, request.getAsJsonArray("messages").size());
        assertEquals("assistant", request.getAsJsonArray("messages").get(2)
                .getAsJsonObject().get("role").getAsString());
        service.clearHistory();
        service.ask("How do I deposit?");
        assertEquals(2, request.getAsJsonArray("messages").size());
    }

    @Test
    void boundsHistoryWhileKeepingTheGuide() throws Exception {
        for (int i = 0; i < 12; i++) service.ask("Question " + i);
        assertTrue(request.getAsJsonArray("messages").size() <= 10);
        String guide = request.getAsJsonArray("messages").get(0).getAsJsonObject()
                .get("content").getAsString();
        assertTrue(guide.contains("controller is NOT implemented"));
    }

    @Test
    void missingModelAndServerErrorsDoNotPolluteHistory() throws Exception {
        status = 404;
        assertTrue(assertThrows(IOException.class, () -> service.ask("Help"))
                .getMessage().contains("ollama pull qwen3:8b"));
        status = 500;
        assertTrue(assertThrows(IOException.class, () -> service.ask("Help"))
                .getMessage().contains("HTTP 500"));
        status = 200;
        service.ask("Help");
        assertEquals(2, request.getAsJsonArray("messages").size());
    }

    @Test
    void rejectsMalformedAndEmptyResponses() {
        for (String invalid : new String[]{"not json", "{}", "{\"message\":{\"content\":\"\"}}"}) {
            response = invalid;
            assertTrue(assertThrows(IOException.class, () -> service.ask("Help"))
                    .getMessage().contains("unreadable or empty"));
        }
    }

    @Test
    void handlesUnavailableOllamaAndInvalidInput() {
        server.stop(0);
        assertTrue(assertThrows(IOException.class, () -> service.ask("Help"))
                .getMessage().contains("Cannot reach Ollama"));
        assertThrows(IllegalArgumentException.class, () -> service.ask(" "));
        assertThrows(IllegalArgumentException.class, () -> service.ask("a".repeat(1001)));
    }
}
