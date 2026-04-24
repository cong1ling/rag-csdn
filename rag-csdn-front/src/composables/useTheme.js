import { ref, watch } from "vue";

const THEME_KEY = "rag-csdn-theme";
const LEGACY_THEME_KEY = "rag-bilibili-theme";

function readStoredTheme() {
  const currentValue = localStorage.getItem(THEME_KEY);
  if (currentValue) {
    return currentValue;
  }

  const legacyValue = localStorage.getItem(LEGACY_THEME_KEY);
  if (legacyValue) {
    localStorage.setItem(THEME_KEY, legacyValue);
    localStorage.removeItem(LEGACY_THEME_KEY);
    return legacyValue;
  }

  return "dark";
}

const theme = ref(readStoredTheme());

export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === "dark" ? "light" : "dark";
  };

  const setTheme = (newTheme) => {
    theme.value = newTheme;
  };

  watch(
    theme,
    (newTheme) => {
      document.documentElement.setAttribute("data-theme", newTheme);
      localStorage.setItem(THEME_KEY, newTheme);
      localStorage.removeItem(LEGACY_THEME_KEY);
    },
    { immediate: true }
  );

  return {
    theme,
    toggleTheme,
    setTheme,
    isDark: () => theme.value === "dark",
  };
}
