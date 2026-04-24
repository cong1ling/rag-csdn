// Source switch for the hidden developer entry.
// true: show the developer entry and allow local developer mode
// false: completely hide and disable the developer entry
export const ENABLE_DEVELOPER_ENTRY = false;

export const DEV_ENTRY_USERNAME = "tsj060205";
export const DEV_ENTRY_PASSWORD = "tsj060205";
export const DEV_AUTH_STORAGE_KEY = "rag-csdn-dev-auth";
export const LEGACY_DEV_AUTH_STORAGE_KEY = "rag-bilibili-dev-auth";

export function getDevUserProfile() {
  return {
    id: 900000,
    username: DEV_ENTRY_USERNAME,
    createTime: "2026-03-10 20:00:00",
  };
}

export function isDeveloperEntryAvailable() {
  return ENABLE_DEVELOPER_ENTRY;
}

export function isDeveloperModeEnabled() {
  if (!ENABLE_DEVELOPER_ENTRY) {
    sessionStorage.removeItem(DEV_AUTH_STORAGE_KEY);
    sessionStorage.removeItem(LEGACY_DEV_AUTH_STORAGE_KEY);
    return false;
  }

  const currentValue = sessionStorage.getItem(DEV_AUTH_STORAGE_KEY);
  if (currentValue === "1") {
    return true;
  }

  const legacyValue = sessionStorage.getItem(LEGACY_DEV_AUTH_STORAGE_KEY);
  if (legacyValue === "1") {
    sessionStorage.setItem(DEV_AUTH_STORAGE_KEY, "1");
    sessionStorage.removeItem(LEGACY_DEV_AUTH_STORAGE_KEY);
    return true;
  }

  return false;
}

export function enableDeveloperMode() {
  if (!ENABLE_DEVELOPER_ENTRY) {
    return;
  }
  sessionStorage.setItem(DEV_AUTH_STORAGE_KEY, "1");
  sessionStorage.removeItem(LEGACY_DEV_AUTH_STORAGE_KEY);
}

export function disableDeveloperMode() {
  sessionStorage.removeItem(DEV_AUTH_STORAGE_KEY);
  sessionStorage.removeItem(LEGACY_DEV_AUTH_STORAGE_KEY);
}
