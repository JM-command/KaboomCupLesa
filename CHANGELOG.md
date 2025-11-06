## 0.1.4 – 2025-11-06
### Changé
- **Scoreboard compact** : les vies d’équipe n’affichent plus les pseudos pour éviter les lignes trop longues.
- Deux modes d’affichage configurables via `scoreboard.livesMode` :
    - `sum` (par défaut) → total des vies restantes par équipe.
    - `bars` → `◆` par joueur vivant, `◇` si out.
- Join (en LOBBY) : clear inventaire + armure au join pour un état propre.

## 0.1.3 – 2025-10-30
### Ajouté
- `BossBarService` pour afficher l’état du match (LOBBY / PAUSE / RUNNING + timer).
- Ajout automatique des joueurs à la bossbar quand ils rejoignent.

### Changé
- Flux lobby → match stabilisé (join en aventure, item hub seulement en lobby).
- Clear de l’item hub au démarrage de la partie.
- Retour lobby qui redonne l’item hub.
- Nametags ré-appliqués si le joueur avait déjà une team.
- Void: on annule les dégâts de chute après TP.
- TNT ownership: meilleure protection côté propre selon `field.middleX`.

### Fix
- Partie qui se terminait alors qu’un seul joueur tombait dans le vide.
- PvP actif au lobby (listener de protection lobby).
- Messages pas alignés avec les nouvelles commandes.


## 0.1.2 – 2025-10-30
- feat: BossBar de match (configurable messages)
- feat: Ownership TNT via PDC + anti friendly-fire par côté
- feat: Équipement auto Bleu/Rouge (plastron/jambières/bottes)
- fix: messages.yml aligné + accents, /menu bloqué en RUNNING
- ci: workflow build/release
