<template>
  <div class="background-3d" aria-hidden="true">
    <!-- Layer 0: Deep Background & Nebula Glows -->
    <div class="nebula-glow">
      <div class="glow g1"></div>
      <div class="glow g2"></div>
      <div class="glow g3"></div>
    </div>

    <!-- Layer 1: Data Stream (Scrolling Hex) -->
    <div class="data-stream-layer">
      <div
        v-for="i in 8"
        :key="'col-'+i"
        class="data-column"
        :style="{ left: (i * 12) + '%', animationDelay: (i * -2) + 's' }"
      >
        <span v-for="j in 20" :key="'char-'+j">{{ generateHex() }}</span>
      </div>
    </div>

    <!-- Layer 2: Grid System -->
    <div class="grid-overlay"></div>

    <!-- Layer 3: Ornaments -->
    <div class="ornaments">
      <!-- Neural Node Cluster (Top Left) -->
      <div class="geo geo-nodes-wrapper">
        <div class="nodes-core">
          <div
            v-for="i in 6"
            :key="'node-'+i"
            class="node"
            :style="{ transform: `rotateY(${i * 60}deg) rotateX(${i * 45}deg) translateZ(80px)` }"
          ></div>
          <div class="node-lines"></div>
        </div>
      </div>

      <!-- Sphere with Earth Map (Top Right) -->
      <div class="geo geo-sphere-wrapper">
        <div class="sphere-bg">
          <div class="sphere-continents"></div>
          <div class="sphere-atmosphere"></div>
        </div>
        <div class="sphere-lines">
          <span class="lat l1"></span><span class="lat l2"></span><span class="lat l3"></span>
          <span class="lat l4"></span><span class="lat l5"></span><span class="lat l6"></span>
          <span class="lat l7"></span>
          <span class="long n1"></span><span class="long n2"></span><span class="long n3"></span>
          <span class="long n4"></span><span class="long n5"></span><span class="long n6"></span>
          <span class="long n7"></span><span class="long n8"></span><span class="long n9"></span>
          <span class="long n10"></span><span class="long n11"></span><span class="long n12"></span>
        </div>
      </div>
      
      <!-- 3D Data Ring / Torus (Left Center) -->
      <div class="geo geo-ring-wrapper">
        <div class="ring-3d">
          <div
            v-for="i in 3"
            :key="'ring-'+i"
            class="ring-layer"
            :style="{ transform: `rotateX(${i * 45}deg) rotateY(${i * 30}deg)` }"
          ></div>
        </div>
      </div>

      <!-- Cube (Bottom Left) -->
      <div class="geo geo-cube-wrapper">
        <div class="cube-3d">
          <span class="face front"></span><span class="face back"></span>
          <span class="face right"></span><span class="face left"></span>
          <span class="face top"></span><span class="face bottom"></span>
        </div>
      </div>

      <!-- DNA Double Helix (Bottom Right) -->
      <div class="geo geo-dna-wrapper">
        <div class="dna-3d">
          <div
            v-for="i in 12"
            :key="i"
            class="dna-unit"
            :style="{ top: (i * 20) + 'px', transform: `rotateY(${i * 30}deg)` }"
          >
            <span class="dot dot1"></span>
            <span class="bar"></span>
            <span class="dot dot2"></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
function generateHex() {
  const chars = "0123456789ABCDEF";
  return chars[Math.floor(Math.random() * chars.length)] + chars[Math.floor(Math.random() * chars.length)];
}
</script>

<style scoped>
.background-3d {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
  background: var(--rb-bg);
  isolation: isolate;
}

