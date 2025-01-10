<script setup lang="ts">
import 'element-plus/dist/index.css'
// import "bootstrap/dist/css/bootstrap.min.css"
// import "bootstrap/dist/js/bootstrap"

</script>

<template>
	<router-view/>
</template>

<style scoped>
/* 禁用滚动条 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden; /* 禁止滚动 */
}

/* 保持其他样式 */
.logo {
    height: 6em;
    padding: 1.5em;
    will-change: filter;
    transition: filter 300ms;
}

.logo:hover {
    filter: drop-shadow(0 0 2em #646cffaa);
}

.logo.vue:hover {
    filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
