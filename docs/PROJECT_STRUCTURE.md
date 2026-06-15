# 校园智能服务小助手 - A组长项目搭建文档

## 一、项目概述

### 1.1 项目名称
校园智能服务小助手（Campus AI Assistant）

### 1.2 技术栈
- **后端框架**: Spring Boot 3.2.5
- **AI引擎**: SpringAI Alibaba（对接阿里云DashScope/通义千问大模型）
- **数据库**: MySQL 8.0+（由成员B负责完善）
- **ORM框架**: MyBatis Plus 3.5.7
- **连接池**: Druid 1.2.23
- **API文档**: Knife4j 4.5.0（OpenAPI 3.0）
- **工具库**: Hutool 5.8.29、Lombok 1.18.32
- **Java版本**: JDK 17

### 1.3 A组长职责范围
根据项目分工文档，A组长负责以下核心模块：
1. **AI智能问答引擎** - 集成SpringAI Alibaba，对接大模型
2. **网络数据通信** - RESTful API接口 + SSE流式响应
3. **系统全局配置与总体架构** - MVC/DAO模式包结构规范
4. **RAG检索增强生成** - 校园文档知识库精准检索（亮点功能）
5. **多线程并发控制** - 线程池管理，防止前端卡顿（亮点功能）

---

## 二、项目目录结构

```
campus-ai-assistant/
├── pom.xml                          # Maven配置文件（依赖管理）
├── src/
│   ├── main/
│   │   ├── java/com/campus/ai/
│   │   │   ├── CampusAiAssistantApplication.java   # 应用启动类
│   │   │   │
│   │   │   ├── config/             # 配置层（A组长负责）
│   │   │   │   ├── GlobalExceptionHandler.java    # 全局异常处理器
│   │   │   │   ├── BusinessException.java         # 业务异常类
│   │   │   │   ├── AiServiceException.java         # AI服务异常类
│   │   │   │   ├── CorsConfig.java                 # 跨域配置
│   │   │   │   └── ThreadPoolConfig.java           # 多线程并发配置
│   │   │   │
│   │   │   ├── controller/          # 控制器层（A组长负责）
│   │   │   │   ├── ChatController.java            # AI问答接口
│   │   │   │   └── RagController.java             # RAG知识库管理接口
│   │   │   │
│   │   │   ├── service/            # 服务接口层
│   │   │   │   └── ChatService.java               # AI问答服务接口
│   │   │   │
│   │   │   ├── service/impl/       # 服务实现层（A组长负责）
│   │   │   │   └── ChatServiceImpl.java            # AI问答服务实现
│   │   │   │
│   │   │   ├── dao/                # 数据访问层（成员B负责扩展）
│   │   │   │
│   │   │   ├── entity/             # 实体类（成员B负责创建）
│   │   │   │
│   │   │   ├── dto/                # 数据传输对象（A组长负责）
│   │   │   │   ├── Result.java                    # 统一响应结果
│   │   │   │   ├── ChatRequest.java              # 聊天请求DTO
│   │   │   │   └── ChatResponse.java             # 聊天响应DTO
│   │   │   │
│   │   │   ├── rag/                # RAG模块（A组长核心亮点）
│   │   │   │   ├── RagService.java                 # RAG服务接口
│   │   │   │   └── RagServiceImpl.java             # RAG服务实现
│   │   │   │
│   │   │   ├── concurrent/         # 并发控制模块（A组长核心亮点）
│   │   │   │   ├── AsyncTaskExecutor.java          # 异步任务执行器
│   │   │   │   └── RateLimiter.java                # 请求限流器
│   │   │   │
│   │   │   └── util/               # 工具类（待扩展）
│   │   │
│   │   └── resources/
│   │       ├── application.yml     # 主配置文件
│   │       ├── static/             # 静态资源
│   │       └── templates/          # 模板文件
│   │
│   └── test/                       # 测试代码
│       └── java/com/campus/ai/
│
└── docs/                           # 文档目录
```

---

## 三、核心模块说明

### 3.1 AI智能问答引擎（ChatService）

**位置**: `service/impl/ChatServiceImpl.java`

**核心功能**:
- 对接阿里云通义千问大模型（qwen-max/qwen-plus等）
- 支持同步和流式（SSE）两种响应模式
- 支持多轮对话（会话管理）
- 集成RAG检索增强，提供更精准的校园相关回答
- 使用线程池异步处理AI请求，提升性能

**API接口**:
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/chat/ask` | 同步智能问答 |
| POST | `/api/chat/stream` | 流式智能问答（SSE） |
| GET | `/api/chat/simple` | 简单快速问答 |
| DELETE | `/api/chat/session/{sessionId}` | 清理会话历史 |
| GET | `/api/chat/session/{sessionId}/history` | 获取会话历史 |

### 3.2 RAG检索增强生成模块（RagService）

**位置**: `rag/RagServiceImpl.java`

**核心功能**:
- 校园文档知识库管理（加载、分块、向量化）
- 基于余弦相似度的语义检索
- 自动从知识库目录加载文档（支持txt/md/pdf/doc/docx）
- 检索上下文自动注入到AI提示词中

**API接口**:
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/rag/document/upload` | 上传文档到知识库 |
| POST | `/api/rag/documents/batch-load` | 批量加载文档 |
| GET | `/api/rag/stats` | 知识库统计信息 |
| DELETE | `/api/rag/clear` | 清空知识库 |
| GET | `/api/rag/test-retrieval` | 测试检索功能 |

