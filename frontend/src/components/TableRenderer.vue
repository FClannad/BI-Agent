<template>
  <div v-if="table" class="table">
    <el-table :data="table.rows" stripe size="small" height="320">
      <el-table-column
        v-for="col in table.columns"
        :key="col.name"
        :prop="col.name"
        :label="col.name"
        show-overflow-tooltip
      />
    </el-table>
  </div>
  <div v-else class="empty">No table</div>
</template>

<script setup lang="ts">
import type { TableResult } from "../types";

defineProps<{ table: TableResult | null | undefined }>();
</script>

<style scoped>
.empty {
  font-size: 12px;
  color: #888;
}
</style>

