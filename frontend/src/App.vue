<template>
  <div class="page">
    <header class="header">
      <div class="title">DeepBI</div>
      <div class="subtitle">BI Agent / 智能报表助手（MVP）</div>
    </header>

    <main class="main">
      <div class="messages">
        <div v-for="(m, idx) in messages" :key="idx" class="msg">
          <div class="role" :class="m.role">{{ m.role }}</div>
          <div class="bubble">
            <div v-if="m.type === 'text'" class="text">{{ m.text }}</div>
            <TableRenderer v-else-if="m.type === 'table'" :table="m.table" />
            <ChartRenderer v-else-if="m.type === 'chart'" :option="m.option" />
          </div>
        </div>
      </div>
    </main>

    <footer class="footer">
      <el-input
        v-model="input"
        type="textarea"
        :rows="2"
        placeholder="比如：帮我看看去年湖南地区的销售额趋势"
        @keydown.enter.exact.prevent="send()"
      />
      <div class="actions">
        <el-button type="primary" :disabled="busy || !input.trim()" @click="send()">发送</el-button>
        <el-button type="success" :disabled="busy || !input.trim()" @click="askBi()">问数(Text→SQL)</el-button>
        <el-button :disabled="busy" @click="sendStream()">流式发送(SSE)</el-button>
        <el-button :disabled="busy" @click="clear()">清空</el-button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { chatOnce, chatStream } from "./api/chat";
import { biAsk } from "./api/bi";
import TableRenderer from "./components/TableRenderer.vue";
import ChartRenderer from "./components/ChartRenderer.vue";
import type { TextMessage, UiMessage } from "./types";

const input = ref("");
const busy = ref(false);
const messages = reactive<UiMessage[]>([]);

function pushUser(text: string) {
  messages.push({ role: "user", type: "text", text });
}

function pushAssistantText(text: string) {
  messages.push({ role: "assistant", type: "text", text });
}

async function send() {
  const text = input.value.trim();
  if (!text) return;

  input.value = "";
  busy.value = true;
  pushUser(text);

  try {
    const resp = await chatOnce({ message: text });
    pushAssistantText(resp.text ?? "");
  } catch (e: any) {
    pushAssistantText(`Error: ${e?.message ?? String(e)}`);
  } finally {
    busy.value = false;
  }
}

async function askBi() {
  const text = input.value.trim();
  if (!text) return;

  input.value = "";
  busy.value = true;
  pushUser(text);

  try {
    const resp = await biAsk({ question: text });

    const parts: string[] = [];
    if (resp.text) parts.push(resp.text);
    if (resp.clarifyQuestion) parts.push(`澄清问题：${resp.clarifyQuestion}`);
    if (resp.sql) parts.push(`SQL:\n${resp.sql}`);
    pushAssistantText(parts.join("\n\n"));

    if (resp.table) {
      messages.push({ role: "assistant", type: "table", table: resp.table });
    }
  } catch (e: any) {
    pushAssistantText(`Error: ${e?.message ?? String(e)}`);
  } finally {
    busy.value = false;
  }
}

async function sendStream() {
  const text = input.value.trim();
  if (!text) return;

  input.value = "";
  busy.value = true;
  pushUser(text);

  const assistant: TextMessage = { role: "assistant", type: "text", text: "" };
  messages.push(assistant);

  try {
    await chatStream(
      { message: text },
      (evt) => {
        if (evt.type === "token") assistant.text += evt.data;
      },
      (err) => {
        assistant.text += `\n[stream error] ${err?.message ?? String(err)}`;
      }
    );
  } finally {
    busy.value = false;
  }
}

function clear() {
  messages.splice(0, messages.length);
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 960px;
  margin: 0 auto;
  padding: 12px;
  gap: 12px;
}
.header {
  padding: 8px 0;
}
.title {
  font-size: 20px;
  font-weight: 700;
}
.subtitle {
  font-size: 12px;
  color: #666;
}
.main {
  flex: 1;
  overflow: auto;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}
.messages {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.msg {
  display: flex;
  gap: 10px;
}
.role {
  width: 80px;
  font-size: 12px;
  color: #666;
  text-transform: uppercase;
  padding-top: 4px;
}
.role.user {
  color: #2b6cb0;
}
.role.assistant {
  color: #2f855a;
}
.bubble {
  flex: 1;
  padding: 10px 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
}
.text {
  white-space: pre-wrap;
  line-height: 1.5;
}
.footer {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
