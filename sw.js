/* Ficha Eclipse — service worker */
const VERSION = 'v2.0.4';
const CACHE = 'ficha-eclipse-' + VERSION;
const ASSETS = [
  './',
  './index.html',
  './biblioteca.html',
  './calendario.html',
  './manifest.webmanifest',
  './icons/icon.svg',
  './icons/icon-maskable.svg',
  './icons/favicon.svg',
  './icons/logo-eclipse-moon.svg',
  './icons/logo-eclipse-star.svg'
];

// Notify clients when a new version installs
self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
      .then(() => self.clients.matchAll({includeUncontrolled:true}))
      .then(cls => cls.forEach(c => { try { c.postMessage({type:'ACTIVATED', version: VERSION}); } catch(_){} }))
  );
});

self.addEventListener('message', e => {
  if (e.data && e.data.type === 'SKIP_WAITING') self.skipWaiting();
  if (e.data && e.data.type === 'GET_VERSION' && e.source) e.source.postMessage({type:'VERSION', version: VERSION});
  if (e.data && e.data.type === 'GET_CURRENT_VERSION' && e.source) e.source.postMessage({type:'CURRENT_VERSION', version: VERSION});
});

self.addEventListener('fetch', e => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);
  if (url.origin !== location.origin) return;

  // Network-first for everything same-origin so updated files (HTML, SVG, JS, CSS) always reflect changes.
  // Cache acts only as offline fallback.
  e.respondWith(
    fetch(req).then(resp => {
      if (resp && resp.status === 200 && resp.type === 'basic') {
        const clone = resp.clone();
        caches.open(CACHE).then(c => c.put(req, clone));
      }
      return resp;
    }).catch(() => caches.match(req).then(c => c || (req.mode === 'navigate' ? caches.match('./index.html') : undefined)))
  );
});