/* Nebula Glows */
.nebula-glow {
  position: absolute;
  inset: 0;
  z-index: 0;
}
.glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.15;
}
.g1 { width: 600px; height: 600px; top: -100px; left: -100px; background: radial-gradient(circle, var(--rb-accent), transparent); }
.g2 { width: 500px; height: 500px; bottom: -50px; right: -50px; background: radial-gradient(circle, #00d2ff, transparent); }
.g3 { width: 400px; height: 400px; top: 40%; left: 30%; background: radial-gradient(circle, #0052d9, transparent); opacity: 0.08; }

/* Data Streams */
.data-stream-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
  opacity: 0.04;
  font-family: var(--font-mono), monospace;
  font-size: 12px;
  overflow: hidden;
}
.data-column {
  position: absolute;
  top: -100px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  animation: stream-down 15s linear infinite;
}
@keyframes stream-down {
  from { transform: translateY(0); }
  to { transform: translateY(100vh); }
}

/* Grid System */
.grid-overlay {
  position: absolute;
  inset: 0;
  z-index: 2;
  background-image:
    linear-gradient(var(--rb-pattern-grid) 1px, transparent 1px),
    linear-gradient(90deg, var(--rb-pattern-grid) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(circle at 50% 40%, black 10%, rgba(0, 0, 0, 0.3) 60%, transparent 100%);
}

.ornaments {
  position: absolute;
  inset: 0;
  z-index: 3;
}

.geo {
  position: absolute;
  opacity: 0.85;
  filter: drop-shadow(0 20px 40px rgba(0, 0, 0, 0.2));
  transform-origin: center;
}

/* Node Cluster */
.geo-nodes-wrapper {
  top: 15%;
  left: 12%;
  width: 200px;
  height: 200px;
}
.nodes-core {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  animation: rotate-3d 30s linear infinite;
}
.node {
  position: absolute;
  width: 8px;
  height: 8px;
  background: var(--rb-accent);
  border-radius: 50%;
  box-shadow: 0 0 15px var(--rb-accent);
  top: 50%; left: 50%;
}

/* Sphere (Earth) */
.geo-sphere-wrapper {
  top: 60px;
  right: 6%;
  width: 240px;
  height: 240px;
  transform-style: preserve-3d;
  animation: float-sphere 15s ease-in-out infinite;
}
.sphere-bg {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, var(--rb-earth-ocean-light) 0%, var(--rb-earth-ocean-dark) 100%);
  box-shadow: inset -10px -10px 40px rgba(0,0,0,0.6), inset 10px 10px 40px rgba(255,255,255,0.05);
  overflow: hidden;
}
.sphere-continents {
  position: absolute;
  inset: 0;
  background-image: url('https://www.transparenttextures.com/patterns/world-map.png');
  background-size: 200% 100%;
  opacity: var(--rb-earth-continents-opacity);
  filter: brightness(1.2);
  animation: continents-rotate 40s linear infinite;
}
.sphere-lines {
  position: absolute;
  inset: -1px;
  transform-style: preserve-3d;
  animation: rotate-earth 40s linear infinite;
}
.sphere-lines span {
  position: absolute;
  inset: 0;
  border: 1px solid var(--rb-earth-grid);
  border-radius: 50%;
  opacity: 1;
}
.lat.l1 { transform: rotateX(90deg); }
.lat.l2 { transform: rotateX(90deg) translateZ(35px) scale(0.95); }
.lat.l3 { transform: rotateX(90deg) translateZ(-35px) scale(0.95); }
.lat.l4 { transform: rotateX(90deg) translateZ(65px) scale(0.8); }
.lat.l5 { transform: rotateX(90deg) translateZ(-65px) scale(0.8); }
.lat.l6 { transform: rotateX(90deg) translateZ(90px) scale(0.5); }
.lat.l7 { transform: rotateX(90deg) translateZ(-90px) scale(0.5); }
.long.n1 { transform: rotateY(0deg); }
.long.n2 { transform: rotateY(30deg); }
.long.n3 { transform: rotateY(60deg); }
.long.n4 { transform: rotateY(90deg); }
.long.n5 { transform: rotateY(120deg); }
.long.n6 { transform: rotateY(150deg); }

/* Data Rings */
.geo-ring-wrapper {
  top: 45%;
  left: 5%;
  width: 180px;
  height: 180px;
  perspective: 1000px;
}
.ring-3d {
  width: 100%; height: 100%;
  transform-style: preserve-3d;
  animation: rotate-3d-alt 25s linear infinite;
}
.ring-layer {
  position: absolute;
  inset: 0;
  border: 2px solid var(--rb-accent);
  border-radius: 50%;
  opacity: 0.3;
  box-shadow: 0 0 20px var(--rb-accent-soft);
}

/* Cube */
.geo-cube-wrapper {
  bottom: 80px;
  left: 6%;
  width: 120px;
  height: 120px;
  perspective: 1000px;
}
.cube-3d {
  width: 100%; height: 100%;
  transform-style: preserve-3d;
  animation: rotate-3d 20s linear infinite;
}
.face {
  position: absolute;
  width: 120px;
  height: 120px;
  background: rgba(var(--rb-accent-rgb), 0.05);
  border: 1px solid var(--rb-accent);
  opacity: 0.8;
}
.front  { transform: translateZ(60px); }
.back   { transform: translateZ(-60px); }
.right  { transform: rotateY(90deg) translateZ(60px); }
.left   { transform: rotateY(-90deg) translateZ(60px); }
.top    { transform: rotateX(90deg) translateZ(60px); }
.bottom { transform: rotateX(-90deg) translateZ(60px); }

/* DNA */
.geo-dna-wrapper {
  bottom: 10%;
  right: 12%;
  width: 80px;
  height: 260px;
  perspective: 800px;
}
.dna-3d {
  width: 100%; height: 100%;
  transform-style: preserve-3d;
  animation: rotate-y 12s linear infinite;
}
.dna-unit {
  position: absolute;
  width: 100%;
  height: 18px;
  transform-style: preserve-3d;
  display: flex; align-items: center; justify-content: center;
}
.dna-unit .dot { width: 10px; height: 10px; border-radius: 50%; background: var(--rb-accent); box-shadow: 0 0 10px var(--rb-accent); }
.dna-unit .bar { width: 50px; height: 1px; background: var(--rb-accent); opacity: 0.4; }
.dna-unit .dot1 { transform: translateZ(35px); }
.dna-unit .dot2 { transform: translateZ(-35px); opacity: 0.6; }

/* Animations */
@keyframes rotate-3d {
  from { transform: rotateX(0) rotateY(0); }
  to { transform: rotateX(360deg) rotateY(360deg); }
}
@keyframes rotate-3d-alt {
  from { transform: rotateX(45deg) rotateY(0) rotateZ(0); }
  to { transform: rotateX(45deg) rotateY(360deg) rotateZ(360deg); }
}
@keyframes rotate-y {
  from { transform: rotateY(0); }
  to { transform: rotateY(360deg); }
}
@keyframes rotate-earth {
  from { transform: rotateX(20deg) rotateZ(23.5deg) rotateY(0); }
  to { transform: rotateX(20deg) rotateZ(23.5deg) rotateY(360deg); }
}
@keyframes continents-rotate {
  from { background-position-x: 0; }
  to { background-position-x: 200%; }
}
@keyframes float-sphere {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

@media (max-width: 768px) {
  .geo-nodes-wrapper, .geo-ring-wrapper { display: none; }
  .geo-sphere-wrapper { width: 160px; height: 160px; top: 20px; right: -30px; }
  .geo-cube-wrapper { width: 80px; height: 80px; bottom: 40px; }
}
</style>
