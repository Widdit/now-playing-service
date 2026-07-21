package com.widdit.nowplaying.service.kugou;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KuGouHttpClientTest {
    @Test
    void preservesLegacyGetRequestMetadata() throws IOException {
        AtomicReference<String> requestMethod = new AtomicReference<>();
        AtomicReference<Headers> requestHeaders = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/search", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            requestHeaders.set(exchange.getRequestHeaders());
            byte[] body = "response".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();

        try {
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/search";

            String response = new KuGouHttpClient().get(url);

            Headers headers = requestHeaders.get();
            assertAll(
                    () -> assertEquals("response", response),
                    () -> assertEquals("GET", requestMethod.get()),
                    () -> assertEquals(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:57.0) "
                                    + "Gecko/20100101 Firefox/57.0",
                            headers.getFirst("User-Agent")),
                    () -> assertEquals("1", headers.getFirst("DNT")),
                    () -> assertEquals(
                            "application/x-www-form-urlencoded",
                            headers.getFirst("Content-Type"))
            );
        } finally {
            server.stop(0);
        }
    }
}
