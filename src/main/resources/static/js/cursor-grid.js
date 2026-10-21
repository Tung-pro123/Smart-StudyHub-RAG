/**
 * CursorGrid - Vanilla WebGL2 Component
 * Adapted from React Bits for Smart StudyHub RAG
 * Highly performant, reactive cursor grid with mouse tracking & shockwave pulse
 */
(function (global) {
  'use strict';

  const INTRO_SECONDS = 1.4;
  const STYLES = { lines: 0, dashed: 1, dots: 2, crosses: 3 };

  const FALLOFF_CURVES = {
    linear: function (t) { return t; },
    smooth: function (t) { return t * t * (3 - 2 * t); },
    sharp: function (t) { return t * t * t; }
  };

  const VERTEX = `#version 300 es
void main() {
  vec2 p = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
  gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
}`;

  const FRAGMENT = `#version 300 es
precision highp float;
precision highp int;

uniform vec2 uResolution;
uniform vec2 uOffset;
uniform float uCellSize;
uniform sampler2D uState;
uniform ivec2 uCells;
uniform vec3 uColor;
uniform vec3 uGridColor;
uniform float uLineWidth;
uniform float uMaxOpacity;
uniform float uFillOpacity;
uniform float uGridOpacity;
uniform float uCellRadius;
uniform float uCellGap;
uniform int uGridStyle;
uniform vec2 uMouse;
uniform float uRadius;
uniform float uSpotlight;
uniform float uPresence;
uniform float uEdgeFade;
uniform float uReveal;

out vec4 outColor;

float level(vec2 cell) {
  if (cell.x < 0.0 || cell.y < 0.0 || cell.x >= float(uCells.x) || cell.y >= float(uCells.y)) return 0.0;
  float v = texelFetch(uState, ivec2(cell), 0).r;
  return v * v * (3.0 - 2.0 * v);
}

float line(float d, float width) {
  float w = max(width, 1.0);
  return clamp(w * 0.5 + 0.5 - d, 0.0, 1.0) * (width / w);
}

float roundBox(vec2 p, vec2 b, float r) {
  vec2 q = abs(p) - b + r;
  return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
  vec2 pos = vec2(gl_FragCoord.x, uResolution.y - gl_FragCoord.y);
  vec2 g = (pos - uOffset) / uCellSize;
  vec2 cell = floor(g);
  vec2 f = g - cell;
  vec2 edge = min(f, 1.0 - f) * uCellSize;

  float vx = line(edge.x, uLineWidth);
  float vy = line(edge.y, uLineWidth);
  if (uGridStyle == 1) {
    float dash = uCellSize / 6.0;
    vx *= step(0.5, fract((pos.y - uOffset.y) / dash + 0.25));
    vy *= step(0.5, fract((pos.x - uOffset.x) / dash + 0.25));
  } else if (uGridStyle == 2) {
    float r = max(uLineWidth * 1.25, 1.2);
    float d = length(edge);
    vx = clamp(r + 0.5 - d, 0.0, 1.0);
    vy = 0.0;
  } else if (uGridStyle == 3) {
    float arm = uCellSize * 0.1 + uLineWidth;
    vx *= step(edge.y, arm);
    vy *= step(edge.x, arm);
  }
  float mark = max(vx, vy);

  vec2 toMouse = pos - uMouse;
  float spot = exp(-dot(toMouse, toMouse) / max(uRadius * uRadius, 1.0) * 1.6) * uSpotlight * uPresence;

  vec2 center = uResolution * 0.5;
  float reach = length(center);
  float shown = smoothstep(uReveal * (reach + uCellSize * 2.0), uReveal * (reach + uCellSize * 2.0) - uCellSize * 2.0, length(pos - center));
  shown = uReveal >= 1.0 ? 1.0 : shown;

  float fade = 1.0;
  if (uEdgeFade > 0.0) {
    vec2 norm = pos / uResolution;
    vec2 a = clamp(norm / uEdgeFade, 0.0, 1.0);
    vec2 b = clamp((1.0 - norm) / uEdgeFade, 0.0, 1.0);
    vec2 sa = a * a * a * (a * (a * 6.0 - 15.0) + 10.0);
    vec2 sb = b * b * b * (b * (b * 6.0 - 15.0) + 10.0);
    fade = sa.x * sa.y * sb.x * sb.y;
  }

  float own = level(cell);
  float gridAlpha = mark * min(uGridOpacity + spot * 0.45, 1.0) * fade * shown;
  float litAlpha = 0.0;
  float fillAlpha = 0.0;

  if (uCellGap <= 0.0 && uCellRadius <= 0.0) {
    vec2 side = step(0.5, f) * 2.0 - 1.0;
    float nx = level(cell + vec2(side.x, 0.0));
    float ny = level(cell + vec2(0.0, side.y));
    float nc = level(cell + side);
    float ex = max(own, nx);
    float ey = max(own, ny);
    float joint = max(max(ex, ey), nc);
    float lit;
    if (uGridStyle == 2) lit = vx * joint;
    else if (uGridStyle == 3) lit = max(vx * (edge.y < uLineWidth ? joint : ex), vy * (edge.x < uLineWidth ? joint : ey));
    else lit = max(vx * (edge.y < uLineWidth ? joint : ex), vy * (edge.x < uLineWidth ? joint : ey));
    litAlpha = lit * uMaxOpacity;
    fillAlpha = own * uFillOpacity;
  } else if (own > 0.0) {
    vec2 local = (f - 0.5) * uCellSize;
    float halfSize = uCellSize * 0.5 - max(uCellGap, 0.0);
    float radius = min(uCellRadius, halfSize);
    float sd = roundBox(local, vec2(halfSize), radius);
    float inside = clamp(0.5 - sd, 0.0, 1.0);
    float border = line(abs(sd + uLineWidth * 0.5), uLineWidth) * inside;
    litAlpha = border * own * uMaxOpacity;
    fillAlpha = inside * own * uFillOpacity;
  }
  litAlpha *= fade;
  fillAlpha *= fade;

  float cellAlpha = clamp(max(litAlpha, fillAlpha), 0.0, 1.0);
  float alpha = cellAlpha + gridAlpha * (1.0 - cellAlpha);
  vec3 premul = uColor * cellAlpha + uGridColor * gridAlpha * (1.0 - cellAlpha);
  outColor = vec4(premul, alpha);
}`;

  function clamp(value, min, max) {
    return Math.min(max, Math.max(min, value));
  }

  function compile(gl, type, source) {
    const shader = gl.createShader(type);
    if (!shader) return null;
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (gl.getShaderParameter(shader, gl.COMPILE_STATUS)) return shader;
    gl.deleteShader(shader);
    return null;
  }

  function link(gl) {
    const vertex = compile(gl, gl.VERTEX_SHADER, VERTEX);
    const fragment = compile(gl, gl.FRAGMENT_SHADER, FRAGMENT);
    const program = gl.createProgram();
    if (!vertex || !fragment || !program) return null;
    gl.attachShader(program, vertex);
    gl.attachShader(program, fragment);
    gl.linkProgram(program);
    gl.deleteShader(vertex);
    gl.deleteShader(fragment);
    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      gl.deleteProgram(program);
      return null;
    }
    const uniforms = {};
    const count = gl.getProgramParameter(program, gl.ACTIVE_UNIFORMS);
    for (let i = 0; i < count; i++) {
      const info = gl.getActiveUniform(program, i);
      if (info) uniforms[info.name] = gl.getUniformLocation(program, info.name);
    }
    return { program: program, uniforms: uniforms };
  }

  const colorCache = new Map();

  function parseHex(hex) {
    let h = String(hex || '#ffffff').replace('#', '').trim();
    if (h.length === 3 || h.length === 4) {
      h = h.slice(0, 3).split('').map(function (c) { return c + c; }).join('');
    }
    const num = parseInt(h.slice(0, 6), 16);
    if (Number.isNaN(num)) return [1, 1, 1];
    return [((num >> 16) & 255) / 255, ((num >> 8) & 255) / 255, (num & 255) / 255];
  }

  function hexToRgb(hex) {
    const key = String(hex);
    const cached = colorCache.get(key);
    if (cached) return cached;
    const rgb = parseHex(hex);
    if (colorCache.size > 64) colorCache.clear();
    colorCache.set(key, rgb);
    return rgb;
  }

  function initCursorGrid(containerSelector, options) {
    const container = typeof containerSelector === 'string'
      ? document.querySelector(containerSelector)
      : containerSelector;

    if (!container) return null;

    let canvas = container.querySelector('canvas');
    if (!canvas) {
      canvas = document.createElement('canvas');
      canvas.className = 'cursor-grid__canvas';
      container.appendChild(canvas);
    }

    const opts = options || {};
    const ink = opts.lightMode ? '#18181b' : '#ffffff';

    const settings = {
      cellSize: Math.max(8, opts.cellSize || 55),
      color: opts.color || '#a855f7',
      gridColor: opts.gridColor || '#6366f1',
      radius: Math.max(1, opts.radius || 135),
      falloff: opts.falloff || 'smooth',
      highlight: opts.highlight || 'area',
      holdTime: Math.max(0, opts.holdTime !== undefined ? opts.holdTime : 300),
      fadeDuration: Math.max(16, opts.fadeDuration || 700),
      lineWidth: Math.max(0.25, opts.lineWidth || 1),
      maxOpacity: clamp(opts.maxOpacity !== undefined ? opts.maxOpacity : 0.35, 0, 1),
      fillOpacity: clamp(opts.fillOpacity !== undefined ? opts.fillOpacity : 0.02, 0, 1),
      gridOpacity: clamp(opts.gridOpacity !== undefined ? opts.gridOpacity : 0.03, 0, 1),
      gridStyle: STYLES[opts.gridStyle] !== undefined ? STYLES[opts.gridStyle] : 0,
      cellRadius: Math.max(0, opts.cellRadius || 0),
      cellGap: Math.max(0, opts.cellGap || 0),
      spotlight: clamp(opts.spotlight !== undefined ? opts.spotlight : 0.35, 0, 1),
      clickPulse: opts.clickPulse !== undefined ? opts.clickPulse : true,
      pulseSpeed: Math.max(1, opts.pulseSpeed || 550),
      ambient: clamp(opts.ambient || 0, 0, 1),
      edgeFade: clamp(opts.edgeFade !== undefined ? opts.edgeFade : 0.15, 0, 0.5),
      intro: opts.intro !== undefined ? opts.intro : false,
      dpr: opts.dpr || Math.min(window.devicePixelRatio || 1, 1.5)
    };

    const gl = canvas.getContext('webgl2', {
      alpha: true,
      premultipliedAlpha: true,
      antialias: false,
      depth: false,
      stencil: false
    });

    if (!gl) {
      console.warn('WebGL2 not supported, CursorGrid disabled.');
      return null;
    }

    const shader = link(gl);
    if (!shader) return null;

    const u = shader.uniforms;
    const vao = gl.createVertexArray();
    const stateTexture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, stateTexture);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
    gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
    gl.pixelStorei(gl.UNPACK_ALIGNMENT, 1);

    const reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const view = { width: 1, height: 1, ratio: 1, cellSize: 0 };
    const grid = { cols: 0, rows: 0, offX: 0, offY: 0 };
    const pointer = { x: 0, y: 0, inside: false };
    const mouse = { x: 0, y: 0 };
    const pulses = [];
    let levels = new Float32Array(0);
    let waves = new Float32Array(0);
    let targets = new Float32Array(0);
    let touched = new Float32Array(0);
    let pixels = new Uint8Array(0);
    let presence = 0;
    let clock = 0;
    let introClock = settings.intro && !reduce ? 0 : 1;
    let raf = 0;
    let last = 0;
    let isEnabled = opts.visible !== undefined ? !!opts.visible : true;
    let visible = true;
    let dirty = true;

    function rebuild() {
      view.width = Math.max(1, container.clientWidth || window.innerWidth);
      view.height = Math.max(1, container.clientHeight || window.innerHeight);
      view.ratio = Math.min(settings.dpr, 2);
      canvas.width = Math.max(1, Math.round(view.width * view.ratio));
      canvas.height = Math.max(1, Math.round(view.height * view.ratio));
      const size = settings.cellSize;
      const cols = Math.ceil(view.width / size) + 1;
      const rows = Math.ceil(view.height / size) + 1;
      const resized = cols !== grid.cols || rows !== grid.rows || size !== view.cellSize;
      grid.cols = cols;
      grid.rows = rows;
      grid.offX = (view.width - cols * size) / 2;
      grid.offY = (view.height - rows * size) / 2;
      view.cellSize = size;
      if (resized) {
        levels = new Float32Array(cols * rows);
        waves = new Float32Array(cols * rows);
        targets = new Float32Array(cols * rows);
        touched = new Float32Array(cols * rows).fill(-1e9);
        pixels = new Uint8Array(cols * rows);
        gl.bindTexture(gl.TEXTURE_2D, stateTexture);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.R8, cols, rows, 0, gl.RED, gl.UNSIGNED_BYTE, pixels);
      }
      dirty = true;
    }

    function light(index, value) {
      if (index >= 0 && index < targets.length) {
        if (value > targets[index]) targets[index] = value;
        touched[index] = clock;
      }
    }

    function energize(x, y) {
      const size = settings.cellSize;
      if (settings.highlight === 'cell') {
        const col = Math.floor((x - grid.offX) / size);
        const row = Math.floor((y - grid.offY) / size);
        if (col >= 0 && row >= 0 && col < grid.cols && row < grid.rows) {
          light(row * grid.cols + col, 1);
        }
        return;
      }
      const r = settings.radius;
      const ease = FALLOFF_CURVES[settings.falloff] || FALLOFF_CURVES.linear;
      const minCol = Math.max(0, Math.floor((x - r - grid.offX) / size));
      const maxCol = Math.min(grid.cols - 1, Math.floor((x + r - grid.offX) / size));
      const minRow = Math.max(0, Math.floor((y - r - grid.offY) / size));
      const maxRow = Math.min(grid.rows - 1, Math.floor((y + r - grid.offY) / size));
      for (let row = minRow; row <= maxRow; row++) {
        for (let col = minCol; col <= maxCol; col++) {
          const cx = grid.offX + col * size + size / 2;
          const cy = grid.offY + row * size + size / 2;
          const dist = Math.hypot(cx - x, cy - y);
          if (dist > r) continue;
          const value = ease(1 - dist / r);
          if (value > 0) light(row * grid.cols + col, value);
        }
      }
    }

    function sweepPulses() {
      const size = settings.cellSize;
      const ahead = size * 0.6;
      const behind = size * 1.4;
      const far = Math.hypot(view.width, view.height);
      waves.fill(0);
      for (let k = pulses.length - 1; k >= 0; k--) {
        const pulse = pulses[k];
        const ringR = (clock - pulse.t0) * settings.pulseSpeed;
        if (ringR - behind * 2 > far) {
          pulses.splice(k, 1);
          continue;
        }
        if (ringR < 0) continue;
        const envelope = pulse.strength * Math.pow(Math.max(0, 1 - ringR / (far + behind)), 0.6);
        const outer = ringR + ahead * 2;
        const minCol = Math.max(0, Math.floor((pulse.x - outer - grid.offX) / size));
        const maxCol = Math.min(grid.cols - 1, Math.floor((pulse.x + outer - grid.offX) / size));
        const minRow = Math.max(0, Math.floor((pulse.y - outer - grid.offY) / size));
        const maxRow = Math.min(grid.rows - 1, Math.floor((pulse.y + outer - grid.offY) / size));
        for (let row = minRow; row <= maxRow; row++) {
          for (let col = minCol; col <= maxCol; col++) {
            const cx = grid.offX + col * size + size / 2;
            const cy = grid.offY + row * size + size / 2;
            const off = Math.hypot(cx - pulse.x, cy - pulse.y) - ringR;
            if (off > ahead * 2 || off < -behind * 2) continue;
            const t = off > 0 ? off / ahead : off / behind;
            const value = envelope * Math.exp(-t * t);
            const index = row * grid.cols + col;
            if (value > waves[index]) waves[index] = value;
          }
        }
      }
      dirty = true;
    }

    function draw() {
      const ratio = view.ratio;
      gl.viewport(0, 0, canvas.width, canvas.height);
      gl.clearColor(0, 0, 0, 0);
      gl.clear(gl.COLOR_BUFFER_BIT);
      gl.useProgram(shader.program);
      gl.bindVertexArray(vao);
      gl.activeTexture(gl.TEXTURE0);
      gl.bindTexture(gl.TEXTURE_2D, stateTexture);
      if (dirty) {
        for (let i = 0; i < levels.length; i++) {
          pixels[i] = Math.round(clamp(Math.max(levels[i], waves[i]), 0, 1) * 255);
        }
        gl.texSubImage2D(gl.TEXTURE_2D, 0, 0, 0, grid.cols, grid.rows, gl.RED, gl.UNSIGNED_BYTE, pixels);
        dirty = false;
      }
      const lineDevice = settings.lineWidth * ratio;
      const snap = Math.round(lineDevice) % 2 === 1 ? 0.5 : 0;
      gl.uniform1i(u.uState, 0);
      gl.uniform2f(u.uResolution, canvas.width, canvas.height);
      gl.uniform2f(u.uOffset, Math.round(grid.offX * ratio) + snap, Math.round(grid.offY * ratio) + snap);
      gl.uniform1f(u.uCellSize, settings.cellSize * ratio);
      gl.uniform2i(u.uCells, grid.cols, grid.rows);
      gl.uniform3fv(u.uColor, hexToRgb(settings.color));
      gl.uniform3fv(u.uGridColor, hexToRgb(settings.gridColor));
      gl.uniform1f(u.uLineWidth, lineDevice);
      gl.uniform1f(u.uMaxOpacity, settings.maxOpacity);
      gl.uniform1f(u.uFillOpacity, settings.fillOpacity);
      gl.uniform1f(u.uGridOpacity, settings.gridOpacity);
      gl.uniform1f(u.uCellRadius, settings.cellRadius * ratio);
      gl.uniform1f(u.uCellGap, settings.cellGap * ratio);
      gl.uniform1i(u.uGridStyle, settings.gridStyle);
      gl.uniform2f(u.uMouse, mouse.x * ratio, mouse.y * ratio);
      gl.uniform1f(u.uRadius, settings.radius * ratio);
      gl.uniform1f(u.uSpotlight, settings.spotlight);
      gl.uniform1f(u.uPresence, presence);
      gl.uniform1f(u.uEdgeFade, settings.edgeFade);
      gl.uniform1f(u.uReveal, introClock >= 1 ? 1 : 1 - Math.pow(1 - introClock, 2.2));
      gl.drawArrays(gl.TRIANGLES, 0, 3);
      gl.bindVertexArray(null);
    }

    function frame(now) {
      raf = 0;
      if (!visible || document.hidden) return;
      const dt = last ? clamp((now - last) / 1000, 0, 1 / 20) : 1 / 60;
      last = now;
      clock += dt;
      if (introClock < 1) introClock = Math.min(1, introClock + dt / INTRO_SECONDS);

      const follow = 1 - Math.exp(-dt / 0.07);
      mouse.x += (pointer.x - mouse.x) * follow;
      mouse.y += (pointer.y - mouse.y) * follow;
      const presenceGoal = pointer.inside ? 1 : 0;
      presence += (presenceGoal - presence) * (1 - Math.exp(-dt / 0.25));

      if (settings.ambient > 0 && !reduce && levels.length) {
        const chance = settings.ambient * dt * levels.length * 0.04;
        let spawns = Math.floor(chance) + (Math.random() < chance % 1 ? 1 : 0);
        while (spawns-- > 0) light(Math.floor(Math.random() * levels.length), 0.25 + Math.random() * 0.45);
      }
      if (pulses.length) sweepPulses();
      else if (waves.length && waves.some(function (value) { return value > 0; })) {
        waves.fill(0);
        dirty = true;
      }

      const holdSeconds = settings.holdTime / 1000;
      const fadeStep = dt / (settings.fadeDuration / 1000);
      const attack = 1 - Math.exp(-dt / 0.045);
      let active = pulses.length > 0 || introClock < 1 || Math.abs(presence - presenceGoal) > 0.002;
      for (let i = 0; i < levels.length; i++) {
        let target = targets[i];
        if (target <= 0 && levels[i] <= 0) continue;
        if (clock - touched[i] > holdSeconds) target = Math.max(0, target - fadeStep);
        targets[i] = target;
        const before = levels[i];
        const next = target > before ? before + (target - before) * attack : target;
        levels[i] = next < 0.002 ? 0 : next;
        if (levels[i] !== before) dirty = true;
        if (levels[i] > 0 || targets[i] > 0) active = true;
      }
      if (Math.abs(mouse.x - pointer.x) + Math.abs(mouse.y - pointer.y) > 0.05 && presence > 0.002) active = true;

      draw();
      if (active) raf = requestAnimationFrame(frame);
    }

    function wake() {
      if (!isEnabled || !visible || document.hidden) return;
      if (!raf) {
        last = 0;
        raf = requestAnimationFrame(frame);
      }
    }

    function locate(event) {
      const rect = container.getBoundingClientRect();
      const x = event.clientX - rect.left;
      const y = event.clientY - rect.top;
      return { x: x, y: y, inside: x >= 0 && y >= 0 && x <= rect.width && y <= rect.height };
    }

    function onPointerMove(event) {
      if (!isEnabled || !visible) return;
      const loc = locate(event);
      const was = pointer.inside;
      pointer.inside = loc.inside;
      if (!loc.inside) {
        if (was) wake();
        return;
      }
      pointer.x = loc.x;
      pointer.y = loc.y;
      if (!was) {
        mouse.x = loc.x;
        mouse.y = loc.y;
      }
      energize(loc.x, loc.y);
      wake();
    }

    function onPointerDown(event) {
      if (!isEnabled || !visible) return;
      const loc = locate(event);
      if (!loc.inside || !settings.clickPulse || reduce) return;
      pulses.push({ x: loc.x, y: loc.y, t0: clock, strength: 0.8 });
      wake();
    }

    function onPointerLeave() {
      pointer.inside = false;
      wake();
    }

    function onVisibility() {
      wake();
    }

    const resizeObserver = new ResizeObserver(function () {
      rebuild();
      draw();
      wake();
    });
    resizeObserver.observe(container);

    const intersection = new IntersectionObserver(function (entries) {
      visible = entries.some(function (entry) { return entry.isIntersecting; });
      if (visible && isEnabled) wake();
    });
    intersection.observe(container);

    window.addEventListener('pointermove', onPointerMove, { passive: true });
    window.addEventListener('pointerdown', onPointerDown, { passive: true });
    document.documentElement.addEventListener('pointerleave', onPointerLeave);
    document.addEventListener('visibilitychange', onVisibility);

    rebuild();
    draw();
    if (isEnabled) wake();

    return {
      destroy: function () {
        cancelAnimationFrame(raf);
        resizeObserver.disconnect();
        intersection.disconnect();
        window.removeEventListener('pointermove', onPointerMove);
        window.removeEventListener('pointerdown', onPointerDown);
        document.documentElement.removeEventListener('pointerleave', onPointerLeave);
        document.removeEventListener('visibilitychange', onVisibility);
        gl.deleteTexture(stateTexture);
        gl.deleteVertexArray(vao);
        gl.deleteProgram(shader.program);
        gl.getExtension('WEBGL_lose_context')?.loseContext();
      },
      setVisible: function (isVis) {
        const next = !!isVis;
        if (isEnabled === next) return;
        isEnabled = next;
        if (!isEnabled) {
          if (raf) {
            cancelAnimationFrame(raf);
            raf = 0;
          }
        } else {
          wake();
        }
      }
    };
  }

  global.initCursorGrid = initCursorGrid;
})(typeof window !== 'undefined' ? window : this);
