/* Ficha Eclipse — service worker */
const CACHE='ficha-eclipse-v34';
const ASSETS=[
  './',
  './index.html',
  './biblioteca.html',
  './manifest.webmanifest',
  './icons/icon.svg',
  './icons/icon-biblioteca.svg',
  './icons/brand.svg',
  './icons/brand-biblioteca.svg',
  './icons/icon-maskable.svg',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/icon-maskable-192.png',
  './icons/icon-maskable-512.png',
  './icons/apple-touch-icon.png',
  './icons/favicon-16.png',
  './icons/favicon-32.png'
];

self.addEventListener('install',e=>{
  e.waitUntil(caches.open(CACHE).then(c=>c.addAll(ASSETS)).then(()=>self.skipWaiting()));
});

self.addEventListener('activate',e=>{
  e.waitUntil(
    caches.keys().then(keys=>Promise.all(keys.filter(k=>k!==CACHE).map(k=>caches.delete(k))))
      .then(()=>self.clients.claim())
  );
});

self.addEventListener('fetch',e=>{
  const req=e.request;
  if(req.method!=='GET')return;
  const url=new URL(req.url);
  if(url.origin!==location.origin)return;

  const isDoc=req.mode==='navigate'||req.destination==='document';
  const isAsset=['script','style','worker'].includes(req.destination);

  if(isDoc||isAsset){
    // network-first: sempre tenta rede, cai pro cache só offline
    e.respondWith(
      fetch(req).then(resp=>{
        if(resp&&resp.status===200&&resp.type==='basic'){
          const clone=resp.clone();
          caches.open(CACHE).then(c=>c.put(req,clone));
        }
        return resp;
      }).catch(()=>caches.match(req).then(c=>c||caches.match('./index.html')))
    );
    return;
  }

  // cache-first para ícones, imagens e afins
  e.respondWith(
    caches.match(req).then(cached=>{
      if(cached)return cached;
      return fetch(req).then(resp=>{
        if(resp&&resp.status===200&&resp.type==='basic'){
          const clone=resp.clone();
          caches.open(CACHE).then(c=>c.put(req,clone));
        }
        return resp;
      }).catch(()=>cached);
    })
  );
});
