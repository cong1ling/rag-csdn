import { http } from "./http";
import {
  DEV_CSDN_SESSION_STORAGE_KEY,
  DEV_CSDN_SESSION_TIME_STORAGE_KEY,
  getDevUserProfile,
  isDeveloperModeEnabled,
} from "../utils/dev-mode";

function nowString() {
  const now = new Date();
  const pad = (value) => String(value).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(
    now.getMinutes()
  )}:${pad(now.getSeconds())}`;
}

export const authApi = {
  register(payload) {
    return http.post("/auth/register", payload);
  },
  login(payload) {
    return http.post("/auth/login", payload);
  },
  logout() {
    return http.post("/auth/logout");
  },
  current() {
    if (isDeveloperModeEnabled()) {
      return Promise.resolve(getDevUserProfile());
    }
    return http.get("/auth/current");
  },
  updateCsdnSession(payload) {
    if (isDeveloperModeEnabled()) {
      sessionStorage.setItem(DEV_CSDN_SESSION_STORAGE_KEY, "1");
      sessionStorage.setItem(DEV_CSDN_SESSION_TIME_STORAGE_KEY, nowString());
      return Promise.resolve(getDevUserProfile());
    }
    return http.put("/auth/csdn-session", payload);
  },
  clearCsdnSession() {
    if (isDeveloperModeEnabled()) {
      sessionStorage.removeItem(DEV_CSDN_SESSION_STORAGE_KEY);
      sessionStorage.removeItem(DEV_CSDN_SESSION_TIME_STORAGE_KEY);
      return Promise.resolve(getDevUserProfile());
    }
    return http.delete("/auth/csdn-session");
  },
};
