# KaboomCup Lesa

Mini-jeu **TNT Wars 5v5** pour ateliers du jeudi [lesa.teliers.com](https://lesa.teliers.com), animé par **JM_command**.  
**Paper 1.20.1**, **Java 17**. Sponsor: **IrixiaGroup.ch**.

## ✨ Features

- **5v5** TNT Wars (build & fight rapide, 20–30 min)
- **/kaboom start** → freeze configurable, TP bases, **kit auto** au start
- **/kaboom pause → /kaboom play** (reprise)
- **/kaboom stop** → retour lobby + item hub
- **Kits**: `/kit simple|tnt|pioche|redstone|blocks|water`
- **Item de hub** (boussole) → clic = menu d’équipes (/menu)
- **Faim infinie**
- **Anti-void** (TP base + -1 vie, pas d’écran de mort)
- **Scoreboard anti-flicker**: états, timer, vies, tailles équipes, sponsor
- **Nametag/TAB colorés** selon l’équipe

## 📦 Installation

1. Build:
   ```bash
   mvn -q -DskipTests package
   ```

Le JAR est dans `target/`.

2. Dépose le JAR dans `plugins/` (Paper 1.20.1, Java 17).

3. Démarre une fois pour générer `config.yml` et `messages.yml`.

## ⚙️ Configuration

`src/main/resources/config.yml` (extraits clés) :

```yaml
world: "tntwars"

spawns:
  spawn:    { world: ..., x: ..., y: ..., z: ..., yaw: ..., pitch: ... }
  baseBlue: { ... }
  baseRed:  { ... }

rules:
  livesPerPlayer: 3
  matchDurationSeconds: 1200
  freezeOnStartSeconds: 5
  maxPlayersPerTeam: 5
  requireTeamsBalanced: true

gameplay:
  infiniteHunger: true
  voidKillY: 50

kits:
  simple:
    giveOnStart: true
    items:
      - TNT:16
      - REDSTONE:32
      - REDSTONE_REPEATER:8
      - LEVER:2
      - OBSIDIAN:16
      - WATER_BUCKET:1
      - STONE:64
      - IRON_PICKAXE:1
```

**Positions** (persistées dans `spawns.*`) :

* `/kaboom setspawn` : hub/lobby
* `/kaboom setblue`  : base bleue
* `/kaboom setred`   : base rouge

## 🧰 Commandes

* `/kaboom start` – Démarrer une partie
* `/kaboom pause` – Mettre en pause
* `/kaboom play` – Reprendre
* `/kaboom stop` – Arrêter et retourner au lobby
* `/kaboom force <player> <blue|red>` – Forcer l’équipe
* `/kaboom setspawn|setblue|setred` – Enregistrer les positions
* `/kaboom reload` – Recharger config/messages
* `/menu` – Ouvrir le menu d’équipes (désactivé en RUNNING)
* `/kit <simple|tnt|pioche|redstone|blocks|water>` – Donner un kit

## 🔐 Permissions

```yaml
kaboom.admin: true   # accès commandes admin /kaboom
```

## 🧪 Cycle (recommandé)

1. `/kaboom setspawn`, `/kaboom setblue`, `/kaboom setred` (première fois)
2. Les joueurs rejoignent → boussole → **/menu** pour choisir équipe
3. `/kaboom start` → freeze → TP bases → **kit simple auto**
4. `/kaboom pause` → explications / corrections → `/kaboom play`
5. `/kaboom stop` → retour lobby + boussole

## 🛠 Build

* **Java 17**, **Maven 3.8+**
* Dépendance : `io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT` (scope provided)

## 👤 Auteur

* **JM_command** — [IrixiaGroup.ch](https://IrixiaGroup.ch)
* Projet ateliers: **[lesa.teliers.com](https://lesa.teliers.com)**

---