### 3.3 多线程并发控制模块

**位置**: `concurrent/` 目录

**核心组件**:
1. **ThreadPoolConfig**: 配置两个专用线程池
   - AI请求处理线程池（core=5, max=20, queue=100）
   - 流式响应线程池（core=10, max=50, queue=200）

2. **AsyncTaskExecutor**: 统一的异步任务执行接口
   - executeAiTask(): 执行AI相关任务
   - executeStreamingTask(): 执行流式响应任务
   - executeWithTimeout(): 带超时的任务执行

3. **RateLimiter**: 基于滑动窗口的请求限流器
   - 分钟级限流：默认30次/分钟
   - 小时限流：默认300次/小时
   - 防止API被滥用

---

## 四、配置文件说明

### 4.1 application.yml 核心配置项

```yaml
# 服务端口与路径
server:
  port: 8080
  servlet:
    context-path: /api

# 数据源配置（需成员B完善数据库表设计后更新）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_assistant
    username: root
    password: root

# SpringAI Alibaba配置（必须配置！）
spring-ai:
  dashscope:
    api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}  # 阿里云API密钥
    chat:
      options:
        model: qwen-max        # 模型选择
        temperature: 0.7       # 温度参数
        max-tokens: 2000       # 最大token数

# RAG配置
rag:
  enabled: true
  knowledge-base:
    path: ./data/knowledge-base  # 知识库文档存放路径
  retrieval:
    top-k: 5                     # 返回最相关的K个结果
    similarity-threshold: 0.7    # 相似度阈值

# 多线程配置
concurrent:
  ai-thread-pool:
    core-pool-size: 5
    maximum-pool-size: 20
  streaming-thread-pool:
    core-pool-size: 10
    maximum-pool-size: 50

# 请求限流配置
app:
  rate-limit:
    enabled: true
    requests-per-minute: 30
    requests-per-hour: 300
```

---

## 五、Git版本管理

已初始化Git仓库并完成初始提交。

### Git工作流程建议：
```
main (主分支)
  ├─ feature/ai-engine (A组长开发分支)
  ├─ feature/database (成员B开发分支)
  └─ feature/frontend (成员C开发分支)
```

---

## 六、后续协作建议

### 6.1 成员B需要完成的工作：
1. 创建MySQL数据库和表结构（用户表、课程表、活动表等）
2. 实现用户中心与安全模块（登录注册、密码加密）
3. 实现校园业务工具模块（课程表查询、活动预约等）
4. 完善DAO层和Entity层的代码

### 6.2 成员C需要完成的工作：
1. 开发前端界面（JavaFX或Vue+ElementPlus）
2. 实现AI聊天交互界面（类似ChatGPT的对话流）
3. 实现校园热点数据展示（Echarts图表）
4. 编写网络爬虫获取校园最新资讯

### 6.3 协作接口约定：
- 前端调用A组长的API基础地址：`http://localhost:8080/api`
- API文档访问地址：`http://localhost:8080/api/doc.html`
- 所有接口返回统一格式：`Result<T>` （code, message, data, timestamp）
- 流式接口使用SSE协议，前端需支持EventSource或fetch stream

---

## 七、亮点功能实现清单（拿A级关键）

### 已完成的亮点功能：

✅ **RAG检索增强生成**
- 文档知识库加载与管理
- 智能文本分块算法
- 余弦相似度语义检索
- 检索上下文自动注入

✅ **多线程并发控制**
- 双线程池架构（AI处理 + 流式响应）
- 异步任务执行器封装
- 基于滑动窗口的请求限流
- 防止前端界面卡顿

### 待完善的优化方向：

🔄 **向量数据库升级**（可选）
- 当前使用内存存储，可升级为Milvus/Chroma专业向量数据库
- 接入阿里云text-embedding模型进行专业向量化

🔄 **会话持久化**（可选）
- 当前会话存储在内存中，可结合Redis实现分布式会话管理
- 支持会话历史记录查询和导出

---

## 八、常见问题排查

### Q1: 启动报错 "Cannot resolve DashScope API"
**解决方案**: 在application.yml中配置正确的阿里云DashScope API Key，或设置环境变量 `DASHSCOPE_API_KEY`

### Q2: RAG知识库为空
**解决方案**: 将校园文档（规章制度、选课指南等）放入 `./data/knowledge-base/` 目录，支持.txt/.md/.pdf格式

### Q3: 前端无法连接后端
**解决方案**: 检查跨域配置（CorsConfig），确保前端地址在允许列表中；检查防火墙是否放行8080端口

### Q4: 流式接口无响应
**解决方案**: 检查浏览器是否支持SSE（Server-Sent Events）；确认前端使用了正确的EventSource或fetch stream方式调用

---

**文档编写人**: A组长
**编写日期**: 2026-06-15
**项目版本**: v1.0.0-initial-setup
