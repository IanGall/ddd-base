package cn.iantech.test.flow;

import cn.iantech.test.nplusone.DetectNPlusOne;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证网关业务流程可以通过强类型 Feign 串联多个 HTTP 接口。
 */
class FeignBusinessFlowTest {
    private HttpServer server;
    private ExecutorService executor;

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        executor = Executors.newSingleThreadExecutor();
        server.setExecutor(executor);
        server.createContext("/login", exchange -> respond(exchange, "{\"token\":\"token-1\"}"));
        server.createContext("/orders/100", exchange -> respond(exchange, "{\"id\":\"100\",\"status\":\"CREATED\"}"));
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
        if (executor != null) executor.shutdownNow();
    }

    @Test
    @DetectNPlusOne(maxRemoteCalls = 2, maxRepeatedRemoteCalls = 1)
    void shouldCallMultipleTypedApis() {
        TestApi api = FeignTestClientFactory.create(TestApi.class, URI.create("http://127.0.0.1:" + server.getAddress().getPort()));
        FlowKey<LoginResponse> login = new FlowKey<>("login", LoginResponse.class);
        FlowKey<OrderResponse> order = new FlowKey<>("order", OrderResponse.class);
        BusinessFlow flow = BusinessFlow.builder("订单自动化流程")
                .then("登录", context -> context.put(login, api.login(new LoginRequest("tester", "secret"))))
                .then("查询订单", context -> context.put(order, api.order("100", context.require(login).token())))
                .then("验证订单", context -> assertThat(context.require(order).status()).isEqualTo("CREATED"))
                .build();
        FlowContext context = new BusinessFlowRunner().run(flow);
        assertThat(context.require(order).id()).isEqualTo("100");
    }

    interface TestApi {
        @RequestLine("POST /login")
        @Headers("Content-Type: application/json")
        LoginResponse login(LoginRequest request);

        @RequestLine("GET /orders/{id}")
        @Headers("Authorization: Bearer {token}")
        OrderResponse order(@Param("id") String id, @Param("token") String token);
    }

    record LoginRequest(String username, String password) {
    }

    record LoginResponse(String token) {
    }

    record OrderResponse(String id, String status) {
    }
}
