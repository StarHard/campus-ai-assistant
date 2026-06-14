# 校园智能服务小助手 - 环境配置指南

## 一、开发环境要求

### 1.1 必需软件

| 软件 | 版本要求 | 用途 | 下载地址 |
|------|----------|------|----------|
| **JDK** | 17+ (推荐17或21) | Java运行环境 | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 或 [Adoptium](https://adoptium.net/) |
| **Maven** | 3.8+ | 项目构建与依赖管理 | [Maven官网](https://maven.apache.org/download.cgi) |
| **MySQL** | 8.0+ | 数据库（成员B负责） | [MySQL官网](https://dev.mysql.com/downloads/) |
| **IDE** | IntelliJ IDEA 2023.2+ | 开发工具（推荐安装Alibaba代码规范插件） | [JetBrains](https://www.jetbrains.com/idea/download/) |
| **Git** | 2.30+ | 版本控制 | [Git官网](https://git-scm.com/downloads) |

### 1.2 可选软件

| 软件 | 用途 | 说明 |
|------|------|------|
| Navicat/DBeaver | 数据库可视化管理 | 方便查看和操作MySQL数据库 |
| Postman/Apifox | API接口测试 | 测试RESTful接口 |
| Redis | 缓存/会话存储 | 用于会话持久化（可选） |

---

## 二、环境配置步骤

### 步骤1: 安装JDK 17

#### Windows系统：
1. 下载JDK 17安装包（推荐Adoptium的Temurin发行版，开源免费）
2. 运行安装程序，选择安装路径（建议：`C:\Program Files\Java\jdk-17`）
3. 配置环境变量：
   - 打开"系统属性" → "高级" → "环境变量"
   - 新建系统变量 `JAVA_HOME` = `C:\Program Files\Java\jdk-17`
   - 编辑 `Path` 变量，添加 `%JAVA_HOME%\bin`
4. 验证安装：打开CMD或PowerShell，执行：
   ```bash
   java -version
   # 应输出类似：openjdk version "17.x.x"
   ```

#### macOS系统：
```bash
# 使用Homebrew安装
brew install openjdk@17

# 配置环境变量
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc

# 验证
java -version
```

#### Linux系统：
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# 验证
java -version
```

---

### 步骤2: 安装Maven

#### Windows系统：
1. 从官网下载Maven二进制包（apache-maven-3.9.x-bin.zip）
2. 解压到指定目录（如 `D:\Tools\apache-maven-3.9.x`）
3. 配置环境变量：
   - 新建 `MAVEN_HOME` = `D:\Tools\apache-maven-3.9.x`
   - 在 `Path` 中添加 `%MAVEN_HOME%\bin`
4. （可选）配置阿里云镜像加速，编辑 `%USERPROFILE%\.m2\settings.xml`：
   ```xml
   <mirrors>
     <mirror>
       <id>aliyunmaven</id>
       <mirrorOf>*</mirrorOf>
       <name>阿里云公共仓库</name>
       <url>https://maven.aliyun.com/repository/public</url>
     </mirror>
   </mirrors>
   ```
5. 验证安装：
   ```bash
   mvn -v
   # 应输出Apache Maven版本信息
   ```

#### macOS/Linux系统：
```bash
# macOS使用Homebrew
brew install maven

# 验证
mvn -v
```

---

### 步骤3: 安装并配置MySQL

1. 下载并安装MySQL 8.0+
2. 安装时设置root密码（记住这个密码！）
3. 启动MySQL服务
4. 创建项目数据库：
   ```sql
   CREATE DATABASE campus_assistant CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   SHOW DATABASES;  -- 确认数据库已创建
   ```
5. （可选）使用Navicat/DBeaver连接测试

---

### 步骤4: 获取阿里云DashScope API Key

**这是AI功能正常运行的关键！**

1. 注册/登录[阿里云账号](https://www.aliyun.com/)
2. 访问[DashScope控制台](https://dashscope.console.aliyun.com/)
3. 进入"API-KEY管理"页面
4. 创建新的API Key（如果还没有）
5. 复制保存API Key（格式：sk-xxxxxxxxxxxxxxxx）

**安全提示**：
- ❌ 不要将API Key提交到Git仓库
- ✅ 推荐使用环境变量存储
- ✅ 生产环境应使用密钥管理服务（KMS）

---

### 步骤5: 克隆/导入项目到IDE

#### 方法A: 命令行克隆（推荐）
```bash
cd D:\TRAE IDE\project
git clone <your-repository-url> campus-ai-assistant
cd campus-ai-assistant
```

#### 方法B: IDEA导入
1. 打开IntelliJ IDEA
2. 选择 "File" → "Open"
3. 选择项目根目录 `campus-ai-assistant`
4. 等待Maven自动下载依赖（首次可能需要几分钟）
5. 如果依赖下载失败，检查Maven配置和代理设置

---

### 步骤6: 配置应用参数

#### 方式1: 环境变量（推荐用于生产环境）
在系统环境变量中设置：
```
DASHSCOPE_API_KEY=sk-你的真实API密钥
```

#### 方式2: 直接修改配置文件（仅限开发环境）
编辑 `src/main/resources/application.yml`：

```yaml
spring-ai:
  dashscope:
    api-key: sk-你的真实API密钥  # 替换这行！
```

同时修改数据库连接信息（等成员B提供后更新）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_assistant?...
    username: root
    password: 你的MySQL密码
```

---

### 步骤7: 安装IDE插件（推荐）

#### IntelliJ IDEA必装插件：
1. **Alibaba Java Coding Guidelines**
   - 安装路径：Settings → Plugins → 搜索 "Alibaba"
   - 用途：代码规范检查，确保符合老师要求的编码标准
   - 使用：右键代码 → "编码规约扫描"

2. **Lombok**
   - 自动生成getter/setter/构造方法等
   - 项目已集成此依赖

3. **Knife4j**
   - API文档增强（可选，已有Web界面）

4. **.ignore**
   - Git忽略文件模板生成

---

## 三、启动项目

### 3.1 通过IDE启动（开发模式）

1. 在IDEA中打开项目
2. 找到启动类：`com.campus.ai.CampusAiAssistantApplication`
3. 右键 → "Run 'CampusAiAssistantApplication'"
4. 等待控制台输出：
   ```
   ===========================================
      校园智能服务小助手启动成功！
      访问地址: http://localhost:8080/api
      API文档: http://localhost:8080/api/doc.html
   ===========================================
   ```
5. 打开浏览器访问上述地址验证

### 3.2 通过命令行启动

```bash
# 进入项目目录
cd campus-ai-assistant

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 运行jar包
java -jar target/campus-ai-assistant-1.0.0.jar
```

### 3.3 通过Maven启动

```bash
mvn spring-boot:run
```

---

## 四、验证安装成功

### 4.1 检查服务是否正常

访问以下URL确认各模块正常工作：

| URL | 预期结果 | 说明 |
|-----|----------|------|
| `http://localhost:8080/api/doc.html` | 显示API文档页面 | Knife4j文档界面 |
| `http://localhost:8080/api/druid/` | Druid监控页面 | 数据库连接池监控 |

### 4.2 测试AI问答接口

使用Postman或curl测试：

```bash
# 同步问答测试
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"学校几点上课？","enableRag":true}'

# 简单问答测试（GET方式）
curl "http://localhost:8080/api/chat/simple?question=你好"
```

### 4.3 测试RAG知识库接口

```bash
# 查看知识库状态
curl http://localhost:8080/api/rag/stats

# 测试检索功能
curl "http://localhost:8080/api/rag/test-retrieval?query=选课"
```

---

## 五、知识库准备（RAG功能）

为了让AI能够回答校园相关问题，需要准备校园文档知识库：

### 5.1 创建知识库目录

```bash
# 在项目根目录下创建
mkdir data/knowledge-base
```

### 5.2 准备文档文件

将以下类型的文档放入 `data/knowledge-base/` 目录：

✅ **支持的文档格式**：
- `.txt` - 纯文本文件
- `.md` - Markdown文件
- `.pdf` - PDF文档
- `.doc` / `.docx` - Word文档

📄 **推荐的文档内容**：
- 学校规章制度汇编
- 学生手册
- 选课指南/培养方案
- 校历/作息时间表
- 专业介绍/培训方案
- 校园生活常见问题FAQ

### 5.3 文档示例格式

创建 `data/knowledge-base/校规校纪.txt`：
```
第一章 总则

第一条 为了维护学校正常的教育教学秩序和生活秩序...
...

第二章 学籍管理

第五条 学生入学注册...
...
```

### 5.4 重启服务加载知识库

修改配置或重启应用后，系统会自动加载知识库目录下的所有文档。

---

## 六、常见问题解决

### 问题1: Maven依赖下载失败

**症状**: `Could not find artifact ...`

**解决方案**:
1. 检查网络连接和代理设置
2. 配置阿里云Maven镜像（见步骤2）
3. 清理本地缓存：`mvn dependency:purge-local-repository`
4. 重试：`mvn clean install -U`

### 问题2: JDK版本不匹配

**症状**: `Unsupported class file major version 61` 或 `62`

**解决方案**:
- 确保使用JDK 17（major version 61）或JDK 21（major version 65）
- IDEA中设置：File → Project Structure → SDK选择JDK 17
- 检查JAVA_HOME环境变量

### 问题3: DashScope API调用失败

**症状**: `401 Unauthorized` 或 `Invalid API Key`

**解决方案**:
1. 确认API Key正确且未过期
2. 检查是否设置了正确的环境变量
3. 确认DashScope账户余额充足（新用户有免费额度）
4. 查看控制台日志中的详细错误信息

### 问题4: MySQL连接失败

**症状**: `Communications link failure` 或 `Access denied`

**解决方案**:
1. 确认MySQL服务已启动
2. 检查用户名密码是否正确
3. 确认数据库 `campus_assistant` 已创建
4. 检查防火墙是否放行3306端口
5. （可选）暂时注释掉MyBatis相关配置，先启动AI功能

### 问题5: 端口被占用

**症状**: `Port 8080 was already in use`

**解决方案**:
1. 查找占用端口的进程：Windows用 `netstat -ano | findstr 8080`
2. 结束占用进程或修改application.yml中的server.port
3. Linux/Mac使用 `lsof -i :8080` 查看

---

## 七、性能优化建议

### 7.1 JVM参数优化（生产环境）

编辑启动命令，添加JVM参数：
```bash
java -Xms512m -Xmx1024 \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar target/campus-ai-assistant-1.0.0.jar
```

### 7.2 连接池调优

根据实际并发量调整Druid配置：
```yaml
spring:
  datasource:
    druid:
      initial-size: 10        # 初始连接数
      max-active: 100         # 最大活跃连接数
      max-wait: 60000         # 获取连接超时时间(ms)
```

### 7.3 AI模型选择建议

| 场景 | 推荐模型 | 特点 |
|------|----------|------|
| 快速响应 | qwen-turbo | 速度快、成本低 |
| 平衡性能 | qwen-plus | 性价比高 |
| 高质量回答 | qwen-max | 质量最好、速度较慢 |
| 复杂推理 | qwen-max-longcontext | 支持长文本 |

---

## 八、团队协作注意事项

### 8.1 代码规范

✅ **必须遵守**：
- 安装并运行Alibaba Java Coding Guidelines插件
- 修复所有Yellow警告和Red错误
- 遵循驼峰命名法
- 类/方法必须有清晰的注释

❌ **禁止事项**：
- 不要提交包含敏感信息的文件（API Key、密码等）
- 不要直接在main分支开发，使用feature分支
- 不要提交IDE生成的配置文件（.idea/, *.iml等）

### 8.2 Git协作流程

```bash
# 1. 创建自己的功能分支
git checkout -b feature/ai-engine

# 2. 开发完成后提交
git add .
git commit -m "feat: 完成AI智能问答引擎基础框架"

# 3. 推送到远程
git push origin feature/ai-engine

# 4. 发起Pull Request合并到主分支
```

### 8.3 分工接口约定

| 模块 | 负责人 | 接口前缀 | 状态 |
|------|--------|----------|------|
| AI问答引擎 | A组长 | `/api/chat/*` | ✅ 已完成基础框架 |
| RAG知识库 | A组长 | `/api/rag/*` | ✅ 已完成基础框架 |
| 用户管理 | 成员B | `/api/user/*` | ⏳ 待实现 |
| 课程业务 | 成员B | `/api/course/*` | ⏳ 待实现 |
| 前端界面 | 成员C | - | ⏳ 待实现 |

---

## 九、技术支持资源

### 官方文档
- [Spring Boot官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [SpringAI Alibaba文档](https://help.aliyun.com/zh/model-studio/getting-started)
- [阿里云DashScope API参考](https://help.aliyun.com/zh/model-studio/developer-reference/)

### 学习资源
- [通义千问API使用教程](https://github.com/AlibabaCloud/dashscope-sdk-java)
- [SpringAI官方示例](https://github.com/spring-projects/spring-ai)
- [MyBatis Plus官方文档](https://baomidou.com/pages/24112f/)

---

**文档编写人**: A组长
**最后更新**: 2026-06-15
**适用版本**: v1.0.0-initial-setup
