# DeepBI（BI Agent / 智能报表助手）

DeepBI 是一个“对话式数据分析 Agent”：将自然语言问题自动转成 **只读 SQL**，安全查询 MySQL，并输出表格/图表（ECharts）与文字洞察，支持多轮追问。

## 文档
- 详细设计与落地路线：`设计.md`
- 英文/ASCII 入口（便于跨平台查看）：`DESIGN.md`

## 快速启动（本仓库骨架）
### 1) 后端（Spring Boot + Maven）
前置：Java 17、Maven、MySQL（可先不连库，仅体验对话接口也行）。

必需环境变量（OpenAI）：
- `OPENAI_API_KEY`：OpenAI API Key
- 可选：`OPENAI_MODEL`（默认 `gpt-4o-mini`）

可选环境变量（MySQL 只读账号）：
- `DEEPBI_MYSQL_URL`（默认 `jdbc:mysql://localhost:3306/deepbi...`）
- `DEEPBI_MYSQL_USER`（默认 `deepbi_ro`）
- `DEEPBI_MYSQL_PASSWORD`

启动：
```bash
cd backend
mvn spring-boot:run
```

接口：
- `GET /health`
- `POST /api/chat`
- `GET /api/chat/stream?message=...`（SSE）
- `GET /api/sql/query?q=SELECT...`（调试用，受只读策略限制）

### 2) 前端（Vue 3 + Vite + Element Plus + ECharts）
前置：Node.js 18+（建议）。

启动：
```bash
cd frontend
npm i
npm run dev
```

说明：
- 默认通过 Vite 代理把 `/api` 转发到 `http://localhost:8080`
- “流式发送(SSE)” 会调用后端的 `/api/chat/stream`

## 计划实现（MVP → 迭代）
- M1：Text-to-SQL → SQL 校验 → MySQL 查询 → 表格展示
- M2：SSE 流式对话
- M3：自动生成 ECharts Option 并渲染图表
- M4：Schema RAG（向量检索 TopK 表结构）

## 建议仓库结构（后续落地时可采用）
```text
BI-Agent/
  backend/        # Spring Boot 3 + OpenAI(优先) LLM Client
  frontend/       # Vue 3 + Element Plus + ECharts
  docs/           # 更多设计/Prompt/测试集
  设计.md
  README.md
```
