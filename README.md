# 智能简历分析系统 - 后端

基于 SpringBoot 3.2 + MyBatis-Plus + MySQL 的智能简历分析系统后端服务

## 技术栈

- **Java**: 17
- **SpringBoot**: 3.2.0
- **MyBatis-Plus**: 3.5.5
- **MySQL**: 5.0+
- **Spring Security**: JWT认证
- **Swagger**: SpringDoc OpenAPI 3
- **工具库**: Hutool, Lombok

## 项目结构

```
IntelligentResume_back/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── app/intelligent/resume/
│   │   │       ├── IntelligentResumeApplication.java    # 启动类
│   │   │       ├── common/                              # 公共模块
│   │   │       │   ├── constant/                        # 常量
│   │   │       │   ├── exception/                       # 异常处理
│   │   │       │   └── result/                          # 统一返回结果
│   │   │       ├── config/                              # 配置类
│   │   │       │   ├── CorsConfig.java                  # 跨域配置
│   │   │       │   ├── JacksonConfig.java               # JSON配置
│   │   │       │   ├── MybatisPlusConfig.java           # MyBatis-Plus配置
│   │   │       │   ├── SecurityConfig.java              # Security配置
│   │   │       │   └── SwaggerConfig.java               # Swagger配置
│   │   │       ├── controller/                          # 控制器
│   │   │       │   ├── AuthController.java              # 认证接口
│   │   │       │   ├── UserController.java              # 用户管理
│   │   │       │   ├── ResumeController.java            # 简历管理
│   │   │       │   ├── JobController.java               # 岗位管理
│   │   │       │   ├── MatchRecordController.java       # 匹配记录
│   │   │       │   └── AnalysisReportController.java    # 分析报告
│   │   │       ├── dto/                                 # 数据传输对象
│   │   │       │   ├── request/                         # 请求DTO
│   │   │       │   └── response/                        # 响应DTO
│   │   │       ├── entity/                              # 实体类
│   │   │       │   ├── User.java                        # 用户
│   │   │       │   ├── Resume.java                      # 简历
│   │   │       │   ├── ResumeDetail.java                # 简历详情
│   │   │       │   ├── Job.java                         # 岗位
│   │   │       │   ├── MatchRecord.java                 # 匹配记录
│   │   │       │   └── AnalysisReport.java              # 分析报告
│   │   │       ├── repository/                          # 数据访问层
│   │   │       ├── security/                            # 安全模块
│   │   │       │   ├── JwtAuthenticationFilter.java     # JWT过滤器
│   │   │       │   ├── JwtTokenProvider.java            # JWT工具
│   │   │       │   ├── SecurityUtils.java               # 安全工具
│   │   │       │   └── UserDetailsServiceImpl.java      # 用户详情服务
│   │   │       └── service/                             # 业务逻辑层
│   │   │           └── impl/                            # 实现类
│   │   └── resources/
│   │       ├── application.yml                          # 主配置
│   │       ├── application-dev.yml                      # 开发环境
│   │       ├── application-prod.yml                     # 生产环境
│   │       └── logback-spring.xml                       # 日志配置
│   └── test/
└── pom.xml
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.0+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE intelligent_resume DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据库脚本：
```bash
mysql -u root -p intelligent_resume < ../database/intelligent_resume.sql
```

3. 修改配置文件 `src/main/resources/application-dev.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/intelligent_resume?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: your_password
```

### 3. 运行项目

```bash
# 使用Maven运行
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/intelligent-resume-1.0.0.jar
```

### 4. 访问接口文档

启动成功后访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

## 核心功能

### 1. 认证授权

- **JWT Token认证**：基于JWT的无状态认证
- **角色权限控制**：支持管理员、HR、求职者三种角色
- **接口权限**：使用 `@PreAuthorize` 注解控制接口访问权限

### 2. 用户管理

- 用户注册、登录、登出
- 用户信息CRUD
- 用户角色管理
- 密码加密（BCrypt）

### 3. 简历管理

- 简历上传、编辑、删除
- 简历列表查询、分页查询
- 简历详情管理
- 简历浏览统计

### 4. 岗位管理

- 岗位发布、编辑、删除
- 岗位列表查询、分页查询
- 招聘中岗位筛选
- 岗位浏览统计

### 5. 智能匹配

- 简历与岗位匹配
- 匹配度计算
- 匹配记录查询
- 技能匹配分析

### 6. 分析报告

- 简历智能分析
- 评分系统
- 优化建议生成
- 分析报告查询

## API接口说明

### 认证接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/auth/login` | POST | 用户登录 | 公开 |
| `/api/auth/register` | POST | 用户注册 | 公开 |
| `/api/auth/logout` | POST | 用户登出 | 需认证 |
| `/api/auth/refresh` | POST | 刷新Token | 公开 |

