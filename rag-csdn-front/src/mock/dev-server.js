import { ERROR_CODES } from "../constants/error-codes";

const DB_KEY = "rag-csdn-dev-db";
const LEGACY_DB_KEY = "rag-bilibili-dev-db";

function readStoredDb() {
  const currentValue = localStorage.getItem(DB_KEY);
  if (currentValue) {
    return currentValue;
  }

  const legacyValue = localStorage.getItem(LEGACY_DB_KEY);
  if (legacyValue) {
    localStorage.setItem(DB_KEY, legacyValue);
    localStorage.removeItem(LEGACY_DB_KEY);
    return legacyValue;
  }

  return null;
}

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

function extractArticleInfo(input) {
  const normalized = String(input || "").trim();
  const matched = normalized.match(/article\/details\/(\d+)/);
  return matched
    ? {
        sourceId: matched[1],
      sourceUrl: normalized.split("?")[0],
      }
    : null;
}

function extractAuthorInfo(input) {
  const normalized = String(input || "").trim();
  const matched = normalized.match(/blog\.csdn\.net\/([^/?#]+)/i);
  return matched ? matched[1] : null;
}

function createVideoRecord(db, articleInfo, title, description) {
  const video = {
    id: db.nextVideoId++,
    bvid: articleInfo.sourceId,
    sourceId: articleInfo.sourceId,
    sourceUrl: articleInfo.sourceUrl,
    title,
    description,
    chunkCount: 24 + Math.floor(Math.random() * 24),
    importTime: nowString(),
    status: "SUCCESS",
    failReason: null,
  };

  db.videos.unshift(video);
  return video;
}

function normalizeSession(session) {
  const normalizedType =
    session?.sessionType === "SINGLE_VIDEO"
      ? "SINGLE_ARTICLE"
      : session?.sessionType === "ALL_VIDEOS"
        ? "ALL_ARTICLES"
        : session?.sessionType;

  return {
    ...session,
    sessionType: normalizedType,
    articleId: session?.articleId ?? session?.videoId ?? null,
    articleTitle: session?.articleTitle ?? session?.videoTitle ?? null,
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
        bvid: "147000001",
        sourceId: "147000001",
        sourceUrl: "https://blog.csdn.net/spring/article/details/147000001",
        title: "Spring AI Alibaba 入门与 RAG 基础",
        description: "介绍 CSDN 文章抓取、分片、向量化、过滤条件以及基础问答链路。",
        chunkCount: 48,
        importTime: createTime,
        status: "SUCCESS",
        failReason: null,
      },
      {
        id: 2,
        bvid: "147000002",
        sourceId: "147000002",
        sourceUrl: "https://blog.csdn.net/vector/article/details/147000002",
        title: "DashVector 检索过滤实践",
        description: "围绕 user_id、source_id 过滤检索与删除联动的实现思路。",
        chunkCount: 36,
        importTime: createTime,
        status: "SUCCESS",
        failReason: null,
      },
      {
        id: 3,
        bvid: "147000003",
        sourceId: "147000003",
        sourceUrl: "https://blog.csdn.net/sse/article/details/147000003",
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
        sessionType: "SINGLE_ARTICLE",
        articleId: 1,
        articleTitle: "Spring AI Alibaba 入门与 RAG 基础",
        createTime,
        conversationSummary: "最近围绕 Spring AI Alibaba 的 RAG 入门链路进行讨论，重点包括字幕读取、文本切分和向量检索。",
        summaryUpdateTime: createTime,
      },
      {
        id: 2,
        sessionType: "ALL_ARTICLES",
        articleId: null,
        articleTitle: null,
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
          content: "这篇文章讲了什么？",
          createTime,
        },
        {
          id: 2,
          role: "ASSISTANT",
          content: "它主要讲了如何用 Spring AI Alibaba 读取 CSDN 文章、切分文本并写入向量库，再围绕这些内容构建问答能力。",
          createTime,
        },
      ],
      2: [
        {
          id: 3,
          role: "USER",
          content: "删除文章时需要清理哪些数据？",
          createTime,
        },
        {
          id: 4,
          role: "ASSISTANT",
          content: "需要删除文章主记录、分片记录、向量映射、DashVector 中对应的向量，以及相关单文章会话。",
          createTime,
        },
      ],
    },
  };
}

