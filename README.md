# Ficha RPG — Aventureiro

Sistema completo de ficha de personagem de RPG com inventário, perícias, veículos, atributos, status, crédito, conquistas, grupo e mais. Construído como SPA single-file em HTML/CSS/JS vanilla — sem dependências, sem build step.

![Stack](https://img.shields.io/badge/stack-HTML%20%7C%20CSS%20%7C%20JS-lightgrey)
![Deps](https://img.shields.io/badge/dependencies-zero-brightgreen)
![Offline](https://img.shields.io/badge/offline-ready-blue)

## Como usar

Abra `index.html` diretamente no navegador — não requer servidor, build ou instalação.

```bash
# opcional: servir local
python -m http.server 8000
# ou
npx serve .
```

## Funcionalidades

- **Dashboard** — Sumário de inventário/crédito, evolução XP/crédito, status com bônus, grupo, categorias (donut), atributos
- **Perícias** — Lista unificada (catálogo com add manual), proficiências, interlúdios de treino com gráfico semanal
- **Progresso** — Nível e estágio XP (travados em 20), gráfico canvas com bezier smooth
- **Crédito** — Histórico transacional, entradas/saídas, moeda personalizável
- **Feitiço / Animal** — Áreas placeholder
- **Veículos** — Integridade (HP), tipo (7 presets + personalizados), atributos individuais (6 categorias com visual próprio), melhorias instaláveis com descrição, imagem customizada
- **Perfil** — Nome, email, linhagem, passado, arquétipo, caminho
- **Save / Load** — Exportação JSON (com imagens embutidas em data URL), import com validação, animação de arquivamento

## Estrutura

```
/
├── index.html          # App completa (HTML + CSS + JS inline)
├── README.md
├── .gitignore
└── LICENSE
```

A abordagem single-file foi escolhida para portabilidade máxima (pode ser enviada por email, salva offline, aberta em qualquer navegador). Para dividir em módulos futuros, extrair:

- `<style>` → `assets/styles.css`
- `<script>` → `assets/app.js`
- SVG icons constant → `assets/icons.js`

## Responsividade

- **Desktop** (>1024px): layout completo 3 colunas
- **Tablet landscape** (>=1024): mantém layout desktop compactado
- **Tablet portrait**: colunas reduzidas, sidebar 60px
- **Mobile portrait** (<640): sidebar vira bottom-nav, layout 1 coluna, inputs com font-size 16px (evita zoom iOS), safe-area (iPhone notch)
- **Mobile landscape**: sidebar lateral fina, 2 colunas densas
- **Print**: remove sidebar/topbar, cards sem sombra, preto no branco
- **prefers-reduced-motion**: animações desativadas
- **pointer:coarse**: alvos táteis >=40px

## Rotação de tela

`updateDeviceOrientation()` define `data-orientation` e `data-device` no `<html>`, redesenha gráficos após resize/orientationchange, com debouncing. Suporte a `screen.orientation API`.

## Persistência

Tudo é salvo em JSON, incluindo imagens como data URLs. `collectAllData()` → blob → download. `applyAllData()` restaura estado completo + `refreshAll()` re-renderiza tudo.

## Stack

- Vanilla JavaScript (ES2020+)
- CSS custom properties (theming dark/light)
- SVG inline para todos os ícones
- Canvas para gráficos (Bezier smooth)
- Web Audio API para efeitos sonoros
- FileReader API para upload de imagens
- Intl/localeCompare para ordenação pt-BR

## Contribuindo

1. Fork
2. Branch: `git checkout -b feat/nome`
3. Commit: `git commit -m 'feat: descrição'`
4. Push: `git push origin feat/nome`
5. PR

## Licença

MIT — ver [LICENSE](LICENSE).
