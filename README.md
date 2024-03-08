# 1 需求分析

- 登录 / 注册
  
- 用户管理（仅管理员可见）对用户的查询或者修改
  
- 用户校验

# 2. 技术选型

- java
  
- spring（依赖注入框架，帮助你管理 Java 对象，集成一些其他的内容）
  
- springmvc（web 框架，提供接口访问、restful 接口等能力）
  
- mybatis（Java 操作数据库的框架，持久层框架，对 jdbc 的封装）
  
- mybatis-plus（对 mybatis 的增强，不用写 sql 也能实现增删改查）
  
- springboot（快速启动 / 快速集成项目。不用自己管理 spring 配置，不用自己整合各种框架）
  
- junit 单元测试库
  
- mysql 数据库

# 3. 数据库表设计

- **用户表**：

```sql
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment '状态 0 - 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员 ',
    planetCode   varchar(512)                       null comment '用户编号'
)
    comment '用户';
```
    
# 4. 登录注册功能

## 4.1 注册逻辑

- 用户在前端输入账户和密码、以及用户编号
  
- **校验**用户的账户、密码、校验密码和用户编号，是否符合要求
  
    - 非空
    - 账户长度 不小于 4 位
    - 密码 不小于 8 位
    - 用户编号 不大于 5 位
    - 账户不能重复
    - 账户不包含特殊字符
    - 密码和校验密码相同
  
- 对密码进行加密
  
- 向数据库插入用户数据

## 4.2 登录逻辑

- 校验用户账户和密码是否合法 (如果这些都不对，就不用去数据库查询，节省资源)

    - 非空
    - 账户长度 不小于 4 位
    - 密码就 不小于 8 位
    - 账户不包含特殊字符

- 校验密码是否输入正确，加密后和数据库中的密文密码去对比

- 用户信息脱敏，隐藏敏感信息，防止数据库中的字段泄露

- 记录用户的登录态(session)，将其存到服务器上(用 SpringBoot 框架封装的服务器 tomcat 去记录)

- 返回脱敏后的用户信息（设置允许返回的字段）

# 5. 用户管理接口

## 5.1 查询和删除功能

- 查询: 根据用户名查询

- 删除: 根据用户id查询

## 5.2 添加权限字段

- user 表新增 userRole 列
  - 再用 mybatis-plus 重新生成下

## 5.3 校验代码

### 5.3.1 从 session 中获取用户的登录态

- 使用 getAttribute 获取用户登录态键

```java
Object userObj = request.getSession().getAttribute(UserService.USER_LOGIN_STATE);
User user = (User) userObj;
```

#### 5.3.2 统一定义常量

- constant 包下**新建 UserConstant 接口**
  - 接口里的属性默认是 `public static`

  - 定义 USER_LOGIN_STATE 和 用户角色类型

#### 5.3.3 设置 session 超时时间

- 在 application.yml 增加 **session 失效时间(1天)**

```yaml
spring:
  # session 失效时间
  session:
    timeout: 86400
```

# 6. 用户注销接口

- 移除登录态即可

```java
request.getSession().removeAttribute(USER_LOGIN_STATE);
```

# 7. 后端代码优化

## 7.1 通用返回对象

创建 common 包，包下: 

- 新建通用的响应封装类 `BaseResponse<T>` ，用于构造API的返回对象，提供统一的响应格式

- 新建工具类 `ResultUtils`，提供了一系列静态方法来快速创建 `BaseResponse` 对象
  - 分别用于构造成功或失败的响应，简化在业务逻辑中生成通用响应对象的过程

- 新建 `ErrorCode` 枚举类，自定义状态码及其对应的消息和描述

```java
SUCCESS(0, "ok", ""),
PARAMS_ERROR(40000, "请求参数错误", ""),
PARAMS_NULL_ERROR(40001, "请求数据为空", ""),
NO_LOGIN(40100, "未登录", ""),
NO_AUTH(40101, "无权限", ""),
USER_SAVE_ERROR(40200, "用户保存错误", ""),
SYSTEM_ERROR(50000, "系统内部异常", "");
```

> `BaseResponse`提供了通用的响应结构
> 
> `ResultUtils`简化了响应对象的创建过程
> 
> `ErrorCode`则为错误情况提供了标准化的表示方法

## 7.2 封装全局异常处理

创建 exception 包, 包下:
  
- **定义业务异常类**: `BusinessException` 继承 `RuntimeException`

  - 相对于 java 的异常类，支持更多字段
    
  - 自定义构造函数，更灵活/快捷的设置字段
  
```java
public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
    this.description = errorCode.getDescription();
}
```
  
- **编写全局异常处理器**：使用 Spring AOP 在调用方法前后进行额外的处理
  
  - 捕获代码中所有的异常，内部消化，集中处理，让前端得到更详细的业务报错/信息
    
  - 同时屏蔽掉项目框架本身的异常（不暴露服务器内部状态）
  
```java
@ExceptionHandler(BusinessException.class) // 针对自定义异常
public BaseResponse businessExceptionHandler(BusinessException e) {
    log.error("businessException" + e.getMessage(), e);
    return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
}
```
  
- **全局请求日志和登录校验**(todo)

# 8. 部署和上线

## 8.1 多环境配置

- **新增生成环境配置文件**：
  
  - 通过 `application.yml` 添加不同的后缀来区分配置文件，复制 `application.yml`，粘贴到resources 包下，重命名为 `application-prod.yml`

- **修改生产环境的配置**： 如数据库等

## 8.2 项目部署

- 部署方式:
  - 原始部署
  
  - 宝塔 
  
  - 容器
  
  - 容器平台
  
> 这里选择容器部署

- dockerfile 编写

```dockerfile
FROM maven:3.5-jdk-8-alpine as builder
  
# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/user_center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]
```

- 用 docker 命令根据 Dockerfile 文件构建镜像：

```shell
docker build -t user-center-frontend:v0.0.1 .
```

- 启动

```shell
docker run -p 8080:8080 -d user-center-backend:v0.0.1
```