### 用户管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/users` | POST | 创建用户 | ADMIN |
| `/api/users` | GET | 查询用户列表 | ADMIN |
| `/api/users/{id}` | GET | 查询用户详情 | 认证用户 |
| `/api/users/{id}` | PUT | 更新用户 | 认证用户 |
| `/api/users/{id}` | DELETE | 删除用户 | ADMIN |
| `/api/users/page` | GET | 分页查询 | ADMIN |

### 简历管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/resumes` | POST | 创建简历 | SEEKER/ADMIN |
| `/api/resumes` | GET | 查询简历列表 | ADMIN/HR |
| `/api/resumes/{id}` | GET | 查询简历详情 | 认证用户 |
| `/api/resumes/{id}` | PUT | 更新简历 | SEEKER/ADMIN |
| `/api/resumes/{id}` | DELETE | 删除简历 | SEEKER/ADMIN |
| `/api/resumes/user/{userId}` | GET | 查询用户简历 | 认证用户 |
| `/api/resumes/page` | GET | 分页查询 | ADMIN/HR |

### 岗位管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/jobs` | POST | 创建岗位 | HR/ADMIN |
| `/api/jobs` | GET | 查询岗位列表 | 认证用户 |
| `/api/jobs/{id}` | GET | 查询岗位详情 | 认证用户 |
| `/api/jobs/{id}` | PUT | 更新岗位 | HR/ADMIN |
| `/api/jobs/{id}` | DELETE | 删除岗位 | HR/ADMIN |
| `/api/jobs/recruiting` | GET | 查询招聘中岗位 | 认证用户 |
| `/api/jobs/page` | GET | 分页查询 | 认证用户 |

### 匹配记录

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/matches` | POST | 创建匹配记录 | 认证用户 |
| `/api/matches/resume/{resumeId}` | GET | 查询简历匹配 | 认证用户 |
| `/api/matches/job/{jobId}` | GET | 查询岗位匹配 | ADMIN/HR |

### 分析报告

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/reports` | POST | 创建分析报告 | ADMIN/SEEKER |
| `/api/reports/resume/{resumeId}` | GET | 查询简历报告 | 认证用户 |
| `/api/reports/resume/{resumeId}/latest` | GET | 查询最新报告 | 认证用户 |

## 统一返回格式

所有接口返回格式统一为：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [],
  "timestamp": 1702345678901
}
```

**注意**：即使是单个对象，`data` 字段也返回数组形式。

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

## 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key-must-be-long-enough
  expiration: 86400000      # 24小时
  refresh-expiration: 604800000  # 7天
```

### MyBatis-Plus配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 驼峰命名转换
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL日志
  global-config:
    db-config:
      logic-delete-field: deleted  # 逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto  # 主键自增
```

## 默认账号

系统初始化后会创建默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

## 开发指南

### 添加新接口

1. 在 `entity` 包创建实体类
2. 在 `repository` 包创建Repository接口
3. 在 `service` 包创建Service接口和实现类
4. 在 `controller` 包创建Controller
5. 使用 `@PreAuthorize` 注解控制权限

### 异常处理

使用 `BusinessException` 抛出业务异常：

```java
throw new BusinessException(ResultCode.USER_NOT_EXIST);
throw new BusinessException("自定义错误消息");
```

### 日志记录

使用 Lombok 的 `@Slf4j` 注解：

```java
@Slf4j
@Service
public class UserServiceImpl {
    public void someMethod() {
        log.info("日志信息");
        log.error("错误信息", exception);
    }
}
```

## 部署说明

### 生产环境配置

1. 修改 `application-prod.yml`
2. 设置环境变量：`export SPRING_PROFILES_ACTIVE=prod`
3. 打包：`mvn clean package -Pprod`
4. 运行：`java -jar target/intelligent-resume-1.0.0.jar`

### Docker部署（可选）

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/intelligent-resume-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 常见问题

### 1. 启动失败

- 检查JDK版本是否为17+
- 检查MySQL是否启动
- 检查数据库连接配置是否正确

### 2. JWT Token无效

- 检查Token是否过期
- 检查请求头格式：`Authorization: Bearer <token>`

### 3. 跨域问题

已配置CORS，如仍有问题请检查 `CorsConfig.java`

## 技术支持

- 项目地址：https://github.com/your-repo/intelligent-resume
- 问题反馈：提交 Issue
- 邮箱：support@intelligent-resume.com

## 许可证

Apache License 2.0

---

**开发团队**: Intelligent Resume Team
**最后更新**: 2024-12-09
