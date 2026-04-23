import { ERROR_CODES } from "../constants/error-codes";

const DB_KEY = "rag-bilibili-dev-db";

function nowString() {
  const now = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(
    now.getMinutes()
  )}:${pad(now.getSeconds())}`;
}

function clampText(value, limit = 120) {
  const text = String(value || "").trim();
  if (!text) {
    return "";
  }
  return text.length > limit ? `${text.slice(0, limit)}...` : text;
}

function createError(code, message) {
  return {
    normalized: true,
    code,
    message,
    detail: {
      code,
      message,
    },
  };
}

function extractBvid(input) {
  const matched = String(input || "").match(/BV[a-zA-Z0-9]+/);
  return matched ? matched[0] : "";
}

function normalizeSession(session) {
  return {
    ...session,
    conversationSummary: session?.conversationSummary ?? null,
    summaryUpdateTime: session?.summaryUpdateTime ?? null,
  };
}

function normalizeDatabase(db) {
  return {
    ...db,
    sessions: (db.sessions || []).map((session) => normalizeSession(session)),
  };
}

function defaultDatabase() {
  const createTime = nowString();
  return {
    nextVideoId: 4,
    nextSessionId: 3,
    nextMessageId: 7,
    videos: [
      {
        id: 1,
        bvid: "BV1DCfsBKExV",
        title: "Spring AI Alibaba 入门与 RAG 基础",
        description: "介绍字幕读取、分片、向量化、过滤条件以及基础问答链路。",
        chunkCount: 48,
        importTime: createTime,
        status: "SUCCESS",
        failReason: null,
      },
      {
        id: 2,
        bvid: "BV1sa4y1K7un",
        title: "DashVector 检索过滤实践",
        description: "围绕 user_id、bvid 过滤检索与删除联动的实现思路。",
        chunkCount: 36,
        importTime: createTime,
        status: "SUCCESS",
        failReason: null,
      },
      {
        id: 3,
        bvid: "BV1qA411M7zk",
        title: "多轮问答与 SSE 流式返回",
        description: "演示 start/content/end 事件驱动的流式聊天体验。",
        chunkCount: 29,
        importTime: createTime,
        status: "SUCCESS",
        failReason: null,
      },
    ],
    sessions: [
      {
        id: 1,
        sessionType: "SINGLE_VIDEO",
        videoId: 1,
        videoTitle: "Spring AI Alibaba 入门与 RAG 基础",
        createTime,
        conversationSummary: "最近围绕 Spring AI Alibaba 的 RAG 入门链路进行讨论，重点包括字幕读取、文本切分和向量检索。",
        summaryUpdateTime: createTime,
      },
      {
        id: 2,
        sessionType: "ALL_VIDEOS",
        videoId: null,
        videoTitle: null,
        createTime,
        conversationSummary: "最近在全视频知识库范围内讨论删除联动，涉及主记录、分片、向量索引和关联会话的清理。",
        summaryUpdateTime: createTime,
      },
    ],
    messages: {
      1: [
        {
          id: 1,
          role: "USER",
          content: "这个视频讲了什么？",
          createTime,
        },
        {
          id: 2,
          role: "ASSISTANT",
          content: "它主要讲了如何用 Spring AI Alibaba 读取 B 站字幕、切分文本并写入向量库，再围绕这些内容构建问答能力。",
          createTime,
        },
      ],
      2: [
        {
          id: 3,
          role: "USER",
          content: "删除视频时需要清理哪些数据？",
          createTime,
        },
        {
          id: 4,
          role: "ASSISTANT",
          content: "需要删除视频主记录、分片记录、向量映射、DashVector 中对应的向量，以及相关单视频会话。",
          createTime,
        },
      ],
    },
  };
}

function readDatabase() {
  const raw = localStorage.getItem(DB_KEY);
  if (!raw) {
    const seeded = defaultDatabase();
    writeDatabase(seeded);
    return seeded;
  }

  try {
    const parsed = JSON.parse(raw);
    const normalized = normalizeDatabase(parsed);
    writeDatabase(normalized);
    return normalized;
  } catch {
    const seeded = defaultDatabase();
    writeDatabase(seeded);
    return seeded;
  }
}

function writeDatabase(db) {
  localStorage.setItem(DB_KEY, JSON.stringify(normalizeDatabase(db)));
}

function delay(ms = 220) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

function classifyQueryIntent(prompt) {
  const text = String(prompt || "").trim().toLowerCase();
  const broadKeywords = ["总结", "对比", "全面", "整体", "分别", "所有", "有哪些方面", "系统介绍", "完整"];

  if (!text || text.length <= 8 || /^(这个|那个|它|啥意思|什么意思)[^，。！？]*$/.test(text)) {
    return "AMBIGUOUS";
  }

  if (text.length >= 24 || broadKeywords.some((keyword) => text.includes(keyword))) {
    return "BROAD";
  }

  return "DIRECT";
}

function rewriteQuery(prompt, intent) {
  const text = String(prompt || "").trim();
  if (!text) {
    return "";
  }
  if (intent === "AMBIGUOUS") {
    return `结合当前会话上下文，解释并补全这个问题的具体指向：${text}`;
  }
  if (intent === "BROAD") {
    return `围绕以下问题拆成几个互补主题检索后再回答：${text}`;
  }
  return text;
}

function buildResponseMeta(session, prompt) {
  const queryIntent = classifyQueryIntent(prompt);
  const rewrittenQuery = rewriteQuery(prompt, queryIntent);
  const existingSummary = Boolean(session.conversationSummary);
  const conversationLength = (readDatabase().messages[session.id] || []).length;
  const summaryUsed = existingSummary || conversationLength >= 4;

  const baseMeta = {
    DIRECT: { confidenceLabel: "HIGH", confidenceScore: 0.88, sourceCount: 5, knowledgeGap: false },
    AMBIGUOUS: { confidenceLabel: "MEDIUM", confidenceScore: 0.66, sourceCount: 4, knowledgeGap: false },
    BROAD: { confidenceLabel: "LOW", confidenceScore: 0.46, sourceCount: 3, knowledgeGap: true },
  };

  const exactnessKeywords = ["源码", "精确", "百分比", "哪一行", "具体时间", "原文"];
  const needsExactness = exactnessKeywords.some((keyword) => String(prompt || "").includes(keyword));
  const meta = { ...baseMeta[queryIntent] };

  if (needsExactness) {
    meta.confidenceScore = Math.max(0.28, meta.confidenceScore - 0.12);
    meta.confidenceLabel = meta.confidenceScore >= 0.75 ? "HIGH" : meta.confidenceScore >= 0.55 ? "MEDIUM" : "LOW";
    meta.knowledgeGap = true;
  }

  return {
    queryIntent,
    rewrittenQuery,
    confidenceLabel: meta.confidenceLabel,
    confidenceScore: meta.confidenceScore,
    sourceCount: meta.sourceCount,
    knowledgeGap: meta.knowledgeGap,
    summaryUsed,
  };
}

function buildAssistantReply(session, prompt, responseMeta) {
  const scopeReply =
    session.sessionType === "SINGLE_VIDEO"
      ? "当前会话限定在单视频范围内，系统会按照 user_id 和 bvid 过滤相关字幕分片，再结合上下文组织回答。"
      : "当前会话限定在全部视频范围内，系统会按 user_id 检索当前用户导入的全部视频内容，再汇总生成答案。";

  const strategyReply =
    responseMeta.queryIntent === "AMBIGUOUS"
      ? "这次问题偏模糊，系统先做了问题补全。"
      : responseMeta.queryIntent === "BROAD"
        ? "这次问题范围较大，系统先拆成多个角度再检索。"
        : "这次问题足够明确，系统直接进入检索。";

  const confidenceReply =
    responseMeta.confidenceLabel === "LOW"
      ? "当前命中文档数量有限，结论更适合当作方向性参考。"
      : responseMeta.confidenceLabel === "MEDIUM"
        ? "当前回答基于有限上下文生成，建议继续追问关键点。"
        : "当前回答基于较充分的命中内容生成。";

  return `${scopeReply} ${strategyReply} 你刚刚的问题是“${prompt}”。${confidenceReply} 在开发模式下，这段回复来自前端本地 mock SSE 流。`;
}

function buildSessionSummary(session, prompt, reply) {
  const scope = session.sessionType === "SINGLE_VIDEO" ? `围绕《${session.videoTitle || "当前视频"}》` : "围绕全视频知识库";
  return `${scope}，最近讨论了“${clampText(prompt, 24)}”，AI 给出的重点是：${clampText(reply, 88)}`;
}

function updateSessionSummary(db, sessionId, prompt, reply) {
  const session = db.sessions.find((item) => item.id === Number(sessionId));
  if (!session) {
    return;
  }
  session.conversationSummary = buildSessionSummary(session, prompt, reply);
  session.summaryUpdateTime = nowString();
}

export const devServer = {
  async importVideo(payload) {
    await delay();
    const db = readDatabase();
    const bvid = extractBvid(payload.bvidOrUrl);

    if (!bvid) {
      throw createError(ERROR_CODES.BVID_PARSE_ERROR, "无法从输入内容中解析 BV 号。");
    }

    if (db.videos.some((item) => item.bvid === bvid)) {
      throw createError(ERROR_CODES.VIDEO_ALREADY_EXISTS, "该视频已存在，请先删除后再导入。");
    }

    const video = {
      id: db.nextVideoId++,
      bvid,
      title: `开发模式视频 ${bvid}`,
      description: "该记录由前端本地开发模式生成，用于测试导入、会话和问答流程。",
      chunkCount: 24 + Math.floor(Math.random() * 24),
      importTime: nowString(),
      status: "SUCCESS",
      failReason: null,
    };

    db.videos.unshift(video);
    writeDatabase(db);
    return video;
  },

  async listVideos() {
    await delay();
    const db = readDatabase();
    return [...db.videos];
  },

  async getVideo(id) {
    await delay();
    const db = readDatabase();
    const video = db.videos.find((item) => item.id === Number(id));
    if (!video) {
      throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "视频不存在或已被删除。");
    }
    return video;
  },

  async rebuildVideo(id, payload) {
    await delay();
    const db = readDatabase();
    const video = db.videos.find((item) => item.id === Number(id));
    if (!video) {
      throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "视频不存在或已被删除。");
    }
    if (!payload?.sessdata || !payload?.biliJct || !payload?.buvid3) {
      throw createError(ERROR_CODES.PARAM_ERROR, "请完整填写重建索引所需的 3 个凭证字段。");
    }

    video.status = "IMPORTING";
    video.failReason = null;
    video.importTime = nowString();
    video.chunkCount = 32 + Math.floor(Math.random() * 24);
    writeDatabase(db);

    await delay(320);
    video.status = "SUCCESS";
    video.description = "该记录已通过开发模式重建索引，模拟了重新抓取字幕、重新切分与重新写入向量库。";
    writeDatabase(db);

    return { ...video };
  },

  async removeVideo(id) {
    await delay();
    const db = readDatabase();
    const targetId = Number(id);
    const exists = db.videos.some((item) => item.id === targetId);
    if (!exists) {
      throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "视频不存在或已被删除。");
    }

    db.videos = db.videos.filter((item) => item.id !== targetId);
    const removedSessionIds = db.sessions.filter((item) => item.videoId === targetId).map((item) => item.id);
    db.sessions = db.sessions.filter((item) => item.videoId !== targetId);
    removedSessionIds.forEach((sessionId) => {
      delete db.messages[sessionId];
    });
    writeDatabase(db);
    return null;
  },

  async listSessions() {
    await delay();
    const db = readDatabase();
    return [...db.sessions];
  },

  async createSession(payload) {
    await delay();
    const db = readDatabase();

    if (payload.sessionType !== "SINGLE_VIDEO" && payload.sessionType !== "ALL_VIDEOS") {
      throw createError(ERROR_CODES.SESSION_TYPE_ERROR, "会话类型不合法，请重新创建会话。");
    }

    let videoTitle = null;
    let videoId = null;

    if (payload.sessionType === "SINGLE_VIDEO") {
      if (!payload.videoId) {
        throw createError(ERROR_CODES.PARAM_ERROR, "单视频对话必须选择目标视频。");
      }
      const video = db.videos.find((item) => item.id === Number(payload.videoId));
      if (!video) {
        throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "目标视频不存在。");
      }
      videoId = video.id;
      videoTitle = video.title;
    }

    const session = {
      id: db.nextSessionId++,
      sessionType: payload.sessionType,
      videoId,
      videoTitle,
      createTime: nowString(),
      conversationSummary: null,
      summaryUpdateTime: null,
    };

    db.sessions.unshift(session);
    db.messages[session.id] = [];
    writeDatabase(db);
    return session;
  },

  async getSession(id) {
    await delay();
    const db = readDatabase();
    const session = db.sessions.find((item) => item.id === Number(id));
    if (!session) {
      throw createError(ERROR_CODES.SESSION_NOT_FOUND, "会话不存在或已被删除。");
    }
    return session;
  },

  async removeSession(id) {
    await delay();
    const db = readDatabase();
    const targetId = Number(id);
    const exists = db.sessions.some((item) => item.id === targetId);
    if (!exists) {
      throw createError(ERROR_CODES.SESSION_NOT_FOUND, "会话不存在或已被删除。");
    }
    db.sessions = db.sessions.filter((item) => item.id !== targetId);
    delete db.messages[targetId];
    writeDatabase(db);
    return null;
  },

  async listMessages(sessionId) {
    await delay();
    const db = readDatabase();
    const session = db.sessions.find((item) => item.id === Number(sessionId));
    if (!session) {
      throw createError(ERROR_CODES.SESSION_NOT_FOUND, "会话不存在或已被删除。");
    }
    return [...(db.messages[sessionId] || [])];
  },

  async streamMessage(sessionId, payload, handlers = {}, signal) {
    const db = readDatabase();
    const session = db.sessions.find((item) => item.id === Number(sessionId));
    if (!session) {
      throw createError(ERROR_CODES.SESSION_NOT_FOUND, "会话不存在或已被删除。");
    }
    if (!payload.content) {
      throw createError(ERROR_CODES.PARAM_ERROR, "消息内容不能为空。");
    }

    const userMessage = {
      id: db.nextMessageId++,
      role: "USER",
      content: payload.content,
      createTime: nowString(),
    };
    db.messages[session.id] = [...(db.messages[session.id] || []), userMessage];
    writeDatabase(db);

    if (handlers.start) {
      await handlers.start({
        type: "start",
        userMessageId: userMessage.id,
      });
    }

    const responseMeta = buildResponseMeta(session, payload.content);
    const fullReply = buildAssistantReply(session, payload.content, responseMeta);
    const chunks = fullReply.match(/.{1,9}/g) || [fullReply];
    let built = "";

    for (const chunk of chunks) {
      if (signal?.aborted) {
        const abortError = new Error("已取消流式响应");
        abortError.name = "AbortError";
        throw abortError;
      }
      built += chunk;
      await delay(120);
      if (handlers.content) {
        await handlers.content({
          type: "content",
          delta: chunk,
        });
      }
    }

    const latestDb = readDatabase();
    const assistantMessage = {
      id: latestDb.nextMessageId++,
      role: "ASSISTANT",
      content: built,
      createTime: nowString(),
    };
    latestDb.messages[session.id] = [...(latestDb.messages[session.id] || []), assistantMessage];
    updateSessionSummary(latestDb, session.id, payload.content, built);
    writeDatabase(latestDb);

    if (handlers.end) {
      await handlers.end({
        type: "end",
        userMessageId: userMessage.id,
        assistantMessageId: assistantMessage.id,
        fullContent: built,
        ...responseMeta,
      });
    }
  },
};
