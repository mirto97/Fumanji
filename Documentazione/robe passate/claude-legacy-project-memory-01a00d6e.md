**Purpose & context**

Salame is building **Fumanji** — a web-based variant of the classic "Gioco dell'Oca" (Game of the Goose) board game — incrementally, starting with pure Java domain logic before integrating Spring Boot + Angular. The project is hosted at a private GitHub repository (`https://github.com/mirto97/Fumanji.git`).

**Stack decisions:**
- Java 21, Maven
- Spring Boot (not yet integrated — domain logic in pure Java first)
- MySQL, REST-only API (no WebSocket)
- Angular frontend (not yet started)
- Authentication needed (JWT vs. session not yet decided)

**Working dynamic:** Salame is the decision-maker ("la mente") — Claude implements ("il braccio"). Claude writes no code and makes no design decisions autonomously; all design is discussed and agreed first. Suggestions and corrections from Claude are welcome but must be discussed. Code is always verified by compiling and running in a sandbox before being presented.

**Language convention:** Italian for comments and communication; English for all code identifiers.

---

**Current state**

The core domain layer is substantially built and verified. Key classes implemented: `Board`, `Dice`, `Player`, `Square`, `Game`, and special square subclasses (`StartOverSquare`, `MoveBackSquare`, `MoveForwardSquare`, `RollAgainSquare`, `SnackSquare`, `DrinkSquare`, `ObligationSquare`, `CheckpointSquare`, `BivioSquare`, `PortalSquare`). Package structure: `entity` and `entity.squares`; demo files have no package declaration.

**Recent completed work (latest session):**
- `Board` builder pattern implemented (`Board.builder(size).square(...).directionChoiceEnabled(...).build()`)
- `Board.getSquares()` and `Game.getPlayers()` return immutable copies; setter methods for board rules removed (immutable after construction)
- Mandatory checkpoint stop: any `isMandatoryStop()` square stops movement (symmetric for forward and backward)
- Anti-loop guardrail: squares activated during a movement chain cannot be reactivated in the same chain (`activatedPositions` HashSet + `chainInProgress` flag with `try/finally`)
- `Game.addSnack(player, amount)` and `Game.addDrink(player, amount)` added (callable at any moment, clamped to 0); designed to be called directly by frontend
- `ObligationSquare.buildMessage(Player, Game)` and `CheckpointSquare.buildMessage(Player)` updated with proper signatures
- `GameDemo5` written and verified with a 30-run randomized regression suite

**Active architectural decisions:**
- `Board(int size)` constructor stays public for now (trivial to make private when Spring is integrated)
- `playTurn()` limited to simple boards without interactive squares; interactive boards require a custom loop checking `isAwaitingChoice()`
- Portal destination constraints handled via explicit `linkedPositions` lists
- `ObligationSquare.buildMessage()` receives both `Player` and `Game` for future use when a target player must be named
- Movement engine uses direct `while (remaining > 0)` loop (replaced earlier `Deque<Runnable>`/`ArrayDeque` queue-based engine)
- Bivio and Portal choice mechanisms unified into a single set of fields/methods (`isAwaitingChoice`, `getChoiceOptions`, `chooseDestination`, `requestChoice`); `awaitingContinueAfterChoice` flag distinguishes whether movement resumes after choice (Bivio: yes; Portal: no)
- `InteractiveSquare` marker interface introduced for `BivioSquare` and `PortalSquare`
- Movement trace on `Game` (`movementTrace`, `clearMovementTrace()`, `getMovementTrace()`) via private `movePlayerTo` helper

---

**On the horizon**

Remaining TODOs (in rough priority order):
- Factory pattern for squares (JSON `type` → `Square` instance) — deferred to Spring integration
- Bivio with non-converging paths and multiple finish squares — deferred until after a working BE+FE base exists
- Spring Boot integration (backend layer)
- Authentication mechanism decision (JWT vs. session)
- Angular frontend

---

**Key learnings & principles**

- **`snack`/`drink`** are stackable integer counters central to the game concept; `SnackSquare`/`DrinkSquare` reused with negative amounts + clamp to 0 for "remove" effects
- **Victory:** win only on exact landing (classic bounce on overshoot); immediate game-over on first winner; single die (1d6)
- **Chain reactions** between special squares are supported, with the anti-loop guardrail preventing infinite loops
- **`StartOverSquare`** chain call is intentionally commented out by Salame
- **Portal** on exact landing only (passing through mid-move does not trigger); no chain activation; no victory check on arrival — explicit design tradeoff
- **Backward movement:** always has a permanent floor at position 0 (crash prevention); mandatory checkpoint stop is conditional on `Board.mandatoryCheckpointEnabled`
- **`addSnack`/`addDrink`** caller is responsible for iterating over `game.getPlayers()` when applying effects to all players
- **Visibility decisions** should favor future-proof, open visibility by default given the project's expected growth

---

**Approach & patterns**

- **Incremental, demo-driven development:** each session ends with a `GameDemoN` that verifies the current state of the engine. **GameDemo files are use-and-throw checkpoints** — they only need to work when first created. When resuming work on the engine or squares, it is acceptable (and expected) for previous GameDemo files to stop compiling; the goal is always the next working GameDemo, not backward compatibility of old ones.
- **Design-first:** all design decisions are discussed and explicitly agreed before any code is written; Claude flags ambiguities rather than assuming
- **Sandbox verification:** all code compiled and run in sandbox before being presented; regression suites run after structural changes
- **Incremental code presentation:** Claude shows only changed methods/lines inline in chat — never rewrites entire classes (Salame adds personal annotations in the IDE and full rewrites would overwrite them). Full class display is a one-off exception only on explicit request for large refactors
- **End-of-session dump:** always a downloadable `.md` file containing all decisions and updated source code in full (not a summary); this is the continuity artifact between sessions
- **No downloadable files mid-session** — all code inline in chat only

#### Modifiche alla memoria
- Progetto Fumanji: le GameDemo (GameDemoN) sono checkpoint usabili e getta - devono funzionare solo al momento in cui vengono create, per verificare che il codice giri. Quando si riprende a modificare il motore/le caselle, non importa se una GameDemo precedente smette di funzionare/compilare: l'obiettivo è arrivare alla prossima GameDemo funzionante, non mantenere retrocompatibili quelle vecchie.
- Progetto Fumanji: fase attuale (fino a nuova indicazione) = una sola board fissa, fornita da Salame, unica per tutti i giocatori. Gioco pensato come multiplayer locale su un solo dispositivo: all'avvio si indica solo il numero di giocatori e i loro nomi, nessun account/login/registrazione. Creazione di board multiple/personalizzate da parte dei giocatori e sistema di autenticazione sono funzionalità future, rimandate a dopo aver completato un frontend funzionante su questa unica board.