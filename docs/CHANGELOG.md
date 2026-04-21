# Changelog

Histórico de versões do Ficha Eclipse. Este arquivo é espelho do `APP_CHANGELOG` em `index.html` — o log que aparece no perfil do aventureiro vem do JS.

Formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) adaptado.

## [v1.5] — 2026-04-21

### Added
- **Banner de atualização** com barra de progresso + safe-area mobile — notifica quando nova versão está disponível e instala ao clicar
- **Service worker v35+** com fluxo `SKIP_WAITING` controlado (não ativa automaticamente)
- **Áudio procedural** via WebAudio: 14 sons semânticos (`coin`, `coinBig`, `spend`, `damage`, `heal`, `levelup`, `critical`, `hover`, `tick`, `save`, `unlock`, `error`, `whoosh`)
  - Som de moedas ao depositar crédito, som diferenciado para grandes somas (≥500)
  - Level-up distinto de progressão normal
  - Dano/cura ao ajustar HP, crítico/falha na rolagem de perícias
  - Pop ao equipar item, whoosh ao descartar
- **Select-to-ask**: destaque de texto em áreas selecionáveis com botão flutuante "Perguntar à IA"
- **Drawer IA assistente** no topbar (Alt+I): textarea + 14 tópicos clicáveis (regras, perícias, combate, caminhos, arquétipos, linhagem, poderes, vida, atributos, crédito, veículos, inventário, dados, biblioteca)
  - Detecção de pergunta vaga → dica pra adicionar contexto
  - Enriquecimento automático com contexto de tópicos selecionados
  - Fila de perguntas persistida (localStorage) com badge de contagem
- **Biblioteca integrada** como aba no app (iframe lazy-load)
  - postMessage bridge: botão "🤖 Perguntar à IA" dentro do reader envia capítulo + trecho pra fila do drawer
- **Templates GitHub**: issue (bug/feature) PT-BR, PR checklist, CONTRIBUTING.md
- **README** redesenhado com badges, roadmap, tabela de download

### Changed
- **Veículos compacto**: colunas 180/1fr/300 → 160/1fr/280, paddings tightened, novo breakpoint 1100-1400 (right panel desce pra linha horizontal), carrossel vira scroller horizontal em <1100
- **Tema claro** refinado: cores WCAG-compliant (text-dim 4.5:1), accents com mais contraste (lime #16a34a, red #dc2626), shadow stack mais rica
- **meta[theme-color]** atualiza dinamicamente ao alternar tema
- **Link de Biblioteca** no perfil agora abre a aba embutida em vez de página externa

### Fixed
- Banner de update respeita `env(safe-area-inset-bottom)` e empilha botões em coluna-reversa em telas <480px
- `updateMetaTheme()` garante status bar correta em PWA Android/iOS

## [v1.4] — 2026-04-18

### Added
- Botão `+` circular dentro de **Linhagem / Arquétipo / Passado / Caminho** (só em modo editar) que abre modal de Nova Habilidade
- Modal Nova Habilidade redesenhado: header colorido por origem (ciano/roxo/amarelo/lime), stepper de custo, preview ao vivo `"5 FOC por uso"`
- Seletor **"Drena de"** no custo da habilidade: Vitalidade / Esforço / Foco / Determinação / Neural (Córtex fica de fora)
- Modal de Crédito redesenhado: header colorido por operação, pills rápidos (10/50/100/500/1000), preview do saldo após, chips de descrição contextual
- Animações temáticas nos ícones dos atributos de veículo ao hover/touch
- Ícone de Aceleração com animação de batimento cardíaco (stroke-dasharray + dashoffset em loop)
- Botão de tema (lua/sol) movido para o topbar, acessível sem scroll
- Bloco **Log** no perfil, abaixo de Estatísticas, com histórico versionado

### Changed
- Linhagem / Arquétipo / Passado / Caminho movidos para o card **Detalhes do Personagem**
- Data de Criação removida do perfil
- Proficiência usa pill "Não possui" em vez de travessão
- Scrollbar invisível globalmente (rolagem continua funcionando)
- Sidebar do perfil sticky e cards com espaçamento uniforme

### Fixed
- Campo Telefone vazio agora tem mesma largura do Fuso Horário
- Custo da habilidade não sobrepõe mais o botão de usar
- Barra inferior de navegação realmente fixa no mobile (fix: `transform` do body criava containing block que quebrava `position:fixed`)
- Botões circulares não viram mais ovais no mobile (`aspect-ratio:1/1` global)
- Saves antigos com `Nível=20 / Estágio=20 / XP=0` são tratados como default antigo e resetam para 0

### Removed
- Sistema de entalhe / modo imersivo (botão fullscreen e toggle)

## [v1.3] — 2026-04-18

### Added
- Bloco **Log** no perfil do aventureiro (abaixo de Estatísticas)
- Autosave em **IndexedDB** como backup do localStorage
- Botão de tema no **topbar** (lua ↔ sol), acessível sem scroll

### Changed
- Dashboard voltou pro layout anterior (Inventário + Evolução + Status + Categorias + Grupo + Atributos)
- Proficiência usa tag "Não possui" em vez de travessão
- Campos Linhagem/Passado/Arquétipo/Caminho ficam limpos (sem `—`)
- Barra inferior de navegação agora realmente fixa no mobile (era quebrada por `transform` em containing block)

### Removed
- Sistema de modo imersivo / tela cheia
- Botão de atualizações do topbar (log fica só no perfil)

### Fixed
- Saves antigos com `level=20 stage=20 xp=0` são tratados como default e zerados
- Botões circulares não viram ovais no mobile (`aspect-ratio:1/1`)

## [v1.2] — 2026-04-18

### Added
- Rebrand: `ficha.dexport` → **Ficha Eclipse**
- Autosave a cada 8s, em `visibilitychange=hidden`, em `pagehide`, em `beforeunload`
- Restauração automática da ficha ao abrir o app
- Confirmação nativa antes de recarregar; `Ctrl+R` / `F5` pedem confirmação extra
- Changelog acessível como modal

### Fixed
- Pull-to-refresh desabilitado (`overscroll-behavior-y:contain`)
- Service worker com estratégia network-first pra HTML/JS/CSS

## [v1.1] — 2026-04-18

### Added
- Splash animada na abertura do app
- Meta `format-detection` pra evitar iOS transformar "0" em link de telefone

### Fixed
- Integridade, HP e Base iniciam zerados
- Tooltip de atributos funciona no toque (mobile)
- Mensagem "Bem-vindo, aventureiro" visível no mobile/tablet
- Crédito, XP, grade e veículo em 1 coluna no vertical
- Quadradinhos de status reformulados (mini-cards)
- Entalhe do celular respeitado via `safe-area-inset`

## [v1.0] — 2026-04-17

### Added
- Release inicial
- Ficha RPG completa: atributos, status, perícias, veículos, crédito, inventário, grupo, conquistas
- PWA instalável com ícone lightning
- Service worker com cache offline
- Tema dark/light
- Save/Load via JSON

---

**Nota de sincronia:** ao adicionar uma entrada aqui, atualizar também `APP_CHANGELOG` em `index.html` (busca por `const APP_CHANGELOG=`) pra aparecer no Log do perfil.
