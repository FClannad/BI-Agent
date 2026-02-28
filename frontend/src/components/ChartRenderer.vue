<template>
  <div v-if="option" ref="el" class="chart"></div>
  <div v-else class="empty">No chart</div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import type { EChartsOption } from "echarts";

const props = defineProps<{ option: EChartsOption | null | undefined }>();
const el = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

function render() {
  if (!el.value) return;
  if (!chart) chart = echarts.init(el.value);
  if (props.option) chart.setOption(props.option, true);
}

onMounted(() => render());
watch(() => props.option, () => render());
onBeforeUnmount(() => {
  chart?.dispose();
  chart = null;
});
</script>

<style scoped>
.chart {
  height: 360px;
  width: 100%;
}
.empty {
  font-size: 12px;
  color: #888;
}
</style>

