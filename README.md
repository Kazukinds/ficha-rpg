# 🌒 Ficha Eclipse

> Ficha de personagem RPG single-file — HTML + CSS + JS vanilla. Zero dependências, zero build step. Rodável offline, instalável como PWA, com widgets Android nativos.

**🌐 Live:** [kazukinds.github.io/eclipse](https://kazukinds.github.io/eclipse/)

[![Abrir PWA](https://img.shields.io/badge/abrir-PWA-22D3EE?style=for-the-badge)](https://kazukinds.github.io/eclipse/)
[![Baixar APK](https://img.shields.io/badge/baixar-APK%20Android-C8F542?style=for-the-badge&logo=android&logoColor=black)](https://github.com/Kazukinds/eclipse/releases/latest/download/Eclipse.apk)
[![Build](https://github.com/Kazukinds/eclipse/actions/workflows/android.yml/badge.svg)](https://github.com/Kazukinds/eclipse/actions/workflows/android.yml)

![Stack](https://img.shields.io/badge/stack-HTML%20%7C%20CSS%20%7C%20JS-lightgrey)
![Deps](https://img.shields.io/badge/dependencies-zero-brightgreen)
![PWA](https://img.shields.io/badge/PWA-ready-blue)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 📥 Download rápido

| Plataforma | Link | Como instalar |
|---|---|---|
| 🌐 **Web / PWA** | [kazukinds.github.io/eclipse](https://kazukinds.github.io/eclipse/) | Abre no navegador → *Instalar app* |
| 🤖 **Android (APK)** | [Eclipse.apk](https://github.com/Kazukinds/eclipse/releases/latest/download/Eclipse.apk) | Habilita *Fontes desconhecidas* → instala |
| 📦 **Todas as releases** | [releases](https://github.com/Kazukinds/eclipse/releases) | Versões anteriores + changelog |

---

## ✨ Funcionalidades

### Ficha do personagem
- 📊 **Atributos** em 3 grupos (Físico, Mental, Social) com bônus
- ❤️ **Status**: HP, estamina, sanidade, fome, sede, fadiga
- ⚔️ **Inventário** com grid visual, tamanhos 1x1 até 3x3, raridade, durabilidade
- 🎯 **Perícias** destreinadas/treinadas/especialistas com rolagem 1d20
- 💰 **Crédito** (moeda customizável) com histórico, depósito/saque/transferência
- 🚗 **Veículos** com upgrades, HP, performance (aceleração/vel. máx/manuseio)
- 📈 **Progressão**: níveis + estágios com XP log
- 👤 **Perfil**: linhagem, passado, arquétipo, caminho

### PWA
- 💾 **Save/load** em JSON (download + IndexedDB backup)
- 🔄 **Banner de atualização** com barra de progresso (safe-area mobile)
- 🌙 **Modo claro e escuro** (toggle instantâneo)
- 🔊 **Som procedural** (WebAudio): moedas, dano, cura, crítico, level-up
- 🎲 **5 widgets dashboard**: dados, iniciativa, nível, notas, timer
- 📴 **Offline 100%** após primeiro load

### Android nativo
- Widgets de home screen independentes do PWA
- APK assinado buildado via CI no push

---

## 🚀 Como rodar

```bash
git clone https://github.com/Kazukinds/eclipse.git
cd eclipse

# abrir direto
start index.html          # Windows
open  index.html          # macOS
xdg-open index.html       # Linux

# ou servir local (recomendado — PWA + SW só funcionam em http://)
python -m http.server 8000
npx serve .
```

## 📱 Instalação como app

| Plataforma | Passos |
|---|---|
| Android (Chrome) | Menu ⋮ → *Instalar aplicativo* |
| iOS (Safari) | Compartilhar ↑ → *Adicionar à tela de início* |
| Desktop (Chrome/Edge) | Ícone ⊕ na barra de endereço |

Service worker cacheia tudo no primeiro load — depois roda 100% offline.

---

## 🗂️ Estrutura

```
/
├── index.html              # App principal (~9k linhas, tudo inline)
├── biblioteca.html         # Compêndio de lore (standalone)
├── sw.js                   # Service worker (network-first docs, cache-first assets)
├── manifest.webmanifest    # PWA manifest
├── alert-component.html    # Componente de alerta isolado
├── icons/                  # Ícones PWA (SVG + PNG maskable)
├── widgets/                # Widgets HTML (dashboard interno)
│   ├── dice.html
│   ├── init.html
│   ├── level.html
│   ├── notes.html
│   └── timer.html
├── android-widgets/        # App Android nativo (home-screen widgets)
├── docs/                   # Documentação técnica
└── .github/                # CI + issue/PR templates
```

---

## 🗺️ Roadmap

- [x] PWA offline + instalação
- [x] Modo claro/escuro
- [x] Widgets Android nativos (Dados)
- [x] Banner de atualização com barra de progresso
- [x] Áudio procedural (coin, damage, heal, levelup…)
- [ ] Biblioteca integrada à ficha (poderes/caminhos/arquétipos)
- [ ] IA assistente com perguntas contextuais
- [ ] Select-to-ask (destaque + pergunta sobre trecho)
- [ ] Upgrade veículos compacto
- [ ] Redesign cores unificado
- [ ] Mais widgets Android (Iniciativa, Nível, Timer)

---

## 📚 Documentação

- **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)** — estrutura interna, estado global, ciclo de render
- **[docs/FUNCIONALIDADES.md](docs/FUNCIONALIDADES.md)** — detalhe de cada aba
- **[docs/IMPLANTACAO.md](docs/IMPLANTACAO.md)** — deploy GitHub Pages, PWA, cache
- **[docs/DESENVOLVIMENTO.md](docs/DESENVOLVIMENTO.md)** — convenções, onde achar cada coisa
- **[docs/HISTORICO.md](docs/HISTORICO.md)** — histórico de versões
- **[build-app/COMPILAR.md](build-app/COMPILAR.md)** — build Android/iOS nativo via Capacitor (ou WebToApp)

---

## 🤝 Contribuir

Leia **[CONTRIBUTING.md](CONTRIBUTING.md)** para convenções, fluxo de PR e como reportar bugs.

- 🐛 [Reportar bug](https://github.com/Kazukinds/eclipse/issues/new?template=bug_report.yml)
- ✨ [Sugerir feature](https://github.com/Kazukinds/eclipse/issues/new?template=feature_request.yml)
- 💬 [Discussões](https://github.com/Kazukinds/eclipse/discussions)

---

## 📄 Licença

MIT — veja [LICENSE](LICENSE).
