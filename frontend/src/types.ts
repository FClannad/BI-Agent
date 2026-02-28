import type { EChartsOption } from "echarts";

export type Role = "user" | "assistant";

export type TextMessage = { role: Role; type: "text"; text: string };
export type TableMessage = { role: Role; type: "table"; table: TableResult };
export type ChartMessage = { role: Role; type: "chart"; option: EChartsOption };

export type UiMessage =
  | TextMessage
  | TableMessage
  | ChartMessage;

export type ChatRequest = {
  message: string;
  sessionId?: string;
};

export type ChatResponse = {
  text: string;
  table?: unknown;
  chart?: unknown;
};

export type ChatEvent = {
  type: "start" | "token" | "done";
  data: string;
};

export type TableResult = {
  columns: { name: string; type: string }[];
  rows: Record<string, unknown>[];
};
