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

    - id(主键)  bigint
    - username 呢称 varchar
    - userAccount 登录账号 varchar
    - userPassword 密码 varchar
    - avatarUrl 头像 varchar
    - gender 性别 tinyint
    - phone 电话 varchar
    - email 邮箱 varchar
    - userStatus 用户状态(是否封号，会员过期等等) int 0-正常
    - userRole 用户角色 0 - 普通用户 1 - 管理员
    - planetCode 用户编号 varchar
    - **createTime 创建时间(数据插入时间)** datetime
    - **updateTime 更新时间(数据更新时间)** datetime
    - **isDelete 是否删除(0 1逻辑删除)** tinyint
    
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


