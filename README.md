hjy-community 合家云社区物业管理平台 - 项目简历描述
项目名称
合家云社区物业管理平台 (hjy-community)

项目时间
2025年3月 - 至今

项目描述
基于Spring Boot开发的模块化社区物业管理平台，采用前后端分离架构，为物业公司提供小区管理、用户权限管理、数据字典等核心功能。项目采用分层架构设计，实现了高内聚、低耦合的模块化开发模式。

技术栈
后端框架: Spring Boot 2.7.8、Spring Security
持久层: MyBatis-Plus 3.4.1、PageHelper
数据库: MySQL 8.0
缓存: Redis (Jedis)
安全认证: JWT Token、BCrypt密码加密
工具类: EasyPOI (Excel导入导出)、Orika (对象拷贝)、FastJSON
其他: Druid连接池、Lombok、Maven
项目职责
1. 架构设计与模块化开发
设计并实现模块化架构，将系统划分为 common（公共基础）、community（小区管理）、system（系统管理）、framework（框架配置）、web（控制层）五大模块
封装 BaseController 基类，统一处理分页查询、响应封装等通用功能，减少代码重复
设计 BaseEntity 基类，实现创建时间、更新时间等通用字段的自动填充
实现统一的响应格式 BaseResponse，规范API返回结构
2. 核心功能开发
小区管理模块: 实现小区的增删改查、多条件分页查询、下拉列表等功能
系统管理模块: 开发用户管理、角色管理、菜单管理、部门管理、字典管理等核心功能
权限控制: 基于Spring Security实现JWT认证机制，使用 @PreAuthorize 注解实现方法级权限控制
数据字典: 实现系统字典类型和字典数据的维护功能
3. 安全认证与权限管理
集成Spring Security，实现基于JWT的无状态认证
开发JWT过滤器 JwtAuthenticationTokenFilter，实现Token验证和用户信息注入
配置CORS跨域支持，解决前后端分离架构下的跨域问题
实现密码BCrypt加密存储，提升系统安全性
4. 数据访问层优化
使用MyBatis-Plus简化CRUD操作，提高开发效率
集成PageHelper实现分页功能，支持多条件动态查询
使用MyBatis注解和XML混合方式，处理复杂SQL查询（如多表关联查询）
配置MyBatis-Plus自动填充功能，实现创建时间、更新时间的自动维护
5. 工具类与公共组件
封装Redis工具类 RedisCache，实现缓存统一管理
开发Excel工具类 ExcelUtils，支持数据导入导出
使用Orika实现DTO与VO之间的对象拷贝，提升代码可维护性
实现全局异常处理器 GlobalExceptionHandler，统一异常处理
6. 性能优化
配置Druid数据库连接池，优化数据库连接管理
集成Redis缓存，提升系统响应速度
配置Tomcat线程池参数，优化并发处理能力
项目亮点
模块化设计: 采用分层架构和模块化设计，代码结构清晰，便于维护和扩展
统一规范: 实现了统一的响应格式、异常处理、分页封装等，提升代码规范性
安全机制: 完整的JWT认证体系和权限控制，保障系统安全
开发效率: 通过基类封装和工具类复用，显著提升开发效率
可扩展性: 良好的模块划分和接口设计，便于后续功能扩展
技术难点与解决方案
JWT无状态认证: 设计JWT过滤器实现Token验证，解决前后端分离架构下的认证问题
动态SQL查询: 使用MyBatis动态SQL实现多条件组合查询，提升查询灵活性
权限细粒度控制: 通过Spring Security的 @PreAuthorize 注解实现方法级权限控制
对象转换优化: 使用Orika实现高性能的对象属性拷贝，替代传统的手动赋值方式
项目成果
完成小区管理、系统管理等核心业务模块的开发
实现完整的用户认证和权限管理体系
建立统一的开发规范和代码架构
系统运行稳定，支持高并发访问
