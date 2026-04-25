import { http } from "./http";
import { devServer } from "../mock/dev-server";
import { isDeveloperModeEnabled } from "../utils/dev-mode";

export const articlesApi = {
  importArticle(payload) {
    if (isDeveloperModeEnabled()) {
      return devServer.importVideo(payload);
    }
    return http.post("/articles", payload);
  },
  importAuthorArticles(payload) {
    if (isDeveloperModeEnabled()) {
      return devServer.importAuthorArticles(payload);
    }
    return http.post("/articles/batch/author", payload);
  },
  importRecommendedArticles(payload = {}) {
    if (isDeveloperModeEnabled()) {
      return devServer.importRecommendedArticles(payload);
    }
    return http.post("/articles/batch/recommendations", payload);
  },
  rebuild(id, payload = {}) {
    if (isDeveloperModeEnabled()) {
      return devServer.rebuildVideo(id, payload);
    }
    return http.post(`/articles/${id}/rebuild`, payload);
  },
  list() {
    if (isDeveloperModeEnabled()) {
      return devServer.listVideos();
    }
    return http.get("/articles");
  },
  detail(id) {
    if (isDeveloperModeEnabled()) {
      return devServer.getVideo(id);
    }
    return http.get(`/articles/${id}`);
  },
  remove(id) {
    if (isDeveloperModeEnabled()) {
      return devServer.removeVideo(id);
    }
    return http.delete(`/articles/${id}`);
  },
};
