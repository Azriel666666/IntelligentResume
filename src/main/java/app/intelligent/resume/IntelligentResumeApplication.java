package app.intelligent.resume;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能简历分析系统启动类
 *
 * @author Intelligent Resume Team
 * @since 2024
 */
@SpringBootApplication
@MapperScan("app.intelligent.resume.repository")
public class IntelligentResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentResumeApplication.class, args);
        System.out.println("""

                ====================================
                智能简历分析系统启动成功！
                Swagger文档地址: http://localhost:8080/swagger-ui.html
                ====================================
                """);
    }

}
