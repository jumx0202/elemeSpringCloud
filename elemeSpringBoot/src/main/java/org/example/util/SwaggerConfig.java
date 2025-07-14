package org.example.util;


import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
//注意导入的包，有重名
import io.swagger.v3.oas.models.info.License;
//以上为较为特殊的导入包
//配置的注解
@Configuration
public class SwaggerConfig {
    //配置证书
    private License license(){
        return new License().name("MIT").url("https://opensource.org/licenses/MIT");
    }
    //网页上提示的信息
    private Info info(){
        return new Info().title("饿了么系统接口文档").description("APIs for ele.me website.").version("v1.0.1").license(this.license());
    }
    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation().description("Baidu").url("https://baidu.com/");
    }
    //将当前方法的返回值对象放到容器当中
    @Bean
    public OpenAPI createOpenAPI(){
        return new OpenAPI().info(info()).externalDocs(externalDocumentation());
    }
}

