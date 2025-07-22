// stores/historyStore.ts
import { defineStore } from "pinia";
import { ref, computed } from "vue";

// interface HistoryItem {
//   id: string;
//   title: string;
//   content: string;
//   timestamp: number;
// }

export const useHistoryStore = defineStore("history", () => {
  // 状态
  const histories = ref<any[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const editDialogVisible = ref(false);
  const currentEditItem = ref<any | null>(null);

  // Getter
  const sortedHistories = computed(() => {
    return [...histories.value].sort((a, b) => b.timestamp - a.timestamp);
  });

  // Actions
  const fetchHistories = async () => {
    try {
      isLoading.value = true;
      // 模拟 API 调用
      const response = await mockApiGetHistories();
      histories.value = response;
    } catch (err) {
      error.value = "Failed to fetch histories";
    } finally {
      isLoading.value = false;
    }
  };

  const deleteHistory = async (id: string) => {
    try {
      await mockApiDeleteHistory(id);
      histories.value = histories.value.filter((item) => item.id !== id);
    } catch (err) {
      error.value = "Failed to delete history";
    }
  };

  const updateHistory = async (updatedItem: any) => {
    try {
      await mockApiUpdateHistory(updatedItem);
      const index = histories.value.findIndex(
        (item) => item.id === updatedItem.id
      );
      if (index !== -1) {
        histories.value.splice(index, 1, updatedItem);
      }
    } catch (err) {
      error.value = "Failed to update history";
    }
  };

  const openEditDialog = (item: any) => {
    currentEditItem.value = { ...item };
    editDialogVisible.value = true;
  };

  const closeEditDialog = () => {
    currentEditItem.value = null;
    editDialogVisible.value = false;
  };

  return {
    histories,
    sortedHistories,
    isLoading,
    error,
    editDialogVisible,
    currentEditItem,
    fetchHistories,
    deleteHistory,
    updateHistory,
    openEditDialog,
    closeEditDialog,
  };
});

// 模拟 API 方法
async function mockApiGetHistories(): Promise<any[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { id: "1", title: "记录1", content: "内容1", timestamp: Date.now() },
        {
          id: "2",
          title: "记录2",
          content: "内容2",
          timestamp: Date.now() - 1000,
        },
      ]);
    }, 500);
  });
}

async function mockApiDeleteHistory(id: string): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 200));
}

async function mockApiUpdateHistory(item: any): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 200));
}
