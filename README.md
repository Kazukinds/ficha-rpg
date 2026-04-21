# Ficha Eclipse

Ficha de personagem RPG single-file — HTML + CSS + JS vanilla, sem dependências, sem build step. Rodável offline, instalável como PWA.

**Live:** https://kazukinds.github.io/ficha-rpg/

[![Abrir PWA](https://img.shields.io/badge/abrir-PWA-22D3EE?style=for-the-badge)](https://kazukinds.github.io/ficha-rpg/)
[![Baixar APK](https://img.shields.io/badge/baixar-APK%20Android-C8F542?style=for-the-badge&logo=android&logoColor=black)](https://github.com/Kazukinds/ficha-rpg/releases/latest/download/FichaEclipse-widgets.apk)
[![Build](https://github.com/Kazukinds/ficha-rpg/actions/workflows/android-widgets.yml/badge.svg)](https://github.com/Kazukinds/ficha-rpg/actions/workflows/android-widgets.yml)

![Stack](https://img.shields.io/badge/stack-HTML%20%7C%20CSS%20%7C%20JS-lightgrey)
![Deps](https://img.shields.io/badge/dependencies-zero-brightgreen)
![PWA](https://img.shields.io/badge/PWA-ready-blue)

## Download rápido

| Plataforma | Link | Como instalar |
|---|---|---|
| 🌐 Web / PWA | [kazukinds.github.io/ficha-rpg](https://kazukinds.github.io/ficha-rpg/) | Abre no navegador → "Instalar app" |
| 🤖 Android (APK) | [FichaEclipse-widgets.apk](https://github.com/Kazukinds/ficha-rpg/releases/latest/download/FichaEclipse-widgets.apk) | Habilita "Fontes desconhecidas" → instala |
| 📦 Todas as releases | [releases](https://github.com/Kazukinds/ficha-rpg/releases) | Versões anteriores + changelog |

## Como rodar

```bash
# abrir direto no navegador
start index.html     # Windows
open  index.html     # macOS
xdg-open index.html  # Linux

# ou servir local pra funcionalidade completa de PWA
python -m http.server 8000
npx serve .
```

## Instalação como app

- **Android (Chrome):** menu → "Instalar aplicativo"
- **iOS (Safari):** compartilhar → "Adicionar à tela de início"
- **Desktop (Chrome/Edge):** ícone de instalar na barra de endereço

O service worker cacheia tudo no primeiro load — depois disso funciona 100% offline.

## Estrutura

```
/
├── index.html              # App completo (HTML + CSS + JS inline, ~420KB)
├── manifest.webmanifest    # PWA manifest
├── sw.js                   # Service worker (network-first)
├── icons/                  # Ícones PWA (maskable, apple-touch, favicon)
├── docs/                   # Documentação técnica
└── LICENSE
```

## Documentação

- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — estrutura interna do `index.html`, estado global, ciclo de renderização
- **[docs/FEATURES.md](docs/FEATURES.md)** — o que cada aba faz (Dashboard, Perícias, Crédito, Veículos, Perfil…)
- **[docs/DEPLOY.md](docs/DEPLOY.md)** — deploy no GitHub Pages, PWA, invalidação de cache
- **[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)** — como editar, convenções, onde achar cada seção
- **[docs/CHANGELOG.md](docs/CHANGELOG.md)** — histórico de versões (sincronizado com o Log no perfil do app)

## Licença

MIT — veja [LICENSE](LICENSE).
