/* Ficha Eclipse — service worker */
const VERSION = 'v3.8.40';
const CACHE = 'ficha-eclipse-' + VERSION;
const ASSETS = [
  './',
  './index.html',
  './manifest.webmanifest',
  './icons/icon.svg',
  './icons/icon-maskable.svg',
  './icons/favicon.svg',
  './icons/logo-eclipse-moon.svg',
  './icons/logo-eclipse-star.svg',
  './icons/equip/cabeca.svg',
  './icons/equip/rosto.svg',
  './icons/equip/torso.svg',
  './icons/equip/pernas.svg',
  './icons/equip/pes.svg',
  './icons/equip/conjunto.svg'
];

self.addEventListener('install', e => {
  // Use addAll com fallback individual: se um asset falhar, não quebra o install inteiro.
  e.waitUntil(caches.open(CACHE).then(c =>
    Promise.all(ASSETS.map(a => c.add(a).catch(err => console.warn('[sw] cache fail', a, err))))
  ));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
      .then(() => self.clients.matchAll({includeUncontrolled:true}))
      .then(cls => cls.forEach(c => { try { c.postMessage({type:'ACTIVATED', version: VERSION}); } catch(e){ console.warn('[sw] postMessage fail', e); } }))
  );
});

self.addEventListener('message', e => {
  if (!e.data) return;
  const src = e.source;
  if (e.data.type === 'SKIP_WAITING') self.skipWaiting();
  else if ((e.data.type === 'GET_VERSION' || e.data.type === 'GET_CURRENT_VERSION') && src) {
    try { src.postMessage({type: e.data.type === 'GET_VERSION' ? 'VERSION' : 'CURRENT_VERSION', version: VERSION}); }
    catch(err){ console.warn('[sw] reply fail', err); }
  }
});

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;

  // Stale-while-revalidate: serve cache imediato, atualiza em background.
  // Cache vive 1 versão (CACHE muda em VERSION bump → activate limpa antigos).
  e.respondWith(
    caches.match(req).then(cached => {
      const fetchPromise = fetch(req).then(resp => {
        if (resp && resp.status === 200 && resp.type === 'basic') {
          const clone = resp.clone();
          caches.open(CACHE).then(c => c.put(req, clone)).catch(()=>{});
        }
        return resp;
      }).catch(() => cached || (req.mode === 'navigate' ? caches.match('./index.html') : undefined));
      return cached || fetchPromise;
    })
  );
});
