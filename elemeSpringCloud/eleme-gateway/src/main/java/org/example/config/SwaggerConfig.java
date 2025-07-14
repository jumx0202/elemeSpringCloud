package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Swagger API文档聚合配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.build();
        
        return route(GET("/v3/api-docs/swagger-config"), request -> {
            String configJson = """
                {
                    "urls": [
                        {
                            "name": "用户服务",
                            "url": "http://localhost:8001/v3/api-docs"
                        },
                        {
                            "name": "商家服务", 
                            "url": "http://localhost:8002/v3/api-docs"
                        },
                        {
                            "name": "食物服务",
                            "url": "http://localhost:8003/v3/api-docs"
                        },
                        {
                            "name": "订单服务",
                            "url": "http://localhost:8004/v3/api-docs"
                        },
                        {
                            "name": "支付服务",
                            "url": "http://localhost:8005/v3/api-docs"
                        },
                        {
                            "name": "通知服务",
                            "url": "http://localhost:8006/v3/api-docs"
                        },
                        {
                            "name": "验证码服务",
                            "url": "http://localhost:8007/v3/api-docs"
                        }
                    ]
                }
                """;
            
            return ServerResponse.ok()
                    .header("Content-Type", "application/json")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "*")
                    .bodyValue(configJson);
        }).andRoute(GET("/swagger-ui.html"), request -> {
            String swaggerHtml = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <title>饿了么微服务API文档</title>
                    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.2.0/swagger-ui-bundle.css" />
                    <style>
                        html {
                            box-sizing: border-box;
                            overflow: -moz-scrollbars-vertical;
                            overflow-y: scroll;
                        }
                        *, *:before, *:after {
                            box-sizing: inherit;
                        }
                        body {
                            margin:0;
                            background: #fafafa;
                        }
                    </style>
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://unpkg.com/swagger-ui-dist@5.2.0/swagger-ui-bundle.js"></script>
                    <script src="https://unpkg.com/swagger-ui-dist@5.2.0/swagger-ui-standalone-preset.js"></script>
                    <script>
                        window.onload = function() {
                            const ui = SwaggerUIBundle({
                                urls: [
                                    {
                                        name: "用户服务",
                                        url: "http://localhost:8001/v3/api-docs"
                                    },
                                    {
                                        name: "商家服务", 
                                        url: "http://localhost:8002/v3/api-docs"
                                    },
                                    {
                                        name: "食物服务",
                                        url: "http://localhost:8003/v3/api-docs"
                                    },
                                    {
                                        name: "订单服务",
                                        url: "http://localhost:8004/v3/api-docs"
                                    },
                                    {
                                        name: "支付服务",
                                        url: "http://localhost:8005/v3/api-docs"
                                    },
                                    {
                                        name: "通知服务",
                                        url: "http://localhost:8006/v3/api-docs"
                                    },
                                    {
                                        name: "验证码服务",
                                        url: "http://localhost:8007/v3/api-docs"
                                    }
                                ],
                                dom_id: '#swagger-ui',
                                deepLinking: true,
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIStandalonePreset
                                ],
                                plugins: [
                                    SwaggerUIBundle.plugins.DownloadUrl
                                ],
                                layout: "StandaloneLayout"
                            });
                        };
                    </script>
                </body>
                </html>
                """;
            
            return ServerResponse.ok()
                    .header("Content-Type", "text/html")
                    .header("Access-Control-Allow-Origin", "*")
                    .bodyValue(swaggerHtml);
        });
    }
} 