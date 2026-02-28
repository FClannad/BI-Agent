import type { ChatEvent, ChatRequest, ChatResponse } from "../types";

export async function chatOnce(req: ChatRequest): Promise<ChatResponse> {
  const resp = await fetch("/api/chat", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });

  if (!resp.ok) {
    const text = await resp.text().catch(() => "");
    throw new Error(`HTTP ${resp.status}: ${text}`);
  }

  return (await resp.json()) as ChatResponse;
}

export async function chatStream(
  req: ChatRequest,
  onEvent: (e: ChatEvent) => void,
  onError?: (err: unknown) => void
): Promise<void> {
  const url = new URL("/api/chat/stream", window.location.origin);
  url.searchParams.set("message", req.message);
  if (req.sessionId) url.searchParams.set("sessionId", req.sessionId);

  return new Promise((resolve) => {
    const es = new EventSource(url.toString());

    const safeClose = () => {
      try {
        es.close();
      } finally {
        resolve();
      }
    };

    es.addEventListener("token", (evt) => {
      const data = (evt as MessageEvent).data ?? "";
      onEvent({ type: "token", data });
    });

    es.addEventListener("start", () => onEvent({ type: "start", data: "" }));
    es.addEventListener("done", () => {
      onEvent({ type: "done", data: "" });
      safeClose();
    });

    es.onerror = (e) => {
      onError?.(e);
      safeClose();
    };
  });
}

