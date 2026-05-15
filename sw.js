/* Ficha Eclipse — service worker
 * Estratégia dual:
 *   STATIC  (icons/manifest/favicon) → cache-first, raramente muda
 *   RUNTIME (HTML/JS/CSS/futuros)    → network-first com fallback cache
 * Navigation Preload acelera primeira navegação.
 * Runtime cache tem teto (LRU manual) pra não crescer sem limite.
 */
const VERSION = 'v3.8.74';
const STATIC_CACHE  = 'eclipse-static-' + VERSION;
const RUNTIME_CACHE = 'eclipse-runtime-' + VERSION;
const RUNTIME_MAX   = 60; // máx entries no runtime cache

// Assets que nunca mudam dentro de uma versão. Precache no install.
const STATIC_ASSETS = [
  './manifest.webmanifest',
  './icons/icon.svg',
  './icons/icon-maskable.svg',
  './icons/favicon.svg',
  './icons/logo-eclipse-moon.svg',
  './icons/logo-eclipse-star.svg',
  './icons/hand-plus.svg',
  './icons/hand-single.svg',
  './icons/equip/cabeca.svg',
  './icons/equip/rosto.svg',
  './icons/equip/torso.svg',
  './icons/equip/pernas.svg',
  './icons/equip/pes.svg',
  './icons/equip/conjunto.svg',
  './icons/mods/cano.svg',
  './icons/mods/bocal-1.svg',
  './icons/mods/mira-1.svg',
  './icons/mods/empunhadura.svg',
  './icons/mods/carregador.svg',
  './icons/mods/trilho-1.svg'
];

self.addEventListener('install', e => {
  // skipWaiting: ativa imediato sem esperar todas as abas fecharem. Boot 1º-instalação mais rápido.
  self.skipWaiting();
  e.waitUntil((async () => {
    const c = await caches.open(STATIC_CACHE);
    // add individual com catch — se um falhar, install não quebra
    await Promise.all(STATIC_ASSETS.map(a => c.add(a).catch(err => console.warn('[sw] precache fail', a, err && err.message))));
    // Precache index.html na runtime também pra ter fallback offline
    try { await (await caches.open(RUNTIME_CACHE)).add('./index.html'); } catch(_){}
  })());
});

self.addEventListener('activate', e => {
  e.waitUntil((async () => {
    // Habilita navigation preload (parallel fetch de navegações)
    if (self.registration.navigationPreload) {
      try { await self.registration.navigationPreload.enable(); } catch(_){}
    }
    // Limpa caches antigos
    const keys = await caches.keys();
    await Promise.all(keys.filter(k => k !== STATIC_CACHE && k !== RUNTIME_CACHE).map(k => caches.delete(k)));
    await self.clients.claim();
    const cls = await self.clients.matchAll({ includeUncontrolled: true });
    cls.forEach(c => { try { c.postMessage({ type: 'ACTIVATED', version: VERSION }); } catch(_){} });
  })());
});

self.addEventListener('message', e => {
  if (!e.data) return;
  const src = e.source;
  if (e.data.type === 'SKIP_WAITING') self.skipWaiting();
  else if ((e.data.type === 'GET_VERSION' || e.data.type === 'GET_CURRENT_VERSION') && src) {
    try { src.postMessage({ type: e.data.type === 'GET_VERSION' ? 'VERSION' : 'CURRENT_VERSION', version: VERSION }); }
    catch(err){ console.warn('[sw] reply fail', err); }
  }
});

// Trim runtime cache (FIFO simples — sw cache não expõe access time)
async function trimRuntime() {
  const c = await caches.open(RUNTIME_CACHE);
  const keys = await c.keys();
  if (keys.length <= RUNTIME_MAX) return;
  const overflow = keys.length - RUNTIME_MAX;
  for (let i = 0; i < overflow; i++) await c.delete(keys[i]);
}

function isStaticAsset(url) {
  return url.pathname.includes('/icons/') || url.pathname.endsWith('.webmanifest') || url.pathname.endsWith('.svg');
}

function isHTML(req, url) {
  if (req.mode === 'navigate') return true;
  const accept = req.headers.get('accept') || '';
  if (accept.includes('text/html')) return true;
  return url.pathname.endsWith('.html') || url.pathname === '/' || url.pathname.endsWith('/');
}

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;

  // STATIC: cache-first (raramente muda — sw bump força refresh via activate)
  if (isStaticAsset(url)) {
    e.respondWith((async () => {
      const cached = await caches.match(req);
      if (cached) return cached;
      try {
        const resp = await fetch(req);
        if (resp && resp.status === 200 && resp.type === 'basic') {
          const clone = resp.clone();
          caches.open(STATIC_CACHE).then(c => c.put(req, clone)).catch(()=>{});
        }
        return resp;
      } catch(_) {
        return cached || Response.error();
      }
    })());
    return;
  }

  // HTML/navigation: stale-while-revalidate — retorna cache instantâneo, atualiza em bg.
  // Boot ~10x mais rápido em runs subsequentes (app Android usa local server local — cache hit instant).
  // UI desatualizada após deploy é detectada pelo update-banner via SW message, não pelo nav.
  if (isHTML(req, url)) {
    e.respondWith((async () => {
      const cached = await caches.match(req) || await caches.match('./index.html');
      const fetchPromise = (async () => {
        try {
          const preload = e.preloadResponse ? await e.preloadResponse : null;
          const resp = preload || await fetch(req);
          if (resp && resp.status === 200 && resp.type === 'basic') {
            const clone = resp.clone();
            caches.open(RUNTIME_CACHE).then(c => c.put(req, clone).then(trimRuntime)).catch(()=>{});
          }
          return resp;
        } catch(_) {
          return cached || Response.error();
        }
      })();
      // Se temos cache, retorna instant (SWR). Sem cache, espera network.
      return cached || fetchPromise;
    })());
    return;
  }

  // Resto (JS/CSS/etc): stale-while-revalidate
  e.respondWith((async () => {
    const cached = await caches.match(req);
    const fetchPromise = fetch(req).then(resp => {
      if (resp && resp.status === 200 && resp.type === 'basic') {
        const clone = resp.clone();
        caches.open(RUNTIME_CACHE).then(c => c.put(req, clone).then(trimRuntime)).catch(()=>{});
      }
      return resp;
    }).catch(() => cached);
    return cached || fetchPromise;
  })());
});