function readDatabase() {
  const raw = readStoredDb();
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
  localStorage.removeItem(LEGACY_DB_KEY);
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
    session.sessionType === "SINGLE_ARTICLE"
      ? "当前会话限定在单文章范围内，系统会按照 user_id 和 source_id 过滤相关内容分片，再结合上下文组织回答。"
      : "当前会话限定在全部文章范围内，系统会按 user_id 检索当前用户导入的全部文章内容，再汇总生成答案。";

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
  const scope = session.sessionType === "SINGLE_ARTICLE" ? `围绕《${session.articleTitle || "当前文章"}》` : "围绕全文章知识库";
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
    const articleInfo = extractArticleInfo(payload.articleUrl);

    if (!articleInfo) {
      throw createError(ERROR_CODES.BVID_PARSE_ERROR, "无法从输入内容中解析 CSDN 文章链接。");
    }

    if (db.videos.some((item) => item.bvid === articleInfo.sourceId)) {
      throw createError(ERROR_CODES.VIDEO_ALREADY_EXISTS, "该文章已存在，请先删除后再导入。");
    }

    const video = createVideoRecord(
      db,
      articleInfo,
      `开发模式文章 ${articleInfo.sourceId}`,
      "该记录由前端本地开发模式生成，用于测试 CSDN 文章导入、会话和问答流程。"
    );
    writeDatabase(db);
    return video;
  },

  async importAuthorArticles(payload = {}) {
    await delay();
    const db = readDatabase();
    const author = extractAuthorInfo(payload.authorUrl);
    if (!author) {
      throw createError(ERROR_CODES.PARAM_ERROR, "无法从输入内容中解析作者主页 URL。");
    }

    const limit = Math.max(1, Math.min(Number(payload.maxArticles) || 10, 30));
    const items = [];
    let submittedCount = 0;
    let duplicateCount = 0;

    for (let index = 1; index <= limit; index++) {
      const sourceId = `${author.replace(/[^a-z0-9]/gi, "").slice(0, 8) || "author"}${String(index).padStart(4, "0")}`;
      const sourceUrl = `https://blog.csdn.net/${author}/article/details/${sourceId}`;
      const existing = db.videos.find((item) => item.sourceId === sourceId);
      if (existing) {
        duplicateCount++;
        items.push({
          articleId: existing.id,
          sourceId,
          sourceUrl,
          title: existing.title,
          status: "SKIPPED_DUPLICATE",
          message: "该文章已存在，请先删除后再导入。",
        });
        continue;
      }

      const video = createVideoRecord(
        db,
        { sourceId, sourceUrl },
        `${author} 的公开文章 ${index}`,
        "该记录由前端本地开发模式批量生成，用于测试作者公开文章批量导入流程。"
      );
      submittedCount++;
      items.push({
        articleId: video.id,
        sourceId,
        sourceUrl,
        title: video.title,
        status: "SUBMITTED",
        message: "已提交导入任务",
      });
    }

    writeDatabase(db);
    return {
      mode: "AUTHOR_PUBLIC",
      target: `https://blog.csdn.net/${author}`,
      discoveredCount: limit,
      submittedCount,
      duplicateCount,
      failedCount: 0,
      items,
    };
  },

  async importRecommendedArticles(payload = {}) {
    await delay();
    const db = readDatabase();
    const limit = Math.max(1, Math.min(Number(payload.limit) || 8, 20));
    const items = [];
    let submittedCount = 0;
    let duplicateCount = 0;

    for (let index = 1; index <= limit; index++) {
      const sourceId = `recommend${String(index).padStart(4, "0")}`;
      const sourceUrl = `https://blog.csdn.net/recommend/article/details/${sourceId}`;
      const existing = db.videos.find((item) => item.sourceId === sourceId);
      if (existing) {
        duplicateCount++;
        items.push({
          articleId: existing.id,
          sourceId,
          sourceUrl,
          title: existing.title,
          status: "SKIPPED_DUPLICATE",
          message: "该文章已存在，请先删除后再导入。",
        });
        continue;
      }

      const video = createVideoRecord(
        db,
        { sourceId, sourceUrl },
        `首页公开推荐文章 ${index}`,
        "该记录由前端本地开发模式批量生成，用于测试首页公开推荐文章一键导入流程。"
      );
      submittedCount++;
      items.push({
        articleId: video.id,
        sourceId,
        sourceUrl,
        title: video.title,
        status: "SUBMITTED",
        message: "已提交导入任务",
      });
    }

    writeDatabase(db);
    return {
      mode: "HOME_RECOMMENDATIONS",
      target: "https://blog.csdn.net/",
      discoveredCount: limit,
      submittedCount,
      duplicateCount,
      failedCount: 0,
      items,
    };
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
      throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "文章不存在或已被删除。");
    }
    return video;
  },

  async rebuildVideo(id) {
    await delay();
    const db = readDatabase();
    const video = db.videos.find((item) => item.id === Number(id));
    if (!video) {
      throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "文章不存在或已被删除。");
    }

    video.status = "IMPORTING";
    video.failReason = null;
    video.importTime = nowString();
    video.chunkCount = 32 + Math.floor(Math.random() * 24);
    writeDatabase(db);

    await delay(320);
    video.status = "SUCCESS";
    video.description = "该记录已通过开发模式重建索引，模拟了重新抓取文章正文、重新切分与重新写入向量库。";
    writeDatabase(db);

    return { ...video };
  },

  async removeVideo(id) {
    await delay();
    const db = readDatabase();
    const targetId = Number(id);
    const exists = db.videos.some((item) => item.id === targetId);
    if (!exists) {
      throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "文章不存在或已被删除。");
    }

    db.videos = db.videos.filter((item) => item.id !== targetId);
    const removedSessionIds = db.sessions.filter((item) => item.articleId === targetId).map((item) => item.id);
    db.sessions = db.sessions.filter((item) => item.articleId !== targetId);
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
    const sessionType =
      payload.sessionType === "SINGLE_VIDEO"
        ? "SINGLE_ARTICLE"
        : payload.sessionType === "ALL_VIDEOS"
          ? "ALL_ARTICLES"
          : payload.sessionType;
    const articleIdInput = payload.articleId ?? payload.videoId ?? null;

    if (sessionType !== "SINGLE_ARTICLE" && sessionType !== "ALL_ARTICLES") {
      throw createError(ERROR_CODES.SESSION_TYPE_ERROR, "会话类型不合法，请重新创建会话。");
    }

    let articleTitle = null;
    let articleId = null;

    if (sessionType === "SINGLE_ARTICLE") {
      if (!articleIdInput) {
        throw createError(ERROR_CODES.PARAM_ERROR, "单文章对话必须选择目标文章。");
      }
      const article = db.videos.find((item) => item.id === Number(articleIdInput));
      if (!article) {
        throw createError(ERROR_CODES.VIDEO_NOT_FOUND, "目标文章不存在。");
      }
      articleId = article.id;
      articleTitle = article.title;
    }

    const session = {
      id: db.nextSessionId++,
      sessionType,
      articleId,
      articleTitle,
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
