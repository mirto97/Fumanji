# FUMANJI — Dump di Progetto (main dump unito)

> **Data e ora**: 02/10/2026, 04:43 (Europe/Rome).
> **Natura del file**: **main dump aggiornato**, ottenuto unendo il dump principale del 24-09-26 (`Fumanji-main-dump-24-09-26.md`) con il delta unito del 30-09-26 15:13 (`Fumanji-dump-delta-unito-30-09-26_1513.md`, a sua volta unione dei delta del 28-09-26, 29-09-26 e 30-09-26). Dal main sono state tolte **solo** le parti diventate obsolete (TODO chiusi, "non ancora scritto/deciso" superati, versioni di codice sostituite); tutto il resto è conservato. Le espressioni relative "questa sessione" del vecchio main sono state sostituite con la data reale (24-09-26).
> Archivio esteso, non riassunto. Contiene lo **stato corrente** del progetto: tutto ciò che è stato deciso e il codice così com'è oggi. Non contiene passaggi intermedi superati (es. `GameDemo2/3/4/5`, tutte superate; il vecchio `ObligationSquare` a messaggio fisso, sostituito da quello parametrico; il vecchio clamp del checkpoint backward di sessioni precedenti, già sostituito dal fermo simmetrico; il vecchio `FixedDice` dei test; lo stato di `GameStateResponse` senza `messages`). Da caricare a inizio di ogni nuova conversazione per riprendere il lavoro da qui.

---

## 1. Scopo e contesto

Sviluppo di una web application: variante del "Gioco dell'Oca" classico, con caselle speciali che attivano effetti vari (alcuni "stupefacenti"). Stack: **Backend Spring Boot + Frontend Angular**. Il layer di dominio in **Java puro** è stato completato nelle sessioni precedenti; il 24-09-26 è stato integrato Spring Boot (scheletro + persistenza JPA) e mappata per intero la board OG reale; dal 28-09-26 al 30-09-26 sono stati scritti il layer applicativo e REST completo per una partita intera e avviato il frontend Angular (30-09-26): **una partita completa è giocabile dal frontend** (benvenuto, nomi, gioco, vincitore). Prossimo pezzo grosso: il tabellone (sezione 7).

**Repository**: `https://github.com/mirto97/Fumanji.git` (privato)

**Fase attuale del progetto** (decisa il 24-09-26): si lavora con **una sola board fissa** (la board OG, disegnata a mano da Salame — vedi sezione 9). Il gioco è pensato come multiplayer **locale su un solo dispositivo**: all'avvio si indicano solo il numero di giocatori e i loro nomi, **nessun account/login**. Creazione di board multiple/personalizzate da parte dei giocatori e sistema di autenticazione sono funzionalità future, rimandate a dopo aver completato un frontend funzionante su questa unica board.

---

## 2. Modalità di lavoro (regole di processo — vincolanti)

- **"IO SONO LA MENTE E TU SEI IL BRACCIO"**: l'utente decide, Claude scrive codice. Claude non deve pensare/decidere al posto dell'utente a meno che non venga esplicitamente richiesto. Consigli, correzioni e accorgimenti sono sempre ben accetti e vanno discussi insieme.
- Risposte **veloci e dirette**; approfondire solo se richiesto esplicitamente; **non ripetere** cose già scritte o decise.
- **Non scrivere codice finché non si è sicuri** di cosa si sta scrivendo. Se c'è un'ambiguità reale di design, si chiede prima di scrivere.
- **Non supporre o dedurre nulla** a meno che non venga detto esplicitamente; in caso di dubbio, si verifica insieme il risultato.
- **Non inventare nulla** di propria iniziativa — chiedere, piuttosto.
- Nomi di classi/metodi/variabili sempre in **inglese**. Commenti nel codice sempre in **italiano**, asciutti e in stile "codice da progetto", e solo quando esplicitamente richiesti.
- Il codice va scritto **sempre inline in chat** (mai come file scaricabile) — **eccetto il dump di fine conversazione**, che è invece sempre un file `.md` scaricabile.
- **Quando si modifica codice già scritto, non riscrivere mai la classe intera** — mostrare solo i metodi/le righe esatte cambiate, per non sovrascrivere i commenti personali che Salame aggiunge via IDE. Le classi **nuove di zecca** si possono mostrare per intero alla prima stesura.
- Esiste sempre un file di progetto con **"...dump..."** nel nome, contenente lo stato corrente del progetto. Se Claude non lo trova in una nuova chat, deve segnalarlo. Ogni nuova conversazione riparte da lì. **A fine conversazione Claude produce un dump delta snello** (solo ciò che è stato fatto/deciso in quella sessione, con la versione finale dei file toccati; cose cancellate/obsolete/superate non compaiono; archivio esteso, non riassunto), sempre come file `.md` scaricabile. L'unione la fa l'utente a parte, **su richiesta e a step**: prima i delta fra loro in un unico delta unito, poi il delta unito con il main dump → nuovo main dump completo.
- **Regole di unione dei dump** (richieste dall'utente il 02-10-26): farla per bene, non saltare niente, non togliere cose dal main dump a meno che non siano diventate obsolete; la data si legge dai dump delta, così da sapere cosa è stato fatto adesso, cosa c'era da prima, cosa è stato modificato o aggiunto.
- **Nomi dei file di dump unito**: tipo di dump unito + data e ora: `Fumanji-dump-delta-unito_DD-MM-YY_HHMM` oppure `Fumanji-main-dump-unito_DD-MM-YY_HHMM`.
- Prassi consolidata: quando possibile, Claude compila ed esegue il codice nel proprio ambiente sandbox per verificarne la correttezza **prima** di presentarlo, e ripete la regressione su tutto il comportamento già documentato dopo ogni modifica strutturale. **Limite noto** (emerso il 24-09-26): il sandbox **non ha accesso a Maven Central**, quindi il codice Spring/JPA/MapStruct non è compilabile lì — quella verifica la fa Salame in locale/IDE e riporta eventuali errori. Solo il Java puro (senza dipendenze Spring) resta compilabile in sandbox. **Aggiornamento del limite noto** (sessioni successive): nel sandbox non c'è il binario `javac`, ma il runtime contiene il modulo `jdk.compiler`, quindi il Java puro si compila con `java -m jdk.compiler/com.sun.tools.javac.Main ...`. Per gli script SQL, la verifica si fa invece ricreando lo schema reale (DDL generato da Hibernate) in un MySQL/MariaDB di sandbox e testando lì l'insert, più una simulazione del grafo di gioco.
- Quando una richiesta rivela una portata strutturale più ampia di quanto sembrasse, Claude deve fermarsi e spiegare chiaramente l'impatto reale prima di procedere, piuttosto che minimizzare o iniziare a scrivere codice parziale.
- **Le `GameDemoN` sono checkpoint usa-e-getta**: devono funzionare solo al momento in cui vengono create, per verificare che il codice giri. Quando si riprende a modificare il motore/le caselle, non importa se una `GameDemo` precedente smette di funzionare o di compilare — l'obiettivo è arrivare alla prossima `GameDemo` funzionante, non mantenere le vecchie retrocompatibili. **`GameDemo5` è stata rotta definitivamente il 24-09-26** (il cambio di firma di `ObligationSquare` la rompe, e va bene così — nessuna nuova demo la sostituisce per ora, si è passati direttamente all'integrazione Spring/persistenza).

### Ambiente e preferenze (sessioni 28-09-26 → 30-09-26)

- **Ambiente dell'utente**: VS Code (non IntelliJ/Eclipse), Windows + PowerShell. Maven **non** è installato globalmente: si usa il wrapper `.\mvnw.cmd` **dalla cartella del backend** (nella root `Fumanji` non esiste; dal 30-09-26 il percorso è `BE\spring`, prima era `BE spring`). I test JUnit si lanciano da VS Code (icone accanto ai `@Test` / pannello Test Results).
- **PowerShell + Maven**: `-Dincludes=g:a` con i due punti viene interpretato male da PowerShell; per cercare una dipendenza si redirige `dependency:tree` su file e si usa `Select-String`.
- **MySQL Workbench**: l'autocompletamento può duplicare il testo mentre si digita/incolla uno script (errore 1064 con `board_entityboard_entityboard_entity`): usare una tab di query nuova e incollare senza editare.
- **`ddl-auto=update` non rende nullable una colonna già esistente `NOT NULL`**: per applicare la modifica di `board_id` è servito droppare e ricreare il DB.
- **Preferenza dell'utente sui test**: vuole poter fissare il risultato del dado nei test per coprire vari casi → realizzato il 29-09-26 con `ControllableDice` (vedi sezione 5, "Dice iniettabile").
- **Angular/TypeScript**: l'utente non è ancora pratico (a differenza di Spring/Java). Non vuole la spiegazione di ogni pezzo di codice: chiede lui quando servono spiegazioni. Le cose vanno comunque verificate insieme spesso.
- **Commenti nel codice TypeScript/Angular**: sempre presenti sui pezzi di logica (italiano, asciutti, da codice di progetto), così l'utente può riguardare e studiare ciò che non conosce. Per il Java resta la regola precedente (commenti solo se richiesti).
- **Frontend**: si parte da componenti base al volo e poi si rifiniscono.
- **Libro di riferimento**: l'utente ha studiato Angular su un libro fino alla versione 20.3.13. Il progetto usa la 21.2.24: se qualcosa nel libro non corrisponde al progetto, si controlla insieme.
- **Versioni installate**: Angular CLI 21.2.24, Angular 21.2.24, TypeScript 5.9.3, Node 24.13.0, npm 11.6.2, rxjs 7.8.2, Vitest 4.1.11 (test runner di default in 21, al posto di Karma/Jasmine).
- **Zoneless**: `app.config.ts` non contiene nessun provider di zone.js/`provideZoneChangeDetection`, quindi il progetto è zoneless (default in 21). Lo stato che si vede a schermo sta in `signal()`/`computed()`. L'utente conosce i signal.
- **Comandi**: `ng ...` da `FE\angular`; `.\mvnw.cmd ...` da `BE\spring` (nuovo percorso). Dopo lo spostamento del backend il Java language server di VS Code aveva perso i package: si risolve con la palette comandi → **Java: Clean Java Language Server Workspace** (restart and delete), riaprendo VS Code sulla cartella radice `Fumanji`.
- **Proxy**: `ng serve` legge `proxy.conf.json` solo all'avvio, quindi dopo modifiche a `proxy.conf.json`/`angular.json` va riavviato. Il backend va avviato prima, con MySQL acceso e board OG seedata sul DB `fumanji`.
- Nessuna verifica in sandbox nella sessione del 30-09-26: codice Angular e Spring verificati dall'utente in locale.

---

## 3. Stack tecnologico

| Aspetto | Scelta |
|---|---|
| Linguaggio/runtime | Java 21 |
| Build tool | Maven |
| Framework backend | **Spring Boot 4.1.1** — scheletro + persistenza JPA (24-09-26); layer applicativo e controller REST completi (28-09-26 → 30-09-26) |
| Packaging | **WAR** (deploy-ready su servlet container esterno; gestito da `ServletInitializer`, generato da Spring Initializr — vedi sezione 6) |
| Maven group/artifact/package | `com.project` / `fumanji` / `com.project.fumanji` |
| Database | MySQL, via Spring Data JPA (integrato il 24-09-26). Schema reale `fumanji`; schema di test `fumanji_test` (sezione 11) |
| Stile API | REST puro (**no WebSocket**) — endpoint `/api/games` completi (sezione 5, "API REST") |
| Frontend | **Angular 21.2.24** (TypeScript 5.9.3, Node 24.13.0, npm 11.6.2, rxjs 7.8.2, Vitest 4.1.11), avviato il 30-09-26, zoneless, proxy del dev server verso il backend (sezioni 2 e 5). Quando BE e FE saranno pronti, valutare **Electron** per le fasi successive |
| Test backend | JUnit + MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) contro lo schema `fumanji_test`; dipendenza test `spring-boot-starter-webmvc-test` (sezione 5, "Spring Boot 4.x — attenzione ai test") |
| Autenticazione | Non serve per la fase attuale (vedi sezione 1). Meccanismo (JWT vs sessione) non ancora deciso, ma la libreria **jjwt** è già nel pom |
| Altre dipendenze pom | Lombok, MapStruct, springdoc-openapi (Swagger, path custom `/swagger` e `/apidocs`, entrambi **disattivati** per ora), Spring Security (presente nel pom, **commentata**, "da attivare") |

---

## 4. Struttura cartelle e package

```
Fumanji/
├── BE/
│   └── spring/                                  (progetto Maven/Spring; era "BE spring" fino al 30-09-26; build e test verificati dal nuovo percorso)
│       ├── pom.xml                              (aggiunta dipendenza test spring-boot-starter-webmvc-test)
│       └── src/
│           ├── main/
│           │   ├── java/com/project/fumanji/
│           │   │   ├── FumanjiApplication.java
│           │   │   ├── ServletInitializer.java
│           │   │   ├── api/                     (package: com.project.fumanji.api)
│           │   │   │   └── GameController.java
│           │   │   ├── config/                  (package: com.project.fumanji.config)
│           │   │   │   └── GameConfig.java
│           │   │   ├── dto/                     (package: com.project.fumanji.dto — record)
│           │   │   │   ├── ChoiceRequest.java
│           │   │   │   ├── CreateGameRequest.java
│           │   │   │   ├── CreateGameResponse.java
│           │   │   │   ├── ErrorResponse.java
│           │   │   │   ├── GameStateResponse.java
│           │   │   │   ├── MoveRequest.java
│           │   │   │   └── PlayerStateResponse.java
│           │   │   ├── entity/                  (package: com.project.fumanji.entity — DOMINIO, Java puro)
│           │   │   │   ├── Board.java
│           │   │   │   ├── Dice.java
│           │   │   │   ├── Game.java            (esteso: fasi del turno, messaggi)
│           │   │   │   ├── InteractiveSquare.java
│           │   │   │   ├── Player.java
│           │   │   │   ├── Square.java
│           │   │   │   ├── TurnPhase.java
│           │   │   │   └── squares/             (package: com.project.fumanji.entity.squares)
│           │   │   │       ├── BivioSquare.java
│           │   │   │       ├── CheckpointSquare.java   (messaggio via Game.addMessage)
│           │   │   │       ├── DrinkSquare.java        (aggiunto getAmount)
│           │   │   │       ├── MoveBackSquare.java
│           │   │   │       ├── MoveForwardSquare.java
│           │   │   │       ├── ObligationSquare.java   (messaggio via Game.addMessage)
│           │   │   │       ├── PortalSquare.java       (aggiunto getLinkedPositions)
│           │   │   │       ├── RollAgainSquare.java
│           │   │   │       ├── SnackSquare.java        (aggiunto getAmount)
│           │   │   │       └── StartOverSquare.java
│           │   │   ├── exception/               (package: com.project.fumanji.exception)
│           │   │   │   ├── GameNotFoundException.java
│           │   │   │   └── GlobalExceptionHandler.java
│           │   │   ├── mapper/                  (package: com.project.fumanji.mapper)
│           │   │   │   └── GameStateAssembler.java
│           │   │   ├── persistence/             (package: com.project.fumanji.persistence — JPA, dominio separato)
│           │   │   │   ├── SquareEntity.java
│           │   │   │   ├── BoardEntity.java     (fetch EAGER su squares)
│           │   │   │   ├── EndGameEntity.java
│           │   │   │   ├── SquareMapper.java
│           │   │   │   ├── BoardAssembler.java
│           │   │   │   └── squares/             (package: com.project.fumanji.persistence.squares)
│           │   │   │       ├── BivioSquareEntity.java   (fetch EAGER su nextPositions/previousPositions)
│           │   │   │       ├── CheckpointSquareEntity.java
│           │   │   │       ├── DrinkSquareEntity.java
│           │   │   │       ├── MoveBackSquareEntity.java
│           │   │   │       ├── MoveForwardSquareEntity.java
│           │   │   │       ├── ObligationSquareEntity.java
│           │   │   │       ├── PortalSquareEntity.java  (fetch EAGER su linkedPositions)
│           │   │   │       ├── RollAgainSquareEntity.java
│           │   │   │       ├── SnackSquareEntity.java
│           │   │   │       └── StartOverSquareEntity.java
│           │   │   ├── repository/              (package: com.project.fumanji.repository)
│           │   │   │   ├── BoardRepository.java
│           │   │   │   └── EndGameRepository.java
│           │   │   └── service/                 (package: com.project.fumanji.service)
│           │   │       ├── EndGameService.java
│           │   │       ├── GameCreationResult.java
│           │   │       ├── GameRegistry.java
│           │   │       └── GameService.java
│           │   └── resources/
│           │       └── application.properties
│           └── test/
│               ├── java/com/project/fumanji/
│               │   ├── api/GameControllerIT.java
│               │   ├── entity/ControllableDice.java
│               │   └── persistence/BoardAssemblerIT.java
│               └── resources/
│                   ├── application-test.properties
│                   └── seed-og-board.sql
├── FE/
│   └── angular/
│       ├── proxy.conf.json
│       └── src/app/
│           ├── app.ts / app.html / app.css / app.config.ts / app.routes.ts / app.spec.ts
│           ├── models/
│           │   ├── create-game-response.ts
│           │   ├── game-state-response.ts
│           │   ├── player-state-response.ts
│           │   └── turn-phase.ts
│           ├── services/
│           │   └── game-api.ts
│           ├── welcome/            (.ts .html .css .spec.ts)
│           ├── player-setup/       (.ts .html .css .spec.ts)
│           ├── game-screen/        (.ts .html .css .spec.ts)
│           └── winner/             (.ts .html .css .spec.ts)
└── Documentazione/
```

Note:
- Il dominio (`entity`/`entity.squares`) è la stessa gerarchia Java pura delle sessioni precedenti, solo **spostata** sotto `com.project.fumanji` — verificato con `javac` diretto (nessuna dipendenza Spring necessaria per queste classi).
- Il package `demo` (`GameDemo1..5`) **non fa più parte dello stato corrente** — nessuna demo lo sostituisce per ora.
- `persistence` è un package **introdotto il 24-09-26**: entity JPA separate dal dominio (vedi sezione 5 per il perché), stessa suddivisione `squares` a specchio del dominio.
- I metodi "di orchestrazione" di `Game` restano **public** per convenzione consolidata (visibilità ampia/future-proof). Eccezioni mirate per vero incapsulamento (`Player.setPosition`/`setCheckpoint`, package-private).
- Convenzione di package che l'utente segue di solito (vista in un progetto passato, EventHub): `api`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, adattata caso per caso. Per Fumanji, oltre a `entity`/`entity.squares` (dominio) e `persistence`/`persistence.squares` (JPA): `repository` (interfacce `JpaRepository`), `service` (`GameRegistry`, `GameService`, `EndGameService`, `GameCreationResult`), `api` (controller), `dto` (record), `exception`, `mapper` (`GameStateAssembler`, scritto a mano come `BoardAssembler`, non MapStruct: non è mappatura dichiarativa pura), `config` (`GameConfig`).
- I DTO sono `record` (contenitori dati immutabili per JSON, accessor `campo()` invece di `getCampo()`, Jackson li gestisce nativamente).
- I file generati dalla CLI 21 non hanno il suffisso `.component` (es. `welcome.ts`, classe `Welcome`; anche `app.ts` con classe `App`).
- Convenzione per i modelli frontend: un'interfaccia per file, in `models/`, che rispecchia i DTO del backend (`dto`). Per il service: cartella `services/`, classe `GameApi`.
- `tsconfig.app.json` modificato dall'utente perché VS Code segnalava errori: contiene `"rootDir": "./src"` insieme a `outDir`, `types: []`, `include: src/**/*.ts`, `exclude: src/**/*.spec.ts`.

---

## 5. Decisioni di design e logica di dominio

### Board — builder, completamente immutabile, con il flag `backwardAllowedAfterCheckpoint` (24-09-26)

- Campi `finishPosition`, `squares`, `directionChoiceEnabled`, `mandatoryCheckpointEnabled`, **`backwardAllowedAfterCheckpoint`** (24-09-26): tutti **final**. `Board` è completamente immutabile una volta costruita.
- **Due modalità di costruzione**, invariate nel principio:
  1. **`Board(int size)`** — costruttore pubblico invariato: board "vuota", regole disattivate (`backwardAllowedAfterCheckpoint` di default `true` = nessun blocco). **Resta pubblico deliberatamente**, costo basso di renderlo privato più avanti.
  2. **`Board.builder(size)`** — pattern Builder fluente:
     ```java
     Board board = Board.builder(110)
         .square(6, new BivioSquare(6, List.of(10, 40), List.of(5), new SnackSquare(0, 1)))
         .directionChoiceEnabled(true)
         .mandatoryCheckpointEnabled(true)
         .backwardAllowedAfterCheckpoint(false)
         .build();
     ```
     - `.square(position, square)`: valida `1 <= position <= finishPosition - 1`, altrimenti `IllegalArgumentException`.
     - `.directionChoiceEnabled(boolean)` / `.mandatoryCheckpointEnabled(boolean)` / **`.backwardAllowedAfterCheckpoint(boolean)`** (24-09-26): uniche vie per impostare le regole.
- `getSquares()` ritorna lista immutabile (`List.copyOf`).

### Nuova regola — `backwardAllowedAfterCheckpoint`

- Introdotta il 24-09-26 per la board OG, che richiede: una volta raggiunto il checkpoint, non si può più tornare indietro **oltre la posizione del checkpoint stesso** (si può ancora scegliere "indietro", ma il movimento non supera mai quel punto).
- **Attenzione alla distinzione**: non è "non puoi più scegliere indietro dopo il checkpoint" (quello avrebbe richiesto modificare `moveByChosenDirection`) — è "il movimento all'indietro non supera mai la posizione del checkpoint", quindi la regola vive in `moveBackwardByInternal`, come **secondo pavimento** accanto a quello fisso su 0:
  ```java
  private void moveBackwardByInternal(Player player, int steps, boolean respectCheckpoint) {
      int remaining = steps;
      while (remaining > 0) {
          if (player.getPosition() <= 0) {
              break;
          }
          if (!board.isBackwardAllowedAfterCheckpoint() && player.getPosition() <= player.getCheckpoint()) {
              break;
          }
          // ... resto invariato
      }
      doLandOnCurrentSquare(player);
  }
  ```
- **Perché serve un pavimento indipendente dal fermo obbligatorio (`mandatoryCheckpointEnabled`)**: quel meccanismo ferma il giocatore solo quando **arriva** sul checkpoint venendo da più avanti. Se il giocatore è **già** esattamente sul checkpoint (ci è appena atterrato, o riparte da lì un turno successivo) e sceglie indietro, quel controllo non scatta mai (non sta arrivando, sta partendo) — da qui il buco che il nuovo pavimento chiude, controllando la posizione **attuale** ad ogni passo invece che solo l'arrivo.
- **Effetto collaterale positivo della posizione scelta** (dentro `moveBackwardByInternal` e non in `moveByChosenDirection`): questo è l'unico punto in cui convergono sia la scelta volontaria del giocatore, sia `MoveBackSquare.applyEffect` (via `moveBackwardBy`), sia la ripresa dopo un bivio (`chooseDestination`) — quindi qualunque cosa spinga il giocatore indietro, volontaria o forzata da una casella, rispetta comunque il limite.
- `true` (default) = comportamento libero, nessun blocco; `false` = dopo il checkpoint si accetta solo di restare a quella posizione o andare avanti.
- `moveByChosenDirection` **non è stato toccato** — resta esattamente come nelle sessioni precedenti (fallback silenzioso ad avanti se `directionChoiceEnabled` è `false`).

### Game — invariato nella struttura il 24-09-26 (un solo metodo toccato); esteso poi il 28-09-26 e il 30-09-26

- `players` chiusa con `List.copyOf` nel costruttore (sessione precedente, invariato).
- Guardrail anti-loop (`activatedPositions`/`chainInProgress`), movement trace, meccanismo bivio/portale unificato (`isAwaitingChoice`/`getChoiceOptions`/`chooseDestination`/`requestChoice`): tutto invariato rispetto alle sessioni precedenti.
- L'unico cambiamento del 24-09-26 è in `moveBackwardByInternal`, descritto sopra.
- Estensioni successive (28-09-26 e 30-09-26): fasi del turno con i metodi `performRoll/Move/Choice` (vedi "Fasi del turno") e lista dei messaggi del turno (vedi "Messaggi delle caselle"). `roll`, `rollAndMove`, `moveByChosenDirection`, `chooseDestination` e il motore interno non sono stati toccati.

### Dado — invariato
- 1 dado per turno, valori 1-6, `ThreadLocalRandom`.

### Checkpoint — invariato (fermo simmetrico avanti/indietro, sessione precedente)
- Forward e backward si fermano sulla prima casella `isMandatoryStop()` incontrata quando `mandatoryCheckpointEnabled` è attivo. Il proprio checkpoint si aggiorna sempre all'ultimo su cui si è atterrati (`CheckpointSquare.applyEffect`).
- Dal 30-09-26 il messaggio del checkpoint non viene più stampato su console ma aggiunto a `Game` con `addMessage` (vedi "Messaggi delle caselle").
- **Nuovo dettaglio di comportamento per la board OG** (non codice, conseguenza dei dati): il checkpoint di questa board è unico, quindi `player.getCheckpoint() > 0` identifica in modo affidabile "il giocatore l'ha già raggiunto" — soluzione cucita su "un solo checkpoint", da rivedere se una board futura ne avesse più di uno in sequenza.

### Direction choice — invariato
- `Game.roll`, `Game.rollAndMove`, `Game.moveByChosenDirection` invariati, fallback silenzioso ad avanti se la regola non è attiva.

### Caselle interattive (`InteractiveSquare`, Bivio, Portale) — invariato nel meccanismo, `BivioSquare` esteso

- Meccanismo di innesco (Bivio a metà mossa, Portale solo su atterraggio esatto) e risoluzione unificata in `Game`: invariati.
- **`BivioSquare` — nuovo campo opzionale `effect`** (24-09-26): un `Square` "delegato" usato **solo** per il suo `applyEffect`, mai per posizione/routing. Nasce dall'esigenza della board OG, dove alcuni bivi (visti sul tabellone fisico) attivano anche un obbligo/snack/drink/startover oltre a far scegliere la direzione — cosa che la struttura precedente non supportava (`BivioSquare` non sovrascriveva mai `applyEffect`).
  ```java
  public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions) {
      this(position, nextPositions, previousPositions, null);
  }

  public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions, Square effect) {
      super(position);
      this.nextPositions = nextPositions;
      this.previousPositions = previousPositions;
      this.effect = effect;
  }

  @Override
  public void applyEffect(Player player, Game game) {
      if (effect != null) {
          effect.applyEffect(player, game);
      }
  }
  ```
  Costruttore a 3 argomenti invariato (delega a quello a 4 con `effect = null`) — retrocompatibile con tutto il codice esistente che non usa l'effetto.
- **Bivio "di instradamento" (routing-only)**: lo stesso meccanismo usato nelle sessioni precedenti per i punti di ricongiungimento (es. `GameDemo5`, casella 45) — un `BivioSquare` con **un solo elemento** in `nextPositions` non fa scattare nessuna scelta per il giocatore (`Game` controlla `options.size() > 1`), quindi si comporta in modo silenzioso: cambia solo la destinazione di un "dritto" che altrimenti sarebbe stato `position + 1`. Usato pesantemente nella board OG per collegare rami con numerazione non adiacente (vedi sezione 9).
- Conferma da sessioni precedenti: le "limitazioni" del Portale erano già risolte — ogni istanza ha la propria lista esplicita di collegamenti (`linkedPositions`).

### Meccanica "a catena" — invariata
- Dopo ogni spostamento si ricontrolla la casella di arrivo: vittoria se è l'ultima, altrimenti si applica l'effetto lì, soggetta al guardrail anti-loop. Eccezioni invariate: `StartOverSquare` (commentata di proposito), `SnackSquare`/`DrinkSquare`/`ObligationSquare`/`CheckpointSquare` (non spostano il giocatore), `PortalSquare` (esplicito, "niente di niente").

### Obbligo — `ObligationSquare`, messaggio ora parametrico per istanza

- **Prima** (sessioni precedenti): un solo messaggio hardcoded, uguale per ogni istanza — non reggeva più con 6 testi diversi richiesti dalla board OG (obbligo generico, turca, crea, 2 tiri, bong, giro bong), alcuni dei quali **non riguardano il giocatore di turno** (es. "turca" e "giro bong" sono rivolti a tutti).
- **Ora**: il messaggio è un parametro del costruttore, con placeholder `{player}` sostituito solo se presente (i messaggi "per tutti" semplicemente non lo contengono):
  ```java
  public class ObligationSquare extends Square {

      private final String message;

      public ObligationSquare(int position, String message) {
          super(position);
          this.message = message;
      }

      @Override
      public void applyEffect(Player player, Game game) {
          game.addMessage(buildMessage(player, game));
      }

      public String buildMessage(Player player, Game game) {
          return message.replace("{player}", player.getName());
      }
  }
  ```
- **Effetto collaterale accettato**: `GameDemo5` (che chiamava `new ObligationSquare(13)` senza messaggio) non compila più — non è stata aggiornata, coerente con la filosofia "usa e getta" delle demo.
- `CheckpointSquare` resta invariata — nessun template, messaggio fisso già confermato nelle sessioni precedenti.
- **Dal 30-09-26** sia `ObligationSquare` sia `CheckpointSquare` non stampano più su console (`System.out.println`): aggiungono il messaggio alla lista dei messaggi del turno di `Game` con `game.addMessage(...)`, così arriva al frontend (vedi "Messaggi delle caselle"). Il codice qui sopra è già la versione aggiornata.
- I 6 template concreti usati sulla board OG sono elencati in sezione 9.

### addSnack / addDrink — invariati
- `Game.addSnack(player, amount)` / `Game.addDrink(player, amount)`, clamp a 0, nessuna variante "per tutti i giocatori" (il chiamante itera su `game.getPlayers()` se serve). Pensati per essere richiamati direttamente dal futuro FE.

### Persistenza JPA — dominio separato (scelta del 24-09-26)

- **Il problema**: annotare direttamente `Square`/`Board` (le classi di dominio) con JPA confligge con l'immutabilità costruita apposta nelle sessioni precedenti (builder, campi `final`, niente setter). Il punto delicato non sono i campi singoli (Hibernate li popola comunque via reflection con un costruttore no-arg protetto) ma le **collection**: `Board.squares` è una `List.copyOf(...)` immutabile assegnata una volta sola — con `@OneToMany` Hibernate ha bisogno di gestire lui la collection (lazy loading, dirty checking), cosa che un campo `final` puntato a una lista immutabile non permette in modo pulito. Stesso discorso per le `List<Integer>` di `BivioSquare`/`PortalSquare` mappate con `@ElementCollection`.
- **Due strade valutate**:
  - **A — stesse classi, meno immutabili**: le collection diventano mutabili, più un costruttore no-arg protetto. Nessuna classe nuova, ma il refactor di immutabilità di `Board` fatto nelle sessioni precedenti va parzialmente ammorbidito.
  - **B — dominio separato da JPA** (scelta): `Square`/`Board`/le 10 sottoclassi **restano esattamente come sono**, zero compromessi. Si aggiunge una gerarchia di entity JPA a parte (package `persistence`), con un mapper MapStruct per convertire in dominio. **Motivazione esplicita di Salame**: è anche il passo già previsto in futuro per il factory pattern delle caselle (JSON `type` → istanza `Square`), quindi tanto vale prepararlo da subito.
- **Struttura scelta**: `SquareEntity` **abstract** (base, `@Inheritance(JOINED)`, discriminator `square_type`) — abstract perché non esiste mai una riga "casella normale" in tabella (l'assenza di riga *è* l'informazione "casella base", stesso principio del builder che sovrascrive solo le caselle speciali). 10 sottoclassi entity a specchio del dominio. `BoardEntity` con `@OneToMany(mappedBy="board", cascade=ALL, orphanRemoval=true)`.
- **Assemblaggio Board↔BoardEntity**: non è mappatura dichiarativa pura (ricostruire `Board` richiede passare dal builder), quindi vive in una **classe separata** (`BoardAssembler`, non un `default` method dentro l'interfaccia MapStruct) — scelta esplicita di Salame, per tenere nette le interfacce MapStruct (`SquareMapper`) come puramente dichiarative.
- **Mapping delle sottoclassi** (`SquareMapper`, `@SubclassMapping` per ognuna delle 10): MapStruct usa l'unico costruttore di ogni classe dominio e abbina i parametri per nome ai getter dell'entity corrispondente — funziona perché dominio ed entity sono compilati nello stesso modulo (annotation processing legge i nomi reali dal sorgente, non serve il flag `-parameters`).
- **Warning innocuo noto**: MapStruct segnala "Unmapped target properties: nextPositions, previousPositions" per le 9 sottoclassi che non sono `BivioSquare`, perché `Square.getNextPositions()`/`getPreviousPositions()` sono metodi pubblici definiti sulla classe base (calcolano `position±1` di default) ed ereditati da tutte le sottoclassi — MapStruct li vede come "proprietà" leggibili anche dove non c'è nulla da mappare. Il codice generato resta corretto (quei getter continuano a calcolare da `position` come sempre). Silenziabile con `unmappedTargetPolicy = ReportingPolicy.IGNORE` sul `@Mapper` (facoltativo, mai applicato).
- **`BivioSquare.effect` in persistenza**: `BivioSquareEntity` ha un `@ManyToOne(cascade = CascadeType.ALL) effect` verso un'altra riga di `SquareEntity`. Quella riga "effetto" **non è posizionata su nessuna board** (`board_id = NULL`, quindi mai vista da `BoardAssembler`/dalla collection `@OneToMany` di `Board`) — per questo `square_entity.board_id` è **nullable** (non più `NOT NULL` come alla primissima stesura). Il cascade `ALL` sulla relazione `effect` garantisce che, cancellando un `BivioSquareEntity` (che a sua volta viene cancellato in cascata insieme alla sua board), venga cancellata anche la sua riga-effetto — altrimenti resterebbe orfana. **Convenzione id/posizione per queste righe**: `id = 1000 + posizione del bivio proprietario` (es. `1006` = effetto del bivio in posizione 6), `position = 0` come placeholder inerte (mai letto per queste righe).

### Debiti tecnici noti (aggiornato al 30-09-26)
- `playTurn()` non gestisce la pausa di `isAwaitingChoice()` e non passa dalle fasi del turno (`TurnPhase`): il flusso "vero" lato API è stato risolto con `performRoll/Move/Choice` + `TurnPhase` (28-09-26). `playTurn()` è stato **mantenuto** per decisione del 29-09-26, per un futuro utilizzo non ancora definito; nessun endpoint lo usa (vedi "playTurn()").
- Guardrail anti-loop: limite noto con pausa Bivio a metà catena (sessioni precedenti) — accettato.
- **Nuovo limite noto**: la regola `backwardAllowedAfterCheckpoint` vive in `moveBackwardByInternal`, quindi copre sia la scelta volontaria sia le caselle che spingono indietro con la forza (`MoveBackSquare`) — ma **solo** quelle. Non esiste, e per ora non serve, un controllo analogo lato "avanti" (non avrebbe senso: non si può "superare" il checkpoint andando avanti, lo si raggiunge e basta).

**Decisioni sul backend applicativo e sull'API (sessioni 28-09-26 → 30-09-26)**

### Persistenza — fetch EAGER (da rivalutare)
- Causa: `BoardAssembler.toDomain()` legge le collection fuori da una sessione Hibernate → `LazyInitializationException` (`@OneToMany` e `@ElementCollection` sono LAZY di default).
- Scelta: `fetch = FetchType.EAGER` su `BoardEntity.squares`, `BivioSquareEntity.nextPositions`, `BivioSquareEntity.previousPositions`, `PortalSquareEntity.linkedPositions`. `BivioSquareEntity.effect` (`@ManyToOne`) è EAGER per default JPA, quindi non toccato.
- Alternative valutate e non scelte: `@Transactional` su ogni lettura; `JOIN FETCH`/`@EntityGraph`. **Da rivalutare quando si lavorerà su più board.**

### Stato di `Game` tra le richieste REST
- Durante la partita `Game` vive **in memoria**: `Map<UUID, Game>` in un bean singleton (`GameRegistry`, `ConcurrentHashMap` perché le richieste REST arrivano su thread diversi).
- Nessuna persistenza per mossa. Motivo: `Game`/`Player` sono dominio puro con stato "in volo" (`awaitingOptions`, ecc.); persisterli richiederebbe una gerarchia JPA "live".
- **Rischio accettato per ora**: un riavvio/crash del server a metà partita perde quella partita (uso locale su singolo dispositivo).
- **TTL** (rimozione delle partite abbandonate via `@Scheduled` su timestamp di ultima attività): non implementato, voluto come miglioramento futuro prossimo. Ridiscusso il 29-09-26 e **confermato rimandato**: nessun deploy con uptime lungo o utenti concorrenti reali per ora, il rischio non si manifesta ancora.
- La costruzione di un `Game` è responsabilità di `GameService`, non del registro (il registro è un puro contenitore).

### Fine partita
- Alla vittoria: `GameService` salva un `EndGameEntity` (nome del vincitore + board giocata) tramite `EndGameService`, poi **rimuove la partita dal `GameRegistry`**. Richieste successive su quel `gameId` → `404`; il vincitore si legge dalla risposta della mossa vincente (`winnerName`).
- Scelta delegata dall'utente a Claude puntando al massimo future proofing: un unico punto di chiusura dove in futuro salvare **tutti** i dati della partita per analisi; `EndGameService.save` riceve l'intero `Game` (oggi usa solo il vincitore).
- Per ora l'utente non è interessato ai dati della partita: gli basta sapere che tutto ha funzionato.
- Board salvata: `OG_BOARD_ID = 1L` fisso (stessa costante di `createGame`), da rivedere con le board multiple.
- Storico completo/statistiche (traccia movimenti **per giocatore**, statistiche): rimandato. Nota: `Game.movementTrace` oggi è una lista unica di posizioni, non attribuita al giocatore; le statistiche da tracciare non sono ancora definite.

### Ordine dei turni
- L'ordine di gioco è semplicemente l'ordine in cui arrivano i nomi in `playerNames` nella richiesta di creazione: lo decidono i giocatori. Il backend lo preserva e non ha logica di riordino.
- Lato frontend, in futuro: far impostare l'ordine esplicitamente ("Giocatore 1", "Giocatore 2"… dove i giocatori scrivono il nome di chi inizia, poi chi è secondo, ecc.).

### Fasi del turno (`TurnPhase`) e metodi di turno in `Game`
- Il server impedisce le azioni fuori sequenza: fasi `WAITING_ROLL` → `WAITING_DIRECTION` → (`WAITING_CHOICE` se c'è una scelta a bivio/portale) → prossimo giocatore, `WAITING_ROLL`.
- Motivo: senza fasi un client potrebbe chiamare `move` senza tirare, tirare due volte, o tirare con una scelta pendente (un doppio click sul frontend basterebbe).
- Metodi pubblici di turno, tutti `synchronized`: `performRoll()`, `performMove(boolean forward)`, `performChoice(int chosenPosition)`. `synchronized` perché altrimenti due richieste concorrenti (doppio click) potrebbero superare entrambe il controllo di fase.
- **`roll`, `moveByChosenDirection`, `chooseDestination` NON sono stati toccati**: le caselle li usano internamente (es. `RollAgainSquare` chiama `rollAndMove` → `roll`) e un controllo di fase lì le romperebbe.
- Il turno avanza (`resolveTurnPhase`) quando il movimento è concluso e non c'è nessuna scelta pendente; se c'è un vincitore non avanza e non cambia fase.
- Dopo la vittoria `getTurnPhase()` resta sull'ultima fase: la fine partita si legge da `getWinner()`. Qualunque azione dopo la vittoria lancia `IllegalStateException("Game is already over")`.
- `RollAgainSquare` a catena dentro `performMove` funziona (verificato).
- `playTurn()` resta com'è e **non passa dalle fasi**; nessun endpoint lo usa. Deciso il 29-09-26 di mantenerlo (vedi "playTurn()" più sotto).
- Nomi dei metodi (`performRoll/Move/Choice`) approvati dall'utente.
- Dal 30-09-26 `performRoll()` svuota anche i messaggi del turno, subito dopo il controllo di fase (vedi "Messaggi delle caselle").

### API REST
- `POST /api/games` — body `{"playerNames": [...]}` (validato: lista non vuota, nomi non blank) → `CreateGameResponse{gameId, currentPlayerName}`.
- `POST /api/games/{gameId}/roll` — tira il dado.
- `POST /api/games/{gameId}/move` — body `{"forward": true|false}`; con `directionChoiceEnabled = false` il `false` ricade silenziosamente su "avanti" (comportamento invariato del dominio).
- `POST /api/games/{gameId}/choice` — body `{"position": n}`; risolve la scelta a bivio/portale.
- `GET /api/games/{gameId}` — stato corrente (per rileggerlo, es. dopo un refresh del frontend).
- Roll, move, choice e GET rispondono tutti con lo stesso `GameStateResponse`: giocatore di turno, fase, ultimo tiro, stato di tutti i giocatori, `choiceOptions` (solo se c'è una scelta pendente), `winnerName` (solo se c'è un vincitore), `messages` (solo se ci sono messaggi delle caselle, dal 30-09-26). Con `spring.jackson.default-property-inclusion=non_null` (già presente in `application.properties`) i campi nulli non compaiono nel JSON.
- Errori (`GlobalExceptionHandler`): `gameId` sconosciuto → `404`; azione fuori fase (`IllegalStateException`) → `409`; scelta non valida (`IllegalArgumentException`) o body non valido (validazione) → `400`. Nota accettata: gli handler su `IllegalStateException`/`IllegalArgumentException` sono ampi, un bug interno che le lancia diventerebbe `409`/`400` invece di `500`; accettato perché il dominio usa già solo queste eccezioni standard.
- I request DTO usano `Boolean`/`Integer` con `@NotNull` (non primitivi): con `boolean` un body `{}` diventerebbe silenziosamente `forward = false`.

### Test di integrazione
- Schema dedicato `fumanji_test` (da creare a mano una volta, stesso server MySQL), profilo `test`, `ddl-auto=create-drop`.
- Seed via `@Sql("/seed-og-board.sql")` (`src/test/resources`, copia identica dello script di seed della board OG, sezione 9).
- **`@Sql` di default gira prima di OGNI metodo di test**: con più `@Test` nella stessa classe il seed verrebbe rieseguito sullo stesso schema → violazione della chiave primaria (`ScriptStatementFailedException` sulla prima `INSERT`). Soluzione adottata in **entrambe** le classi (`GameControllerIT` e `BoardAssemblerIT`): `executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS` (disponibile da Spring Framework 6.1).
- Il dado nei test è il bean `ControllableDice` (vedi "Dice iniettabile"): il tiro è fissabile a runtime, quindi sono coperti anche gli scenari che dipendono dal risultato (scelta valida a bivio con prosecuzione dei passi residui, scelta non valida → 400, messaggi delle caselle). Nei test che non fissano il tiro, `@BeforeEach` lo resetta a 1 (isolamento fra metodi).
- Il test di fine partita registra direttamente in `GameRegistry` una partita piccola (`new Board(5)`, dado fissato a 5) e la chiude passando dagli endpoint veri.
- `BoardAssemblerIT` contiene sia lo smoke test iniziale sia la regressione completa della board OG (vedi "Regressione completa BoardAssemblerIT").

### Spring Boot 4.x — attenzione ai test (scoperto in sessione)
- `@AutoConfigureMockMvc`/`@WebMvcTest` sono passati da `org.springframework.boot.test.autoconfigure.web.servlet` a **`org.springframework.boot.webmvc.test.autoconfigure`** e richiedono la dipendenza test **`spring-boot-starter-webmvc-test`** (non più inclusa in `spring-boot-starter-test`). Aggiunta al pom.
- `TestRestTemplate` è passato in un altro modulo (`org.springframework.boot.resttestclient`, richiede `@AutoConfigureTestRestTemplate`): non usato in questo progetto.
- Il jar `spring-boot-test-autoconfigure-4.1.1` di ~27 KB nel repository Maven locale era **corretto** (non corrotto): è piccolo perché la parte web è stata spostata fuori.

### Dice iniettabile
- Nuovo bean Spring per `Dice` invece di `@Component` diretto sulla classe: `Dice` resta pure Java, senza annotazioni, coerente col dominio puro (stesso principio della persistenza separata). Bean dichiarato in un nuovo `GameConfig` (`@Configuration`, `@Bean Dice dice()`).
- `GameService` riceve un'unica istanza `Dice` condivisa via costruttore, al posto di `new Dice()` per ogni partita creata in `createGame` (`Dice` è stateless — `ThreadLocalRandom.current()` — condivisione sicura, tiro sempre casuale in produzione).
- Nei test (`GameControllerIT`), il bean di produzione viene sostituito da `ControllableDice` (`extends Dice`, `setNextRoll(int)` impostabile a runtime) tramite un `@TestConfiguration` + `@Primary` interno alla classe di test. Sostituisce il precedente `FixedDice` a valore fisso nel costruttore.
- **Gotcha Spring Boot scoperto in sessione**: un bean di test con lo stesso nome (= nome del metodo `@Bean`) di un bean di produzione fallisce la registrazione (`BeanDefinitionOverrideException`) prima che `@Primary` possa intervenire — `@Primary` risolve solo l'ambiguità fra più bean candidati per tipo, non un conflitto di nome. Fix: rinominato il bean di test da `dice()` a `testDice()`. Il campo `@Autowired ControllableDice dice` nel test resta risolto per tipo, il nome del bean non conta ai fini dell'injection.
- `@BeforeEach` aggiunto in `GameControllerIT` per resettare il dado a un valore neutro prima di ogni test (isolamento fra metodi, dato che il bean è condiviso nel contesto Spring cachato).
- Sbloccati due scenari HTTP prima non testabili: scelta valida a bivio (con prosecuzione dei passi residui) e scelta non valida (400).

### playTurn()
- Deciso di mantenerlo: non passa dalle fasi (`TurnPhase`) e nessun endpoint lo usa oggi, ma resta per un futuro utilizzo non ancora definito.

### Regressione completa BoardAssemblerIT
- Con un secondo `@Test` nella classe, serviva la stessa correzione già nota da `GameControllerIT`: `@Sql` passato a `executionPhase = BEFORE_TEST_CLASS` (il default riesegue il seed prima di ogni metodo, violando la chiave primaria dal secondo test in poi).
- Nuovo test che copre: tipo esatto di tutte le 68 caselle piazzate + le caselle base attese (partenza, arrivo, i 6 gap "no smoke"); `next`/`previous` di tutti i 10 bivi; collegamenti dei 6 portali (ognuno verso le altre 5 posizioni); `amount` di tutte le caselle Snack/Drink.
- Aggiunti getter pubblici mancanti per coerenza con `BivioSquare` (che espone già `getNextPositions()`/`getPreviousPositions()` pubblici, nessuna copia difensiva): `PortalSquare.getLinkedPositions()`, `SnackSquare.getAmount()`, `DrinkSquare.getAmount()`.

### Messaggi delle caselle (obblighi e checkpoint) — 30-09-26
- **Problema**: `ObligationSquare` (e `CheckpointSquare`) stampavano il messaggio solo su console, quindi il frontend non lo riceveva.
- **Piano approvato**: `Game` tiene la lista dei messaggi del turno; `ObligationSquare` e `CheckpointSquare` li aggiungono lì invece di stampare su console; l'API li espone in `GameStateResponse.messages` (campo omesso quando non ci sono messaggi, coerente con `default-property-inclusion=non_null`).
- **Lista e non stringa singola**: il motore permette catene di caselle (es. `RollAgainSquare`), quindi un'azione può in teoria produrre più messaggi.
- **Durata**: i messaggi restano visibili **fino al tiro successivo**. La lista viene svuotata all'inizio di `performRoll()` (dopo il controllo di fase, così un `roll` fuori fase non cancella nulla). `roll()`, `moveByChosenDirection` e `chooseDestination` non sono stati toccati.
- Il messaggio del checkpoint segue lo stesso meccanismo; il bivio con effetto (`BivioSquare.effect`) funziona senza modifiche perché delega a `applyEffect`.
- `addMessage` e `getTurnMessages` sono `synchronized`: il `GET` legge la lista mentre un `roll`/`move` può scriverla, e i metodi di turno usano già lo stesso lock.
- Il messaggio si costruisce lato server come stringa (`buildMessage` invariato, placeholder `{player}` sostituito col nome).


**Decisioni sul frontend Angular (30-09-26)**

### Frontend — Routing e flusso
- Navigazione con il **router Angular**, per poter ricaricare la pagina senza perdere la partita (il `GET /api/games/{gameId}` serve a questo).
- Rotte: `''` (benvenuto), `players` (nomi), `game/:gameId` (partita), `winner` (vincitore).
- Flusso: benvenuto → nomi → (all'invio) creazione partita sul backend → `/game/:gameId` → alla vittoria `/winner`.

### Frontend — Collegamento al backend: proxy
- Scelto il **proxy del dev server Angular** (`proxy.conf.json`, prassi abituale dell'utente) invece del CORS in Spring: frontend su `localhost:4200`, backend su `8080`, il frontend chiama percorsi relativi `/api/...`.
- Il proxy vale solo per `ng serve`: come verrà servito il frontend in produzione non è ancora deciso.

### Frontend — Schermata di benvenuto
- Click ovunque nella pagina per proseguire (contenitore `position: fixed; inset: 0` con `(click)`). Estetica non prioritaria: per ora conta solo che funzioni.

### Frontend — Schermata dei nomi (`player-setup`)
- Stato con signal + `(input)`, senza moduli form.
- All'apertura **2 box**; **minimo 2** e **massimo 10** giocatori (limiti solo nel frontend: il backend accetta qualsiasi lista non vuota); pulsante per aggiungere box e pulsante "Rimuovi" per box.
- L'ordine dei box è l'ordine di gioco.
- "Inizia" disabilitato finché un nome è vuoto/blank (il backend risponderebbe `400`) e mentre una richiesta è in corso (evita partite doppie con click ripetuti). I nomi vengono `trim()`-ati prima dell'invio.
- **Nomi doppi non bloccati**: l'uso previsto è dell'utente e di pochi amici fidati, si conta sull'intelligenza di chi usa l'app purché non si rompa. L'utente proverà a romperla in ogni modo più avanti, per sistemarla e migliorarla insieme.
- In caso di errore alla creazione, messaggio provvisorio sotto i pulsanti.

### Frontend — Schermata di gioco (`game-screen`)
- Lo stato (`GameStateResponse`) sta in un signal; viene letto con il GET in `ngOnInit`, anche dopo un refresh.
- Azioni del turno guidate dalla fase (`turnPhase`) che arriva dal backend, con `@switch`:
  - `WAITING_ROLL` → "Tira il dado" (`POST /roll`)
  - `WAITING_DIRECTION` → "Avanti" / "Indietro" (`POST /move`)
  - `WAITING_CHOICE` → un pulsante per ogni posizione in `choiceOptions` (`POST /choice`)
- Ogni risposta sostituisce lo stato mostrato. `isBusy` disabilita i pulsanti durante una richiesta e fa ignorare i click ripetuti.
- Errori HTTP tradotti in messaggi per il giocatore: `404` "Partita non trovata.", `409` "Azione non consentita in questa fase del turno.", `400` "Richiesta non valida.", altro "Errore di comunicazione con il server."
- `@for` sulle righe dei giocatori con `track $index` (i nomi doppi sono ammessi, quindi il nome non è una chiave univoca).
- Provvisori: la fase è mostrata come valore grezzo (`WAITING_ROLL`, ecc.) e tutti i testi dell'interfaccia.
- Alla vittoria (`winnerName` nella risposta) si naviga a `/winner`. Il ramo "vincitore" precedentemente nel template è stato rimosso.

### Frontend — Schermata del vincitore (`winner`)
- Mostra "Ha vinto X!" e un link "Nuova partita" verso `/players`.
- Il nome arriva con lo **stato della navigazione del router** (`state: { winnerName }`) e si legge da `history.state`. Motivo: alla vittoria il backend toglie la partita dal registro, quindi il vincitore non è più rileggibile con il `GET`. Aperta direttamente senza nome, la schermata mostra "Nessun vincitore da mostrare."
- Il comportamento al refresh della pagina `/winner` non è stato verificato esplicitamente.
- Per ora basta questo: altro (per esempio le posizioni finali) si aggiungerà dopo.

---

## 6. Codice backend — versione aggiornata (include le modifiche fino al 30-09-26)

Il dominio (`entity`/`entity.squares`) è stato verificato con `javac` diretto in sandbox dopo lo spostamento di package (nessuna rottura). Il codice Spring/JPA/MapStruct **non è compilabile in sandbox** (niente Maven Central) — scritto con cura ma la verifica reale è quella fatta da Salame in locale (schema Hibernate generato con successo, nessun errore di compilazione riportato). Le classi toccate dal 28-09-26 al 30-09-26 sono riportate qui già nella versione finale; i file nuovi di quel periodo (`TurnPhase`, `EndGameEntity`, repository, `config`, `service`, `dto`, `exception`, `mapper`, `api`) sono in fondo alla sezione. Il codice frontend è in sezione 10, i test in sezione 11.

### `pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.1.1</version>
		<relativePath/>
	</parent>
	<groupId>com.project</groupId>
	<artifactId>fumanji</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>war</packaging>

	<properties>
		<java.version>21</java.version>
		<org.mapstruct.version>1.6.3</org.mapstruct.version>
		<lombok.version>1.18.36</lombok.version>
		<lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
		<jjwt.version>0.11.5</jjwt.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<!-- SECURITY da attivare
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		-->
		<!-- REST API - questa scommentala solo se serve
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-rest</artifactId>
		</dependency>
		-->
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>3.0.3</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.mapstruct</groupId>
			<artifactId>mapstruct</artifactId>
			<version>${org.mapstruct.version}</version>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>${lombok.version}</version>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-api</artifactId>
			<version>${jjwt.version}</version>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-impl</artifactId>
			<version>${jjwt.version}</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-jackson</artifactId>
			<version>${jjwt.version}</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.13.0</version>
				<configuration>
					<source>${java.version}</source>
					<target>${java.version}</target>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
							<version>${lombok.version}</version>
						</path>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok-mapstruct-binding</artifactId>
							<version>${lombok-mapstruct-binding.version}</version>
						</path>
						<path>
							<groupId>org.mapstruct</groupId>
							<artifactId>mapstruct-processor</artifactId>
							<version>${org.mapstruct.version}</version>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```

### `FumanjiApplication.java`
```java
package com.project.fumanji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FumanjiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FumanjiApplication.class, args);
    }

}
```

### `ServletInitializer.java`
*(generato da Spring Initializr per il packaging WAR — attivo solo su deploy in un servlet container esterno, codice morto quando si lancia da `main()`/IDE)*
```java
package com.project.fumanji;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(FumanjiApplication.class);
	}

}
```

### `application.properties`
```properties
spring.application.name=fumanji

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/fumanji?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate settings for automatic schema creation
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Spring Data REST properties: per le rest api
# spring.data.rest.base-path=/api

# custom path per swagger
springdoc.swagger-ui.path=/swagger
springdoc.swagger-ui.enabled=false

# custom path per API docs (così per il json, con .yaml per lo yaml)
springdoc.api-docs.path=/apidocs
springdoc.api-docs.enabled=false

# impostazione di Jacskon per produrre i json: non mandare i valori nulli
spring.jackson.default-property-inclusion=non_null

# Show JPA/Hibernate logging messages
logging.level.org.hibernate.SQL=trace
logging.level.org.hibernate.orm.jdbc.bind=trace

# spring security impostazione per vedere solo i log
logging.level.org.springframework.security=debug

# impostazione per ridurre il logging e tenere solo i warn
logging.level.root=warn
```
*Nota: `logging.level.org.springframework.security=debug` non ha ancora effetto — Security è commentata nel pom.*

### `entity/Board.java`
```java
package com.project.fumanji.entity;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private final boolean directionChoiceEnabled;
    private final boolean mandatoryCheckpointEnabled;
    private final boolean backwardAllowedAfterCheckpoint;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = defaultSquares(size);
        this.directionChoiceEnabled = false;
        this.mandatoryCheckpointEnabled = false;
        this.backwardAllowedAfterCheckpoint = true;
    }

    private Board(int finishPosition, List<Square> squares,
            boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled,
            boolean backwardAllowedAfterCheckpoint) {
        this.finishPosition = finishPosition;
        this.squares = squares;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
        this.backwardAllowedAfterCheckpoint = backwardAllowedAfterCheckpoint;
    }

    private static List<Square> defaultSquares(int size) {
        List<Square> result = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            result.add(new Square(position));
        }
        return List.copyOf(result);
    }

    public static Builder builder(int size) {
        return new Builder(size);
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public boolean isBackwardAllowedAfterCheckpoint() {
        return backwardAllowedAfterCheckpoint;
    }

    public static class Builder {

        private final int finishPosition;
        private final List<Square> squares;
        private boolean directionChoiceEnabled;
        private boolean mandatoryCheckpointEnabled;
        private boolean backwardAllowedAfterCheckpoint = true;

        private Builder(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Board size must be positive");
            }
            this.finishPosition = size;
            this.squares = new ArrayList<>();
            for (int position = 0; position <= size; position++) {
                this.squares.add(new Square(position));
            }
            this.directionChoiceEnabled = false;
            this.mandatoryCheckpointEnabled = false;
        }

        public Builder square(int position, Square square) {
            if (position <= 0 || position >= finishPosition) {
                throw new IllegalArgumentException(
                        "Position must be between 1 and finishPosition - 1 (got " + position + ")");
            }
            squares.set(position, square);
            return this;
        }

        public Builder directionChoiceEnabled(boolean enabled) {
            this.directionChoiceEnabled = enabled;
            return this;
        }

        public Builder mandatoryCheckpointEnabled(boolean enabled) {
            this.mandatoryCheckpointEnabled = enabled;
            return this;
        }

        public Builder backwardAllowedAfterCheckpoint(boolean enabled) {
            this.backwardAllowedAfterCheckpoint = enabled;
            return this;
        }

        public Board build() {
            return new Board(finishPosition, List.copyOf(squares),
                    directionChoiceEnabled, mandatoryCheckpointEnabled,
                    backwardAllowedAfterCheckpoint);
        }
    }
}
```

### `entity/Dice.java`
```java
package com.project.fumanji.entity;

import java.util.concurrent.ThreadLocalRandom;

public class Dice {

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 6;

    public int roll() {
        return ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE + 1);
    }
}
```

### `entity/Player.java`
```java
package com.project.fumanji.entity;

public class Player {

    private final String name;
    private int position;
    private int snack;
    private int drink;
    private int checkpoint;

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.snack = 0;
        this.drink = 0;
        this.checkpoint = 0;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    public int getSnack() {
        return snack;
    }

    public void setSnack(int snack) {
        this.snack = snack;
    }

    public int getDrink() {
        return drink;
    }

    public void setDrink(int drink) {
        this.drink = drink;
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    void setCheckpoint(int checkpoint) {
        this.checkpoint = Math.max(0, checkpoint);
    }
}
```

### `entity/Square.java`
```java
package com.project.fumanji.entity;

import java.util.List;

public class Square {

    private final int position;

    public Square(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void applyEffect(Player player, Game game) {
    }

    public List<Integer> getNextPositions() {
        return List.of(position + 1);
    }

    public List<Integer> getPreviousPositions() {
        return List.of(position - 1);
    }

    public boolean isMandatoryStop() {
        return false;
    }
}
```

### `entity/InteractiveSquare.java`
```java
package com.project.fumanji.entity;

/**
 * Marcatore per le caselle che richiedono una scelta del giocatore per proseguire
 * (es. Bivio, Portale con piu' uscite). Nessun metodo: i due meccanismi di innesco
 * restano diversi (a meta' step per il Bivio, solo su atterraggio esatto per il Portale),
 * la risoluzione della scelta e' invece unificata in Game (isAwaitingChoice/getChoiceOptions/chooseDestination).
 */
public interface InteractiveSquare {
}
```

### `entity/Game.java`
```java
package com.project.fumanji.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;
    private Player winner;
    private int lastRoll;
    private Player lastPlayerToMove;
    private Player awaitingPlayer;
    private List<Integer> awaitingOptions;
    private int awaitingRemainingSteps;
    private boolean awaitingForward;
    private boolean awaitingRespectCheckpoint;
    private boolean awaitingContinueAfterChoice;
    private final List<Integer> movementTrace = new ArrayList<>();
    private final Set<Integer> activatedPositions = new HashSet<>();
    private boolean chainInProgress;
    private TurnPhase turnPhase = TurnPhase.WAITING_ROLL;
    private final List<String> turnMessages = new ArrayList<>();

    public Game(Board board, List<Player> players, Dice dice) {
        if (board == null) {
            throw new IllegalArgumentException("Board must not be null");
        }
        if (dice == null) {
            throw new IllegalArgumentException("Dice must not be null");
        }
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required");
        }
        this.board = board;
        this.players = List.copyOf(players);
        this.dice = dice;
        this.currentPlayerIndex = 0;
        this.winner = null;
    }

    public void playTurn() {
        if (winner != null) {
            return;
        }

        Player currentPlayer = players.get(currentPlayerIndex);
        rollAndMove(currentPlayer);

        if (winner == null) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    public synchronized int performRoll() {
        checkTurnAction(TurnPhase.WAITING_ROLL);
        turnMessages.clear();
        int rollValue = roll(getCurrentPlayer());
        turnPhase = TurnPhase.WAITING_DIRECTION;
        return rollValue;
    }

    public synchronized void performMove(boolean forward) {
        checkTurnAction(TurnPhase.WAITING_DIRECTION);
        moveByChosenDirection(getCurrentPlayer(), forward);
        resolveTurnPhase();
    }

    public synchronized void performChoice(int chosenPosition) {
        checkTurnAction(TurnPhase.WAITING_CHOICE);
        chooseDestination(getCurrentPlayer(), chosenPosition);
        resolveTurnPhase();
    }

    private void checkTurnAction(TurnPhase expectedPhase) {
        if (winner != null) {
            throw new IllegalStateException("Game is already over");
        }
        if (turnPhase != expectedPhase) {
            throw new IllegalStateException(
                    "Action not allowed in phase " + turnPhase + " (expected " + expectedPhase + ")");
        }
    }

    private void resolveTurnPhase() {
        if (winner != null) {
            return;
        }
        if (isAwaitingChoice()) {
            turnPhase = TurnPhase.WAITING_CHOICE;
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turnPhase = TurnPhase.WAITING_ROLL;
    }

    public int roll(Player player) {
        int rollValue = dice.roll();
        lastRoll = rollValue;
        lastPlayerToMove = player;
        return rollValue;
    }

    public void rollAndMove(Player player) {
        int rollValue = roll(player);
        moveForwardBy(player, rollValue);
    }

    public void moveByChosenDirection(Player player, boolean forward) {
        // se non specificato, va avanti
        if (forward || !board.isDirectionChoiceEnabled()) {
            moveForwardBy(player, lastRoll);
        } else {
            moveBackwardBy(player, lastRoll);
        }
    }

    public void moveForwardBy(Player player, int steps) {
        int remaining = steps;

        while (remaining > 0) {
            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getNextPositions();

            if (options.size() > 1) {
                requestBivioChoice(player, options, remaining, true, false);
                return;
            }

            int nextPosition = options.get(0);

            if (nextPosition > board.getFinishPosition()) {
                // rimbalzo: il passo si riflette, si muove indietro di 1 invece che avanti,
                // senza rispettare il checkpoint (stesso comportamento aritmetico di sempre)
                movePlayerTo(player, currentPosition - 1);
                moveBackwardByInternal(player, remaining - 1, false);
                return;
            }

            movePlayerTo(player, nextPosition);
            remaining--;

            Square nextSquare = board.getSquares().get(nextPosition);
            if (board.isMandatoryCheckpointEnabled() && nextSquare.isMandatoryStop()) {
                remaining = 0;
            }
        }

        doLandOnCurrentSquare(player);
    }

    public void moveBackwardBy(Player player, int steps) {
        boolean respectCheckpoint = board.isMandatoryCheckpointEnabled();
        moveBackwardByInternal(player, steps, respectCheckpoint);
    }

    private void moveBackwardByInternal(Player player, int steps, boolean respectCheckpoint) {
        int remaining = steps;

        while (remaining > 0) {
            // pavimento fisso a 0, sempre attivo indipendentemente dalla regola del checkpoint
            if (player.getPosition() <= 0) {
                break;
            }
            // secondo pavimento (introdotto il 24-09-26): se attivo, non si supera mai (indietro)
            // la posizione del proprio checkpoint, sia per scelta volontaria che per spinta forzata
            if (!board.isBackwardAllowedAfterCheckpoint() && player.getPosition() <= player.getCheckpoint()) {
                break;
            }

            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getPreviousPositions();

            if (options.size() > 1) {
                requestBivioChoice(player, options, remaining, false, respectCheckpoint);
                return;
            }

            int nextPosition = options.get(0);
            movePlayerTo(player, nextPosition);
            remaining--;

            Square nextSquare = board.getSquares().get(nextPosition);
            if (respectCheckpoint && nextSquare.isMandatoryStop()) {
                remaining = 0;
            }
        }

        doLandOnCurrentSquare(player);
    }

    private void requestBivioChoice(Player player, List<Integer> options, int remainingSteps,
            boolean forward, boolean respectCheckpoint) {
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = remainingSteps;
        awaitingForward = forward;
        awaitingRespectCheckpoint = respectCheckpoint;
        awaitingContinueAfterChoice = true;
    }

    public void requestChoice(Player player, List<Integer> options) {
        // usato dalle caselle che si attivano solo su atterraggio esatto (es. Portale):
        // nessuna prosecuzione dopo la scelta, "niente di niente" come il teletrasporto normale
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = 0;
        awaitingForward = true;
        awaitingRespectCheckpoint = false;
        awaitingContinueAfterChoice = false;
    }

    public boolean isAwaitingChoice() {
        return awaitingOptions != null;
    }

    public List<Integer> getChoiceOptions() {
        return awaitingOptions;
    }

    public void chooseDestination(Player player, int chosenPosition) {
        if (awaitingOptions == null) {
            throw new IllegalStateException("No choice is pending");
        }
        if (player != awaitingPlayer) {
            throw new IllegalArgumentException("This player has no pending choice");
        }
        if (!awaitingOptions.contains(chosenPosition)) {
            throw new IllegalArgumentException("Chosen position is not among the available options");
        }

        Player waitingPlayer = awaitingPlayer;
        int remainingSteps = awaitingRemainingSteps;
        boolean forward = awaitingForward;
        boolean respectCheckpoint = awaitingRespectCheckpoint;
        boolean continueAfterChoice = awaitingContinueAfterChoice;

        awaitingPlayer = null;
        awaitingOptions = null;

        movePlayerTo(waitingPlayer, chosenPosition);

        if (!continueAfterChoice) {
            return;
        }

        int nextRemaining = remainingSteps - 1;
        if (forward) {
            moveForwardBy(waitingPlayer, nextRemaining);
        } else {
            moveBackwardByInternal(waitingPlayer, nextRemaining, respectCheckpoint);
        }
    }

    public void landOnCurrentSquare(Player player) {
        doLandOnCurrentSquare(player);
    }

    private void doLandOnCurrentSquare(Player player) {
        boolean isChainStart = !chainInProgress;
        if (isChainStart) {
            chainInProgress = true;
            activatedPositions.clear();
        }

        try {
            if (player.getPosition() == board.getFinishPosition()) {
                winner = player;
                return;
            }

            int position = player.getPosition();
            if (!activatedPositions.add(position)) {
                // casella gia' attivata in questo turno di movimento: niente effetto, si comporta da casella base
                return;
            }

            Square square = board.getSquares().get(position);
            square.applyEffect(player, this);
        } finally {
            if (isChainStart) {
                chainInProgress = false;
            }
        }
    }

    public void teleportTo(Player player, int position) {
        movePlayerTo(player, position);
    }

    public void resetToStart(Player player) {
        movePlayerTo(player, player.getCheckpoint());
    }

    private void movePlayerTo(Player player, int position) {
        player.setPosition(position);
        movementTrace.add(position);
    }

    public void clearMovementTrace() {
        movementTrace.clear();
    }

    public List<Integer> getMovementTrace() {
        return List.copyOf(movementTrace);
    }

    public void updateCheckpoint(Player player) {
        player.setCheckpoint(player.getPosition());
    }

    public void addSnack(Player player, int amount) {
        player.setSnack(Math.max(0, player.getSnack() + amount));
    }

    public void addDrink(Player player, int amount) {
        player.setDrink(Math.max(0, player.getDrink() + amount));
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public TurnPhase getTurnPhase() {
        return turnPhase;
    }

    public synchronized void addMessage(String message) {
        turnMessages.add(message);
    }

    public synchronized List<String> getTurnMessages() {
        return List.copyOf(turnMessages);
    }

    public Board getBoard() {
        return board;
    }

    public Player getWinner() {
        return winner;
    }

    public int getLastRoll() {
        return lastRoll;
    }

    public Player getLastPlayerToMove() {
        return lastPlayerToMove;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
```

### `entity/TurnPhase.java`
```java
package com.project.fumanji.entity;

public enum TurnPhase {
    WAITING_ROLL,
    WAITING_DIRECTION,
    WAITING_CHOICE
}
```

### `entity/squares/StartOverSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class StartOverSquare extends Square {

    public StartOverSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.resetToStart(player);
        // game.landOnCurrentSquare(player);
    }
}
```

### `entity/squares/MoveBackSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class MoveBackSquare extends Square {

    private final int steps;

    public MoveBackSquare(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.moveBackwardBy(player, steps);
    }
}
```

### `entity/squares/MoveForwardSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class MoveForwardSquare extends Square {

    private final int steps;

    public MoveForwardSquare(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.moveForwardBy(player, steps);
    }
}
```

### `entity/squares/RollAgainSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class RollAgainSquare extends Square {

    public RollAgainSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.rollAndMove(player);
    }
}
```

### `entity/squares/SnackSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class SnackSquare extends Square {

    private final int amount;

    public SnackSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.addSnack(player, amount);
    }

    public int getAmount() {
        return amount;
    }
}
```

### `entity/squares/DrinkSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class DrinkSquare extends Square {

    private final int amount;

    public DrinkSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.addDrink(player, amount);
    }

    public int getAmount() {
        return amount;
    }
}
```

### `entity/squares/ObligationSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class ObligationSquare extends Square {

    private final String message;

    public ObligationSquare(int position, String message) {
        super(position);
        this.message = message;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.addMessage(buildMessage(player, game));
    }

    public String buildMessage(Player player, Game game) {
        return message.replace("{player}", player.getName());
    }
}
```

### `entity/squares/CheckpointSquare.java`
```java
package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class CheckpointSquare extends Square {

    public CheckpointSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.updateCheckpoint(player);
        game.addMessage(buildMessage(player));
    }

    public String buildMessage(Player player) {
        return player.getName() + " ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";
    }

    @Override
    public boolean isMandatoryStop() {
        return true;
    }
}
```

### `entity/squares/PortalSquare.java`
```java
package com.project.fumanji.entity.squares;

import java.util.List;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.InteractiveSquare;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class PortalSquare extends Square implements InteractiveSquare {

    private final List<Integer> linkedPositions;

    public PortalSquare(int position, List<Integer> linkedPositions) {
        super(position);
        this.linkedPositions = linkedPositions;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (linkedPositions.size() > 1) {
            game.requestChoice(player, linkedPositions);
        } else {
            game.teleportTo(player, linkedPositions.get(0));
        }
    }

    public List<Integer> getLinkedPositions() {
        return linkedPositions;
    }
}
```

### `entity/squares/BivioSquare.java`
```java
package com.project.fumanji.entity.squares;

import java.util.List;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.InteractiveSquare;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class BivioSquare extends Square implements InteractiveSquare {

    private final List<Integer> nextPositions;
    private final List<Integer> previousPositions;
    private final Square effect;

    public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions) {
        this(position, nextPositions, previousPositions, null);
    }

    public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions, Square effect) {
        super(position);
        this.nextPositions = nextPositions;
        this.previousPositions = previousPositions;
        this.effect = effect;
    }

    @Override
    public List<Integer> getNextPositions() {
        return nextPositions;
    }

    @Override
    public List<Integer> getPreviousPositions() {
        return previousPositions;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (effect != null) {
            effect.applyEffect(player, game);
        }
    }
}
```

### `persistence/SquareEntity.java`
```java
package com.project.fumanji.persistence;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "square_type")
public abstract class SquareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    private int position;

    protected SquareEntity() {
    }

    public SquareEntity(int position) {
        this.position = position;
    }

    void setBoard(BoardEntity board) {
        this.board = board;
    }

    public Long getId() {
        return id;
    }

    public BoardEntity getBoard() {
        return board;
    }

    public int getPosition() {
        return position;
    }
}
```
*(`board_id` è nullable — le righe "effetto" attaccate a un `BivioSquareEntity` non appartengono a nessuna board.)*

### `persistence/BoardEntity.java`
```java
package com.project.fumanji.persistence;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int finishPosition;
    private boolean directionChoiceEnabled;
    private boolean mandatoryCheckpointEnabled;
    private boolean backwardAllowedAfterCheckpoint;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SquareEntity> squares = new ArrayList<>();

    protected BoardEntity() {
    }

    public BoardEntity(int finishPosition, boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled,
            boolean backwardAllowedAfterCheckpoint) {
        this.finishPosition = finishPosition;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
        this.backwardAllowedAfterCheckpoint = backwardAllowedAfterCheckpoint;
    }

    public void addSquare(SquareEntity square) {
        squares.add(square);
        square.setBoard(this);
    }

    public Long getId() {
        return id;
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public boolean isBackwardAllowedAfterCheckpoint() {
        return backwardAllowedAfterCheckpoint;
    }

    public List<SquareEntity> getSquares() {
        return squares;
    }
}
```

### `persistence/SquareMapper.java`
```java
package com.project.fumanji.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;

import com.project.fumanji.entity.Square;
import com.project.fumanji.entity.squares.BivioSquare;
import com.project.fumanji.entity.squares.CheckpointSquare;
import com.project.fumanji.entity.squares.DrinkSquare;
import com.project.fumanji.entity.squares.MoveBackSquare;
import com.project.fumanji.entity.squares.MoveForwardSquare;
import com.project.fumanji.entity.squares.ObligationSquare;
import com.project.fumanji.entity.squares.PortalSquare;
import com.project.fumanji.entity.squares.RollAgainSquare;
import com.project.fumanji.entity.squares.SnackSquare;
import com.project.fumanji.entity.squares.StartOverSquare;
import com.project.fumanji.persistence.squares.BivioSquareEntity;
import com.project.fumanji.persistence.squares.CheckpointSquareEntity;
import com.project.fumanji.persistence.squares.DrinkSquareEntity;
import com.project.fumanji.persistence.squares.MoveBackSquareEntity;
import com.project.fumanji.persistence.squares.MoveForwardSquareEntity;
import com.project.fumanji.persistence.squares.ObligationSquareEntity;
import com.project.fumanji.persistence.squares.PortalSquareEntity;
import com.project.fumanji.persistence.squares.RollAgainSquareEntity;
import com.project.fumanji.persistence.squares.SnackSquareEntity;
import com.project.fumanji.persistence.squares.StartOverSquareEntity;

@Mapper(componentModel = "spring")
public interface SquareMapper {

    @SubclassMapping(source = StartOverSquareEntity.class, target = StartOverSquare.class)
    @SubclassMapping(source = MoveBackSquareEntity.class, target = MoveBackSquare.class)
    @SubclassMapping(source = MoveForwardSquareEntity.class, target = MoveForwardSquare.class)
    @SubclassMapping(source = RollAgainSquareEntity.class, target = RollAgainSquare.class)
    @SubclassMapping(source = SnackSquareEntity.class, target = SnackSquare.class)
    @SubclassMapping(source = DrinkSquareEntity.class, target = DrinkSquare.class)
    @SubclassMapping(source = ObligationSquareEntity.class, target = ObligationSquare.class)
    @SubclassMapping(source = CheckpointSquareEntity.class, target = CheckpointSquare.class)
    @SubclassMapping(source = PortalSquareEntity.class, target = PortalSquare.class)
    @SubclassMapping(source = BivioSquareEntity.class, target = BivioSquare.class)
    Square toDomain(SquareEntity entity);
}
```
*Warning noto e innocuo di MapStruct ("Unmapped target properties: nextPositions, previousPositions" su 9 sottoclassi) — vedi spiegazione in sezione 5. Non è stato applicato `unmappedTargetPolicy = IGNORE` (facoltativo, mai richiesto).*

### `persistence/BoardAssembler.java`
```java
package com.project.fumanji.persistence;

import org.springframework.stereotype.Component;

import com.project.fumanji.entity.Board;

@Component
public class BoardAssembler {

    private final SquareMapper squareMapper;

    public BoardAssembler(SquareMapper squareMapper) {
        this.squareMapper = squareMapper;
    }

    public Board toDomain(BoardEntity entity) {
        Board.Builder builder = Board.builder(entity.getFinishPosition())
                .directionChoiceEnabled(entity.isDirectionChoiceEnabled())
                .mandatoryCheckpointEnabled(entity.isMandatoryCheckpointEnabled())
                .backwardAllowedAfterCheckpoint(entity.isBackwardAllowedAfterCheckpoint());

        for (SquareEntity squareEntity : entity.getSquares()) {
            builder.square(squareEntity.getPosition(), squareMapper.toDomain(squareEntity));
        }

        return builder.build();
    }
}
```

### `persistence/squares/StartOverSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class StartOverSquareEntity extends SquareEntity {

    protected StartOverSquareEntity() {
    }

    public StartOverSquareEntity(int position) {
        super(position);
    }
}
```

### `persistence/squares/RollAgainSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class RollAgainSquareEntity extends SquareEntity {

    protected RollAgainSquareEntity() {
    }

    public RollAgainSquareEntity(int position) {
        super(position);
    }
}
```

### `persistence/squares/CheckpointSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class CheckpointSquareEntity extends SquareEntity {

    protected CheckpointSquareEntity() {
    }

    public CheckpointSquareEntity(int position) {
        super(position);
    }
}
```

### `persistence/squares/ObligationSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class ObligationSquareEntity extends SquareEntity {

    private String message;

    protected ObligationSquareEntity() {
    }

    public ObligationSquareEntity(int position, String message) {
        super(position);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```

### `persistence/squares/MoveBackSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class MoveBackSquareEntity extends SquareEntity {

    private int steps;

    protected MoveBackSquareEntity() {
    }

    public MoveBackSquareEntity(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }
}
```

### `persistence/squares/MoveForwardSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class MoveForwardSquareEntity extends SquareEntity {

    private int steps;

    protected MoveForwardSquareEntity() {
    }

    public MoveForwardSquareEntity(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }
}
```

### `persistence/squares/SnackSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class SnackSquareEntity extends SquareEntity {

    private int amount;

    protected SnackSquareEntity() {
    }

    public SnackSquareEntity(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
```

### `persistence/squares/DrinkSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class DrinkSquareEntity extends SquareEntity {

    private int amount;

    protected DrinkSquareEntity() {
    }

    public DrinkSquareEntity(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
```

### `persistence/squares/PortalSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class PortalSquareEntity extends SquareEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "portal_square_link", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "linked_position")
    private List<Integer> linkedPositions = new ArrayList<>();

    protected PortalSquareEntity() {
    }

    public PortalSquareEntity(int position, List<Integer> linkedPositions) {
        super(position);
        this.linkedPositions = new ArrayList<>(linkedPositions);
    }

    public List<Integer> getLinkedPositions() {
        return linkedPositions;
    }
}
```

### `persistence/squares/BivioSquareEntity.java`
```java
package com.project.fumanji.persistence.squares;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class BivioSquareEntity extends SquareEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bivio_square_next", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "next_position")
    private List<Integer> nextPositions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bivio_square_previous", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "previous_position")
    private List<Integer> previousPositions = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "effect_square_id")
    private SquareEntity effect;

    protected BivioSquareEntity() {
    }

    public BivioSquareEntity(int position, List<Integer> nextPositions, List<Integer> previousPositions) {
        this(position, nextPositions, previousPositions, null);
    }

    public BivioSquareEntity(int position, List<Integer> nextPositions, List<Integer> previousPositions,
            SquareEntity effect) {
        super(position);
        this.nextPositions = new ArrayList<>(nextPositions);
        this.previousPositions = new ArrayList<>(previousPositions);
        this.effect = effect;
    }

    public List<Integer> getNextPositions() {
        return nextPositions;
    }

    public List<Integer> getPreviousPositions() {
        return previousPositions;
    }

    public SquareEntity getEffect() {
        return effect;
    }
}
```

### `persistence/EndGameEntity.java`
```java
package com.project.fumanji.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class EndGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String winnerName;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    protected EndGameEntity() {
    }

    public EndGameEntity(String winnerName, BoardEntity board) {
        this.winnerName = winnerName;
        this.board = board;
    }

    public Long getId() {
        return id;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public BoardEntity getBoard() {
        return board;
    }
}
```
La tabella `end_game_entity` la crea Hibernate al primo avvio (`ddl-auto=update`) sul DB `fumanji`.

### `repository/BoardRepository.java`
```java
package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.fumanji.persistence.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
}
```

### `repository/EndGameRepository.java`
```java
package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.fumanji.persistence.EndGameEntity;

public interface EndGameRepository extends JpaRepository<EndGameEntity, Long> {
}
```

### `config/GameConfig.java`
```java
package com.project.fumanji.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.project.fumanji.entity.Dice;

@Configuration
public class GameConfig {

    @Bean
    public Dice dice() {
        return new Dice();
    }
}
```

### `service/GameRegistry.java`
```java
package com.project.fumanji.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.project.fumanji.entity.Game;

/**
 * Registro in-memory delle partite in corso: nessuna persistenza per mossa,
 * il salvataggio su DB avviene solo a fine partita (vedi EndGameEntity, futuro).
 * ConcurrentHashMap perche' il bean e' singleton e le richieste REST arrivano
 * concorrenti su thread diversi.
 */
@Component
public class GameRegistry {

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    public UUID register(Game game) {
        UUID gameId = UUID.randomUUID();
        games.put(gameId, game);
        return gameId;
    }

    public Optional<Game> get(UUID gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public void remove(UUID gameId) {
        games.remove(gameId);
    }
}
```

### `service/GameCreationResult.java`
```java
package com.project.fumanji.service;

import java.util.UUID;

public record GameCreationResult(UUID gameId, String currentPlayerName) {
}
```

### `service/EndGameService.java`
```java
package com.project.fumanji.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.fumanji.entity.Game;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.persistence.EndGameEntity;
import com.project.fumanji.repository.BoardRepository;
import com.project.fumanji.repository.EndGameRepository;

/**
 * Salva su DB l'esito di una partita conclusa.
 * Fase attuale: solo vincitore e board giocata - punto di estensione per lo storico completo.
 */
@Service
public class EndGameService {

    private final BoardRepository boardRepository;
    private final EndGameRepository endGameRepository;

    public EndGameService(BoardRepository boardRepository, EndGameRepository endGameRepository) {
        this.boardRepository = boardRepository;
        this.endGameRepository = endGameRepository;
    }

    @Transactional
    public void save(Game game, Long boardId) {
        BoardEntity board = boardRepository.getReferenceById(boardId);
        endGameRepository.save(new EndGameEntity(game.getWinner().getName(), board));
    }
}
```

### `service/GameService.java`
```java
package com.project.fumanji.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Dice;
import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.exception.GameNotFoundException;
import com.project.fumanji.persistence.BoardAssembler;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.repository.BoardRepository;

/**
 * Costruisce una nuova partita e la registra in GameRegistry.
 * Fase attuale: una sola board fissa (id 1, la board OG) - da rivedere
 * quando si supporteranno board multiple.
 */
@Service
public class GameService {

    private static final long OG_BOARD_ID = 1L;

    private final BoardRepository boardRepository;
    private final BoardAssembler boardAssembler;
    private final GameRegistry gameRegistry;
    private final EndGameService endGameService;
    private final Dice dice;

    public GameService(BoardRepository boardRepository, BoardAssembler boardAssembler,
            GameRegistry gameRegistry, EndGameService endGameService, Dice dice) {
        this.boardRepository = boardRepository;
        this.boardAssembler = boardAssembler;
        this.gameRegistry = gameRegistry;
        this.endGameService = endGameService;
        this.dice = dice;
    }

    public GameCreationResult createGame(List<String> playerNames) {
        BoardEntity boardEntity = boardRepository.findById(OG_BOARD_ID).orElseThrow();
        Board board = boardAssembler.toDomain(boardEntity);

        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }

        Game game = new Game(board, players, dice);
        UUID gameId = gameRegistry.register(game);
        return new GameCreationResult(gameId, players.get(0).getName());
    }

    public Game roll(UUID gameId) {
        Game game = getGame(gameId);
        game.performRoll();
        return game;
    }

    public Game move(UUID gameId, boolean forward) {
        Game game = getGame(gameId);
        game.performMove(forward);
        closeIfFinished(gameId, game);
        return game;
    }

    public Game choose(UUID gameId, int position) {
        Game game = getGame(gameId);
        game.performChoice(position);
        closeIfFinished(gameId, game);
        return game;
    }

    public Game getGame(UUID gameId) {
        return gameRegistry.get(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private void closeIfFinished(UUID gameId, Game game) {
        if (game.getWinner() == null) {
            return;
        }
        endGameService.save(game, OG_BOARD_ID);
        gameRegistry.remove(gameId);
    }
}
```

### `dto/CreateGameRequest.java`
```java
package com.project.fumanji.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateGameRequest(@NotEmpty List<@NotBlank String> playerNames) {
}
```

### `dto/CreateGameResponse.java`
```java
package com.project.fumanji.dto;

import java.util.UUID;

public record CreateGameResponse(UUID gameId, String currentPlayerName) {
}
```

### `dto/PlayerStateResponse.java`
```java
package com.project.fumanji.dto;

public record PlayerStateResponse(String name, int position, int snack, int drink, int checkpoint) {
}
```

### `dto/GameStateResponse.java`
```java
package com.project.fumanji.dto;

import java.util.List;

import com.project.fumanji.entity.TurnPhase;

public record GameStateResponse(
        String currentPlayerName,
        TurnPhase turnPhase,
        int lastRoll,
        List<PlayerStateResponse> players,
        List<Integer> choiceOptions,
        String winnerName,
        List<String> messages) {
}
```

### `dto/MoveRequest.java`
```java
package com.project.fumanji.dto;

import jakarta.validation.constraints.NotNull;

public record MoveRequest(@NotNull Boolean forward) {
}
```

### `dto/ChoiceRequest.java`
```java
package com.project.fumanji.dto;

import jakarta.validation.constraints.NotNull;

public record ChoiceRequest(@NotNull Integer position) {
}
```

### `dto/ErrorResponse.java`
```java
package com.project.fumanji.dto;

public record ErrorResponse(String message) {
}
```

### `exception/GameNotFoundException.java`
```java
package com.project.fumanji.exception;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(UUID gameId) {
        super("Game not found: " + gameId);
    }
}
```

### `exception/GlobalExceptionHandler.java`
```java
package com.project.fumanji.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.fumanji.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(GameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }
}
```

### `mapper/GameStateAssembler.java`
```java
package com.project.fumanji.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.PlayerStateResponse;
import com.project.fumanji.entity.Game;

@Component
public class GameStateAssembler {

    public GameStateResponse toResponse(Game game) {
        List<PlayerStateResponse> players = game.getPlayers().stream()
                .map(player -> new PlayerStateResponse(player.getName(), player.getPosition(),
                        player.getSnack(), player.getDrink(), player.getCheckpoint()))
                .toList();

        List<Integer> choiceOptions = game.isAwaitingChoice() ? List.copyOf(game.getChoiceOptions()) : null;
        String winnerName = game.getWinner() != null ? game.getWinner().getName() : null;
        List<String> turnMessages = game.getTurnMessages();

        return new GameStateResponse(game.getCurrentPlayer().getName(), game.getTurnPhase(),
                game.getLastRoll(), players, choiceOptions, winnerName,
                turnMessages.isEmpty() ? null : turnMessages);
    }
}
```

### `api/GameController.java`
```java
package com.project.fumanji.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.fumanji.dto.ChoiceRequest;
import com.project.fumanji.dto.CreateGameRequest;
import com.project.fumanji.dto.CreateGameResponse;
import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.MoveRequest;
import com.project.fumanji.entity.Game;
import com.project.fumanji.mapper.GameStateAssembler;
import com.project.fumanji.service.GameCreationResult;
import com.project.fumanji.service.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameStateAssembler gameStateAssembler;

    public GameController(GameService gameService, GameStateAssembler gameStateAssembler) {
        this.gameService = gameService;
        this.gameStateAssembler = gameStateAssembler;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@RequestBody @Valid CreateGameRequest request) {
        GameCreationResult result = gameService.createGame(request.playerNames());
        return ResponseEntity.ok(new CreateGameResponse(result.gameId(), result.currentPlayerName()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/roll")
    public ResponseEntity<GameStateResponse> roll(@PathVariable UUID gameId) {
        Game game = gameService.roll(gameId);
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameStateResponse> move(@PathVariable UUID gameId,
            @RequestBody @Valid MoveRequest request) {
        Game game = gameService.move(gameId, request.forward());
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/choice")
    public ResponseEntity<GameStateResponse> choice(@PathVariable UUID gameId,
            @RequestBody @Valid ChoiceRequest request) {
        Game game = gameService.choose(gameId, request.position());
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }
}
```


---

## 7. TODO / Prossimi passi (aggiornato al 30-09-26)

### Aperti
1. **Tabellone** (prossimo pezzo grosso del frontend): servono un endpoint nuovo con caselle e collegamenti (oggi nessun endpoint li restituisce) e la decisione su come disegnare 110 caselle con rami, bivi e portali.
2. **Schermata del vincitore**: aggiungere altro (per esempio posizioni finali di tutti i giocatori).
3. **Spec generati dalla CLI** (`ng test`, Vitest): eseguirli e adattarli o rimuoverli.
4. **Provvisori da rifinire**: fase mostrata come valore grezzo, testi dell'interfaccia, messaggi d'errore, estetica in generale.
5. **Nomi doppi**: non bloccati per scelta; da riconsiderare quando l'utente proverà a "rompere" l'app.
6. **Proxy solo per sviluppo**: da decidere come verrà servito il frontend in produzione.
7. **Ordine dei turni lato frontend**: oggi l'ordine dei box è l'ordine di gioco; in futuro si potrebbe far impostare l'ordine esplicitamente ("Giocatore 1", "Giocatore 2"…, dove i giocatori scrivono il nome di chi inizia, poi chi è secondo, ecc.).
8. **Test Postman** di `roll`/`move`/`choice`/`GET`: a carico dell'utente, li fa appena ha tempo e scrive solo se sono negativi.


### Rimandato a dopo la base BE+FE funzionante
- **Board multiple / creabili dai giocatori** — per ora una sola board fissa (vedi sezione 1). Quando arriverà: il factory pattern per le caselle (JSON con attributo `type` → istanza `Square`), riusando probabilmente `SquareMapper`/`BoardAssembler` come base.
- **Bivio con percorsi non riconvergenti** — per ora tutti i percorsi si ricongiungono prima del traguardo (vedi board OG, sezione 9); in futuro percorsi che non si ricongiungono mai, col rimbalzo di fine tabellone che in quel caso cambierebbe solo il senso di marcia.
- **Board con più caselle di arrivo** — conseguenza diretta del punto sopra.
- **Autenticazione (JWT vs sessione)** — non serve per la fase attuale (nessun account).
- **Valutare Electron** per le fasi successive, quando BE e FE saranno pronti.
- **Obbligo con bersaglio specifico** — per ora `ObligationSquare` non nomina mai un giocatore bersaglio (solo messaggi generici/parametrici su chi ha attivato la casella); in futuro potrebbe scegliere un bersaglio tra `game.getPlayers()`.


### Rimandati (backend)
- **TTL** per le partite abbandonate in `GameRegistry` (`@Scheduled`) — non necessario ora (nessun deploy con uptime lungo/utenti concorrenti reali).
- **Storico/statistiche complete a fine partita**: ridisegnare `Game.movementTrace` per-giocatore, definire quali statistiche tracciare, estendere `EndGameService` (che già riceve l'intero `Game`).
- **Rivalutare EAGER** e l'`OG_BOARD_ID` fisso in `GameService` (`createGame` e `closeIfFinished`) — quando si supporteranno board multiple.


### Deciso di non fare / rimosso dal backlog (per chiarezza storica — non voci attive)
- Altri tipi di caselle speciali oltre ai 10 attuali — non in programma per ora.
- `Game.getLastRoll()` esteso a mostrare gli step intermedi di una catena — deciso che non serve.
- Portale con limitazioni oltre alla lista esplicita — già risolto, ogni istanza ha la propria lista.
- Frontend Angular come voce di backlog a sé — resta nel piano generale (sezione 3).
- Controllo di `backwardAllowedAfterCheckpoint` anche per la scelta volontaria in `moveByChosenDirection` — non era quello richiesto (vedi sezione 5, distinzione importante chiarita il 24-09-26), rimane solo in `moveBackwardByInternal`.


### Chiusi
- ~~`BoardRepository`, script SQL sul DB reale, prova end-to-end~~ → fatto (28-09-26).
- ~~Stato di `Game` tra le richieste REST, controller REST~~ → fatto (28-09-26).
- ~~Rendere `Dice` iniettabile~~ → `GameConfig` + `GameService` + `ControllableDice` (29-09-26).
- ~~`playTurn()`: rimuovere o mantenere~~ → mantenuto (29-09-26).
- ~~`BoardAssemblerIT`: regressione completa e `executionPhase`~~ → fatto (29-09-26).
- ~~Test JUnit con dado fisso per il caso `WAITING_CHOICE` end-to-end~~ → coperto a livello HTTP in `GameControllerIT` (29-09-26).
- ~~Frontend Angular: schermata iniziale con numero di giocatori + nomi, gestione delle risposte `404`/`409`/`400`~~ → fatto, quattro schermate funzionanti (30-09-26).
- ~~Messaggi delle caselle (obblighi/checkpoint) esposti nell'API~~ → fatto (30-09-26).

- ~~Le quattro priorità del main del 24-09-26~~ (script SQL della board OG sul DB reale, `BoardRepository`, prova end-to-end, controller REST + stato di `Game` tra le richieste) → tutte chiuse il 28-09-26 (vedi sopra).


### Chiusi nella sessione del 24-09-26 (dal vecchio main)
- ~~Integrazione Spring Boot~~ → scheletro completo (WAR, `FumanjiApplication`/`ServletInitializer`, `application.properties`), persistenza JPA per `Board`/`Square` (dominio separato + MapStruct + `BoardAssembler`). Repository e controller chiusi poi il 28-09-26.
- ~~Costruire la board OG~~ → mappata per intero (sezione 9) e script di seed scritto e verificato.
- ~~`BivioSquare` con effetto~~ → campo opzionale `effect`, retrocompatibile.
- ~~Regola "non si torna indietro oltre il checkpoint"~~ → `Board.backwardAllowedAfterCheckpoint`, implementata in `moveBackwardByInternal`.
- ~~`ObligationSquare` con messaggi diversi~~ → messaggio parametrico per istanza con placeholder `{player}`.
- ~~Definire la fase/scopo attuale del progetto~~ → una sola board fissa, multiplayer locale su singolo dispositivo, niente account (sezione 1).


---

## 8. Decisioni "minori" ma da ricordare

- Traduzione "obbligo" → `ObligationSquare` (nome definitivo, invariato).
- Package dominio: `com.project.fumanji.entity` e `.entity.squares`. Package persistenza: `com.project.fumanji.persistence` e `.persistence.squares` (introdotto il 24-09-26, scelto per non confondersi col nome "entity" già usato dal dominio).
- Convenzione naming entity JPA: suffisso `Entity` su ogni classe (`SquareEntity`, `BoardEntity`, `SnackSquareEntity`, ecc.).
- Convenzione di visibilità: preferire scelte più ampie/future-proof di default, tranne quando serve vero incapsulamento.
- `resetToStart` ha mantenuto il nome anche se torna al checkpoint (non sempre a 0).
- `moveByChosenDirection` non lancia eccezione se la regola di direzione non è attiva — fallback silenzioso ad "avanti". **Non tocca** la regola `backwardAllowedAfterCheckpoint` (vive altrove, vedi sezione 5).
- Atterrare esattamente su un `BivioSquare` con 0 passi rimasti non forza una scelta — vale anche per il Portale.
- Il rimbalzo di fine tabellone non rispetta mai il checkpoint — asimmetria voluta, invariata.
- Le caselle di ricongiungimento di un bivio sono istanze della stessa classe `BivioSquare`, spesso usate in modalità "solo instradamento" (un solo `nextPosition`, nessuna scelta reale — vedi sezione 5 e 9).
- Costruendo board con bivi, attenzione alla numerazione: un percorso alternativo con caselle proprie deve usare numeri che il percorso principale non attraverserebbe mai di default.
- `Board(int size)` resta pubblico deliberatamente anche dopo l'introduzione del builder.
- **`SquareEntity` è `abstract`**: non esiste mai una riga "casella normale" nel database, l'assenza di riga è l'informazione stessa.
- **Convenzione id per le righe "effetto" di un bivio**: `id = 1000 + posizione del bivio proprietario`, `board_id = NULL`, `position = 0` come placeholder inerte (mai letto).
- **`BivioSquareEntity.effect`**: `@ManyToOne(cascade = CascadeType.ALL)` — necessario per non lasciare righe orfane quando una board (e quindi i suoi bivi) viene cancellata in futuro.
- **`ObligationSquareEntity.message`**: nessuna modifica al mapper necessaria, MapStruct lo abbina da solo per nome col nuovo parametro del costruttore dominio.
- WAR: `ServletInitializer` (generato da Spring Initializr) gestisce da solo l'avvio su servlet container esterno — nessuna modifica necessaria su `FumanjiApplication`, che resta la classe `@SpringBootApplication` "normale". Codice morto quando si lancia da IDE/`main()`, si attiva solo su deploy esterno.
- Sandbox: niente Maven Central raggiungibile — Spring/JPA/MapStruct verificati da Salame in locale, non da Claude. Script SQL invece verificabili in sandbox ricreando lo schema Hibernate reale in MySQL/MariaDB + simulazione del grafo in Python. Il Java puro è compilabile in sandbox con il modulo `jdk.compiler` (vedi sezione 2).


---

## 9. Board OG — dati completi (mappata il 24-09-26 da un tabellone disegnato a mano da Salame)

`finishPosition = 110`. Regole: `directionChoiceEnabled = true`, `mandatoryCheckpointEnabled = true`, `backwardAllowedAfterCheckpoint = false`.

### Legenda tipi (dalla foto del tabellone fisico)
- Cerchio "JUMANJI" → `CheckpointSquare`
- Pallini neri → `StartOverSquare`
- Pallini neri con numero romano → `PortalSquare` (6 portali, **tutti collegati fra loro** — ogni `linkedPositions` contiene le altre 5 posizioni)
- "H2O" → `DrinkSquare` (amount 1, o 2 dove segnato "H2O x2")
- "Schimica"/"Schinica" → `SnackSquare` (amount 1, o 2 dove segnato "x2")
- "No smoke" → nessun effetto, trattata come casella base (**nessuna riga nel database**)
- Tutte le altre scritte → `ObligationSquare`, messaggio diverso per tipo (vedi sotto)

### Messaggi `ObligationSquare` (placeholder `{player}` sostituito col nome di chi ha attivato la casella)
| Tipo | Messaggio |
|---|---|
| obbligo (generico) | `{player} deve dare un obbligo a qualcuno` |
| turca | `tutti i giocatori devono fare una turca (almeno 3 tiri a giro)` |
| crea | `{player} deve fare 3 tiri di creazione` |
| 2 tiri | `{player} deve fare 2 tiri` |
| bong | `{player} deve fare un hit di bong o pipa` |
| giro bong | `tutti devono fare un hit di bong o pipa` |

*(`CheckpointSquare` ha il suo messaggio fisso invariato, non fa parte di questa tabella.)*

Dal 30-09-26 tutti questi messaggi (obblighi e checkpoint) arrivano al frontend in `GameStateResponse.messages` (sezione 5, "Messaggi delle caselle").

### Topologia — bivi e ricongiungimenti

Dalla partenza (posizione 0) si arriva a **Bivio A** (effetto: snack), che si dirama in due:
- **dritto** → **Bivio B** (effetto: obbligo generico), a sua volta diramato in due:
  - **destra** → confluisce, tramite un bivio di solo instradamento, in **Bivio D**
  - **sinistra** → lungo percorso con 3 portali → confluisce in un bivio di solo instradamento pre-checkpoint
- **destra** → **Bivio C** (effetto: turca), a sua volta diramato in due:
  - **su** → confluisce, tramite un bivio di solo instradamento, in **Bivio D**
  - **destra** → lungo percorso con altri 3 portali → confluisce in un bivio di solo instradamento pre-checkpoint

**Bivio D** (effetto: startover) è quindi un punto di **ricongiungimento** (arriva da Bivio C-su e da Bivio B-destra, ciascuno tramite un bivio-redirect intermedio), non un vero bivio in avanti: un solo `nextPosition`, ma **due** `previousPositions`. Continua con una breve coda che confluisce anch'essa nel bivio pre-checkpoint.

Il **bivio pre-checkpoint** (nessun effetto, solo instradamento) raccoglie quindi **3** provenienze (Bivio C-destra, Bivio B-sinistra, la coda di Bivio D) e porta a un solo `nextPosition`: il Checkpoint.

Dal Checkpoint riparte un **unico percorso lineare, senza bivi**, di 28 caselle fino a FINE — e siccome `backwardAllowedAfterCheckpoint = false`, da lì non si torna più indietro oltre il checkpoint stesso.

### Tabella posizioni

| Posizione | Contenuto |
|---|---|
| 0 | Partenza (vuota) |
| 1 | Obbligo (generico) |
| 2 | Turca |
| 3 | H2O |
| 4 | StartOver |
| 5 | *(no smoke — nessuna riga)* |
| **6** | **Bivio A** — next=[10,40], previous=[5], effetto: Snack |
| 10 | 2 tiri |
| 11 | Turca |
| **12** | **Bivio B** — next=[16,22], previous=[11], effetto: Obbligo (generico) |
| 16 | StartOver |
| **17** | Bivio (redirect, no effetto) — next=[70], previous=[16] |
| 22 | H2O |
| 23 | Portale (II) |
| 24 | *(no smoke)* |
| 25 | Turca |
| 26 | Obbligo (generico) |
| 27 | Portale (V) |
| 28 | Schimica |
| 29 | 2 tiri |
| 30 | Obbligo (generico) |
| 31 | H2O |
| 32 | Portale (III) |
| **33** | Bivio (redirect) — next=[80], previous=[32], effetto: Obbligo (crea) |
| 40 | 2 tiri |
| 41 | Obbligo (generico) |
| **42** | **Bivio C** — next=[46,52], previous=[41], effetto: Turca |
| 46 | StartOver |
| **47** | Bivio (redirect) — next=[70], previous=[46], effetto: Drink |
| 52 | H2O |
| 53 | Portale (I) |
| 54 | *(no smoke)* |
| 55 | Turca |
| 56 | Schimica |
| 57 | 2 tiri |
| 58 | Portale (VI) |
| 59 | Obbligo (generico) |
| 60 | H2O |
| 61 | Turca |
| 62 | Portale (IV) |
| **63** | Bivio (redirect) — next=[80], previous=[62], effetto: Obbligo (crea) |
| **70** | **Bivio D** — next=[71], previous=[17,47], effetto: StartOver |
| 71 | Schimica |
| 72 | StartOver |
| **73** | Bivio (redirect) — next=[80], previous=[72], effetto: Obbligo (crea) |
| **80** | Bivio pre-checkpoint — next=[81], previous=[33,63,73], nessun effetto |
| 81 | **Checkpoint JUMANJI** |
| 82 | Obbligo (crea) |
| 83 | H2O x2 |
| 84 | Schimica |
| 85 | *(no smoke)* |
| 86 | Obbligo (generico) |
| 87 | H2O |
| 88 | StartOver |
| 89 | Obbligo (generico) |
| 90 | Obbligo (crea) |
| 91 | Obbligo (bong) |
| 92 | Turca |
| 93 | StartOver |
| 94 | H2O x2 |
| 95 | Schimica |
| 96 | StartOver |
| 97 | *(no smoke)* |
| 98 | Turca |
| 99 | 2 tiri |
| 100 | Obbligo (generico) |
| 101 | *(no smoke)* |
| 102 | Schimica |
| 103 | StartOver |
| 104 | H2O x2 |
| 105 | Obbligo (giro bong) |
| 106 | StartOver |
| 107 | Obbligo (bong) |
| 108 | Obbligo (crea) |
| 109 | StartOver |
| 110 | **FINE** (`finishPosition`, nessuna riga) |

### Portali — collegamenti (numero romano dalla foto → posizione)
Portale I=53, II=23, III=32, IV=62, V=27, VI=58. Ognuno collegato a **tutti gli altri 5**.

### Script di seed — verificato

Verificato ricreando lo schema esatto generato da Hibernate (incluse le modifiche del 24-09-26: `board_id` nullable, `effect_square_id`, `message`, `backward_allowed_after_checkpoint`) in un MySQL/MariaDB di sandbox, più una simulazione in Python del grafo di gioco (BFS da 0, segue `next`/`bivio_square_next` come farebbe il motore): raggiunge 110, tutte le 68 caselle piazzate vengono toccate, i 6 gap "no smoke" tornano dove previsto, nessuna incoerenza `next`/`previous` sui 10 bivi, ogni portale ha esattamente gli altri 5 collegamenti.

**Convenzione id**: id = numero di posizione per le caselle piazzate; per le 8 righe "effetto" (attaccate a un bivio, non posizionate) `id = 1000 + posizione del bivio proprietario`, `board_id = NULL`, `position = 0` (placeholder inerte, mai letto).

```sql
-- BOARD
INSERT INTO board_entity (id, finish_position, direction_choice_enabled, mandatory_checkpoint_enabled, backward_allowed_after_checkpoint)
VALUES (1, 110, 1, 1, 0);

-- SQUARE_ENTITY: righe posizionate (board_id = 1) + righe "effetto" (board_id = NULL, position = 0)
INSERT INTO square_entity (id, position, board_id, square_type) VALUES
(1, 1, 1, 'ObligationSquareEntity'),
(2, 2, 1, 'ObligationSquareEntity'),
(3, 3, 1, 'DrinkSquareEntity'),
(4, 4, 1, 'StartOverSquareEntity'),
(6, 6, 1, 'BivioSquareEntity'),
(10, 10, 1, 'ObligationSquareEntity'),
(11, 11, 1, 'ObligationSquareEntity'),
(12, 12, 1, 'BivioSquareEntity'),
(16, 16, 1, 'StartOverSquareEntity'),
(17, 17, 1, 'BivioSquareEntity'),
(22, 22, 1, 'DrinkSquareEntity'),
(23, 23, 1, 'PortalSquareEntity'),
(25, 25, 1, 'ObligationSquareEntity'),
(26, 26, 1, 'ObligationSquareEntity'),
(27, 27, 1, 'PortalSquareEntity'),
(28, 28, 1, 'SnackSquareEntity'),
(29, 29, 1, 'ObligationSquareEntity'),
(30, 30, 1, 'ObligationSquareEntity'),
(31, 31, 1, 'DrinkSquareEntity'),
(32, 32, 1, 'PortalSquareEntity'),
(33, 33, 1, 'BivioSquareEntity'),
(40, 40, 1, 'ObligationSquareEntity'),
(41, 41, 1, 'ObligationSquareEntity'),
(42, 42, 1, 'BivioSquareEntity'),
(46, 46, 1, 'StartOverSquareEntity'),
(47, 47, 1, 'BivioSquareEntity'),
(52, 52, 1, 'DrinkSquareEntity'),
(53, 53, 1, 'PortalSquareEntity'),
(55, 55, 1, 'ObligationSquareEntity'),
(56, 56, 1, 'SnackSquareEntity'),
(57, 57, 1, 'ObligationSquareEntity'),
(58, 58, 1, 'PortalSquareEntity'),
(59, 59, 1, 'ObligationSquareEntity'),
(60, 60, 1, 'DrinkSquareEntity'),
(61, 61, 1, 'ObligationSquareEntity'),
(62, 62, 1, 'PortalSquareEntity'),
(63, 63, 1, 'BivioSquareEntity'),
(70, 70, 1, 'BivioSquareEntity'),
(71, 71, 1, 'SnackSquareEntity'),
(72, 72, 1, 'StartOverSquareEntity'),
(73, 73, 1, 'BivioSquareEntity'),
(80, 80, 1, 'BivioSquareEntity'),
(81, 81, 1, 'CheckpointSquareEntity'),
(82, 82, 1, 'ObligationSquareEntity'),
(83, 83, 1, 'DrinkSquareEntity'),
(84, 84, 1, 'SnackSquareEntity'),
(86, 86, 1, 'ObligationSquareEntity'),
(87, 87, 1, 'DrinkSquareEntity'),
(88, 88, 1, 'StartOverSquareEntity'),
(89, 89, 1, 'ObligationSquareEntity'),
(90, 90, 1, 'ObligationSquareEntity'),
(91, 91, 1, 'ObligationSquareEntity'),
(92, 92, 1, 'ObligationSquareEntity'),
(93, 93, 1, 'StartOverSquareEntity'),
(94, 94, 1, 'DrinkSquareEntity'),
(95, 95, 1, 'SnackSquareEntity'),
(96, 96, 1, 'StartOverSquareEntity'),
(98, 98, 1, 'ObligationSquareEntity'),
(99, 99, 1, 'ObligationSquareEntity'),
(100, 100, 1, 'ObligationSquareEntity'),
(102, 102, 1, 'SnackSquareEntity'),
(103, 103, 1, 'StartOverSquareEntity'),
(104, 104, 1, 'DrinkSquareEntity'),
(105, 105, 1, 'ObligationSquareEntity'),
(106, 106, 1, 'StartOverSquareEntity'),
(107, 107, 1, 'ObligationSquareEntity'),
(108, 108, 1, 'ObligationSquareEntity'),
(109, 109, 1, 'StartOverSquareEntity'),
-- righe "effetto" dei bivi: nessuna board (non sono posizionate), position=0 = placeholder inerte
(1006, 0, NULL, 'SnackSquareEntity'),
(1012, 0, NULL, 'ObligationSquareEntity'),
(1042, 0, NULL, 'ObligationSquareEntity'),
(1070, 0, NULL, 'StartOverSquareEntity'),
(1033, 0, NULL, 'ObligationSquareEntity'),
(1047, 0, NULL, 'DrinkSquareEntity'),
(1063, 0, NULL, 'ObligationSquareEntity'),
(1073, 0, NULL, 'ObligationSquareEntity');

-- OBLIGATION: messaggi (positioned + effetto)
INSERT INTO obligation_square_entity (id, message) VALUES
(1, '{player} deve dare un obbligo a qualcuno'),
(2, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(10, '{player} deve fare 2 tiri'),
(11, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(25, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(26, '{player} deve dare un obbligo a qualcuno'),
(29, '{player} deve fare 2 tiri'),
(30, '{player} deve dare un obbligo a qualcuno'),
(40, '{player} deve fare 2 tiri'),
(41, '{player} deve dare un obbligo a qualcuno'),
(55, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(57, '{player} deve fare 2 tiri'),
(59, '{player} deve dare un obbligo a qualcuno'),
(61, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(82, '{player} deve fare 3 tiri di creazione'),
(86, '{player} deve dare un obbligo a qualcuno'),
(89, '{player} deve dare un obbligo a qualcuno'),
(90, '{player} deve fare 3 tiri di creazione'),
(91, '{player} deve fare un hit di bong o pipa'),
(92, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(98, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(99, '{player} deve fare 2 tiri'),
(100, '{player} deve dare un obbligo a qualcuno'),
(105, 'tutti devono fare un hit di bong o pipa'),
(107, '{player} deve fare un hit di bong o pipa'),
(108, '{player} deve fare 3 tiri di creazione'),
(1012, '{player} deve dare un obbligo a qualcuno'),
(1042, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(1033, '{player} deve fare 3 tiri di creazione'),
(1063, '{player} deve fare 3 tiri di creazione'),
(1073, '{player} deve fare 3 tiri di creazione');

-- DRINK (amount 1 di default, 2 dove segnato "x2")
INSERT INTO drink_square_entity (id, amount) VALUES
(3, 1), (22, 1), (31, 1), (52, 1), (60, 1),
(83, 2), (87, 1), (94, 2), (104, 2),
(1047, 1);

-- SNACK
INSERT INTO snack_square_entity (id, amount) VALUES
(28, 1), (56, 1), (71, 1), (84, 1), (95, 1), (102, 1),
(1006, 1);

-- STARTOVER
INSERT INTO start_over_square_entity (id) VALUES
(4), (16), (46), (72), (88), (93), (96), (103), (106), (109),
(1070);

-- CHECKPOINT
INSERT INTO checkpoint_square_entity (id) VALUES (81);

-- PORTAL
INSERT INTO portal_square_entity (id) VALUES (23), (27), (32), (53), (58), (62);

INSERT INTO portal_square_link (square_id, linked_position) VALUES
(53, 23), (53, 32), (53, 62), (53, 27), (53, 58),
(23, 53), (23, 32), (23, 62), (23, 27), (23, 58),
(32, 53), (32, 23), (32, 62), (32, 27), (32, 58),
(62, 53), (62, 23), (62, 32), (62, 27), (62, 58),
(27, 53), (27, 23), (27, 32), (27, 62), (27, 58),
(58, 53), (58, 23), (58, 32), (58, 62), (58, 27);

-- BIVIO (id, effect_square_id nullable)
INSERT INTO bivio_square_entity (id, effect_square_id) VALUES
(6, 1006),
(12, 1012),
(17, NULL),
(33, 1033),
(42, 1042),
(47, 1047),
(63, 1063),
(70, 1070),
(73, 1073),
(80, NULL);

-- NEXT / PREVIOUS POSITIONS DEI BIVI
INSERT INTO bivio_square_next (square_id, next_position) VALUES
(6, 10), (6, 40),
(12, 16), (12, 22),
(17, 70),
(33, 80),
(42, 46), (42, 52),
(47, 70),
(63, 80),
(70, 71),
(73, 80),
(80, 81);

INSERT INTO bivio_square_previous (square_id, previous_position) VALUES
(6, 5),
(12, 11),
(17, 16),
(33, 32),
(42, 41),
(47, 46),
(63, 62),
(70, 17), (70, 47),
(73, 72),
(80, 33), (80, 63), (80, 73);
```


---

## 10. Codice frontend — versioni finali (30-09-26)

### `proxy.conf.json` (nuovo, in `FE/angular`, accanto ad `angular.json`)
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

### `angular.json` — aggiunta dentro il target `serve` (accanto a `builder`)
```json
"options": {
  "proxyConfig": "proxy.conf.json"
},
```

### `app.config.ts`
```typescript
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Abilita HttpClient per le chiamate al backend
    provideHttpClient(),
  ]
};
```

### `app.routes.ts`
```typescript
import { Routes } from '@angular/router';

import { GameScreen } from './game-screen/game-screen';
import { PlayerSetup } from './player-setup/player-setup';
import { Welcome } from './welcome/welcome';
import { Winner } from './winner/winner';

export const routes: Routes = [
  { path: '', component: Welcome },
  { path: 'players', component: PlayerSetup },
  { path: 'game/:gameId', component: GameScreen },
  { path: 'winner', component: Winner },
];
```

### `app.html`
```html
<router-outlet />
```
*(`app.ts` invariato; il signal `title` non è più usato e la sua rimozione, insieme all'import di `signal`, è facoltativa.)*

### `app.spec.ts`
Rimosso il test `should render title` (cercava un `h1` che in `App` non esiste più). Resta solo `should create the app`.

### Models (`src/app/models/`)

`turn-phase.ts`:
```typescript
export type TurnPhase = 'WAITING_ROLL' | 'WAITING_DIRECTION' | 'WAITING_CHOICE';
```

`create-game-response.ts`:
```typescript
export interface CreateGameResponse {
  gameId: string;
  currentPlayerName: string;
}
```

`player-state-response.ts`:
```typescript
export interface PlayerStateResponse {
  name: string;
  position: number;
  snack: number;
  drink: number;
  checkpoint: number;
}
```

`game-state-response.ts`:
```typescript
import { PlayerStateResponse } from './player-state-response';
import { TurnPhase } from './turn-phase';

export interface GameStateResponse {
  currentPlayerName: string;
  turnPhase: TurnPhase;
  lastRoll: number;
  players: PlayerStateResponse[];
  // Assenti dalla risposta quando non ci sono (il backend omette i valori null)
  choiceOptions?: number[];
  winnerName?: string;
  // Messaggi generati dalle caselle durante il turno; assenti dalla risposta quando non ce ne sono
  messages?: string[];
}
```

### `services/game-api.ts`
```typescript
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CreateGameResponse } from '../models/create-game-response';
import { GameStateResponse } from '../models/game-state-response';

@Injectable({
  providedIn: 'root',
})
export class GameApi {

  private readonly http = inject(HttpClient);

  // Percorso relativo: in sviluppo lo inoltra il proxy del dev server al backend
  private readonly baseUrl = '/api/games';

  createGame(playerNames: string[]): Observable<CreateGameResponse> {
    return this.http.post<CreateGameResponse>(this.baseUrl, { playerNames });
  }

  getGame(gameId: string): Observable<GameStateResponse> {
    return this.http.get<GameStateResponse>(`${this.baseUrl}/${gameId}`);
  }

  roll(gameId: string): Observable<GameStateResponse> {
    return this.http.post<GameStateResponse>(`${this.baseUrl}/${gameId}/roll`, null);
  }

  move(gameId: string, forward: boolean): Observable<GameStateResponse> {
    return this.http.post<GameStateResponse>(`${this.baseUrl}/${gameId}/move`, { forward });
  }

  choose(gameId: string, position: number): Observable<GameStateResponse> {
    return this.http.post<GameStateResponse>(`${this.baseUrl}/${gameId}/choice`, { position });
  }
}
```

### `welcome/`

`welcome.ts`:
```typescript
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-welcome',
  imports: [],
  templateUrl: './welcome.html',
  styleUrl: './welcome.css',
})
export class Welcome {

  private readonly router = inject(Router);

  goToPlayers(): void {
    this.router.navigate(['/players']);
  }
}
```

`welcome.html`:
```html
<div class="welcome" (click)="goToPlayers()">
  <h1>Fumanji</h1>
  <p>Clicca ovunque per iniziare</p>
</div>
```

`welcome.css`:
```css
.welcome {
  position: fixed;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
```

### `player-setup/`

`player-setup.ts`:
```typescript
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { GameApi } from '../services/game-api';

const MIN_PLAYERS = 2;
const MAX_PLAYERS = 10;

@Component({
  selector: 'app-player-setup',
  imports: [],
  templateUrl: './player-setup.html',
  styleUrl: './player-setup.css',
})
export class PlayerSetup {

  // Nomi dei giocatori nell'ordine di gioco; si parte con il numero minimo di caselle vuote
  readonly names = signal<string[]>(Array<string>(MIN_PLAYERS).fill(''));

  private readonly gameApi = inject(GameApi);
  private readonly router = inject(Router);

  // True mentre la richiesta di creazione è in corso: evita partite doppie con click ripetuti
  readonly isSubmitting = signal(false);

  // Messaggio mostrato se la creazione della partita fallisce
  readonly errorMessage = signal<string | null>(null);

  // Si può aggiungere un giocatore finché non si raggiunge il massimo
  readonly canAdd = computed(() => this.names().length < MAX_PLAYERS);

  // Si può togliere un giocatore finché non si scende sotto il minimo
  readonly canRemove = computed(() => this.names().length > MIN_PLAYERS);

  // L'invio è abilitato solo se nessun nome è vuoto (il backend rifiuta i nomi blank) e nessuna richiesta è in corso
  readonly canSubmit = computed(() =>
    !this.isSubmitting() && this.names().every(name => name.trim().length > 0));

  addPlayer(): void {
    if (!this.canAdd()) {
      return;
    }
    // L'array non si modifica sul posto: update() ne restituisce uno nuovo, così il signal si accorge del cambio
    this.names.update(names => [...names, '']);
  }

  removePlayer(index: number): void {
    if (!this.canRemove()) {
      return;
    }
    // Si tengono tutti i nomi tranne quello alla posizione indicata
    this.names.update(names => names.filter((_, i) => i !== index));
  }

  updateName(index: number, value: string): void {
    // Sostituisce solo il nome alla posizione indicata, gli altri restano invariati
    this.names.update(names => names.map((name, i) => (i === index ? value : name)));
  }

  submit(): void {
    if (!this.canSubmit()) {
      return;
    }
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    // La richiesta parte solo con subscribe(): se va a buon fine si passa alla schermata di gioco, altrimenti si mostra l'errore
    this.gameApi.createGame(this.names().map(name => name.trim())).subscribe({
      next: response => this.router.navigate(['/game', response.gameId]),
      error: () => {
        this.errorMessage.set('Impossibile creare la partita. Riprova.');
        this.isSubmitting.set(false);
      },
      complete: () => this.isSubmitting.set(false),
    });
  }
}
```

`player-setup.html`:
```html
<div class="player-setup">
  <h2>Chi gioca?</h2>
  <p>Inserisci i nomi nell'ordine di gioco</p>

  @for (name of names(); track $index) {
    <div class="player-row">
      <input
        #nameInput
        type="text"
        [value]="name"
        [placeholder]="'Giocatore ' + ($index + 1)"
        (input)="updateName($index, nameInput.value)"
      />
      <button type="button" (click)="removePlayer($index)" [disabled]="!canRemove()">Rimuovi</button>
    </div>
  }

  <button type="button" (click)="addPlayer()" [disabled]="!canAdd()">Aggiungi giocatore</button>
  @if (errorMessage()) {
    <p class="error">{{ errorMessage() }}</p>
  }
  <button type="button" (click)="submit()" [disabled]="!canSubmit()">Inizia</button>
</div>
```

`player-setup.css`:
```css
.player-setup {
  max-width: 400px;
  margin: 2rem auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.player-row {
  display: flex;
  gap: 0.5rem;
}

.player-row input {
  flex: 1;
}

.error {
  color: red;
}
```

### `game-screen/`

`game-screen.ts`:
```typescript
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { GameStateResponse } from '../models/game-state-response';
import { GameApi } from '../services/game-api';

@Component({
  selector: 'app-game-screen',
  imports: [],
  templateUrl: './game-screen.html',
  styleUrl: './game-screen.css',
})
export class GameScreen implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly gameApi = inject(GameApi);
  private readonly router = inject(Router);

  // Identificativo della partita, preso dall'URL (/game/:gameId); la rotta lo garantisce sempre presente
  private readonly gameId = this.route.snapshot.paramMap.get('gameId')!;

  // Ultimo stato ricevuto dal backend; resta null finché non arriva la prima risposta
  readonly state = signal<GameStateResponse | null>(null);

  // Messaggio mostrato se lo stato non si riesce a caricare o un'azione fallisce
  readonly errorMessage = signal<string | null>(null);

  // True mentre un'azione è in corso: disabilita i pulsanti ed evita richieste doppie con click ripetuti
  readonly isBusy = signal(false);

  ngOnInit(): void {
    // Lo stato si legge dal backend a ogni apertura della pagina, anche dopo un refresh
    this.gameApi.getGame(this.gameId).subscribe({
      next: state => this.state.set(state),
      error: (error: HttpErrorResponse) => this.errorMessage.set(this.toErrorMessage(error)),
    });
  }

  roll(): void {
    this.runAction(this.gameApi.roll(this.gameId));
  }

  move(forward: boolean): void {
    this.runAction(this.gameApi.move(this.gameId, forward));
  }

  choose(position: number): void {
    this.runAction(this.gameApi.choose(this.gameId, position));
  }

  // Esegue un'azione del turno: la richiesta parte solo con subscribe() e, se ne è già in corso una, la nuova viene ignorata
  private runAction(action: Observable<GameStateResponse>): void {
    if (this.isBusy()) {
      return;
    }
    this.isBusy.set(true);
    this.errorMessage.set(null);

    action.subscribe({
      next: state => {
        this.state.set(state);
        // Alla vittoria si passa alla schermata del vincitore: la partita non è più rileggibile dal backend
        if (state.winnerName) {
          this.router.navigate(['/winner'], { state: { winnerName: state.winnerName } });
        }
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(this.toErrorMessage(error));
        this.isBusy.set(false);
      },
      complete: () => this.isBusy.set(false),
    });
  }

  // Traduce lo status HTTP dell'errore in un messaggio per il giocatore
  private toErrorMessage(error: HttpErrorResponse): string {
    switch (error.status) {
      case 404:
        return 'Partita non trovata.';
      case 409:
        return 'Azione non consentita in questa fase del turno.';
      case 400:
        return 'Richiesta non valida.';
      default:
        return 'Errore di comunicazione con il server.';
    }
  }
}
```

`game-screen.html`:
```html
<div class="game-screen">
  @if (errorMessage()) {
    <p class="error">{{ errorMessage() }}</p>
  }

  @let game = state();
  @if (game) {
    <h2>Turno di {{ game.currentPlayerName }}</h2>
    <p>Fase: {{ game.turnPhase }}</p>
    @if (game.lastRoll > 0) {
      <p>Ultimo tiro: {{ game.lastRoll }}</p>
    }

    <div class="actions">
      @switch (game.turnPhase) {
        @case ('WAITING_ROLL') {
          <button type="button" (click)="roll()" [disabled]="isBusy()">Tira il dado</button>
        }
        @case ('WAITING_DIRECTION') {
          <button type="button" (click)="move(true)" [disabled]="isBusy()">Avanti</button>
          <button type="button" (click)="move(false)" [disabled]="isBusy()">Indietro</button>
        }
        @case ('WAITING_CHOICE') {
          @for (option of game.choiceOptions ?? []; track option) {
            <button type="button" (click)="choose(option)" [disabled]="isBusy()">
              Vai alla casella {{ option }}
            </button>
          }
        }
      }
    </div>

    @if (game.messages; as messages) {
      <div class="messages">
        @for (message of messages; track $index) {
          <p>{{ message }}</p>
        }
      </div>
    }

    <table>
      <thead>
        <tr>
          <th>Giocatore</th>
          <th>Posizione</th>
          <th>Snack</th>
          <th>Drink</th>
          <th>Checkpoint</th>
        </tr>
      </thead>
      <tbody>
        @for (player of game.players; track $index) {
          <tr>
            <td>{{ player.name }}</td>
            <td>{{ player.position }}</td>
            <td>{{ player.snack }}</td>
            <td>{{ player.drink }}</td>
            <td>{{ player.checkpoint }}</td>
          </tr>
        }
      </tbody>
    </table>
  }
</div>
```

`game-screen.css`:
```css
.game-screen {
  max-width: 600px;
  margin: 2rem auto;
}

.error {
  color: red;
}

.actions {
  display: flex;
  gap: 0.5rem;
  margin: 1rem 0;
}

.messages {
  border: 1px solid #ccc;
  padding: 0.5rem 1rem;
  margin: 1rem 0;
}

table {
  border-collapse: collapse;
  width: 100%;
}

th,
td {
  border: 1px solid #ccc;
  padding: 0.5rem;
  text-align: left;
}
```

### `winner/`

`winner.ts`:
```typescript
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-winner',
  imports: [RouterLink],
  templateUrl: './winner.html',
  styleUrl: './winner.css',
})
export class Winner {

  // Nome del vincitore passato con lo stato della navigazione: history.state sopravvive al refresh della pagina
  readonly winnerName: string | undefined = history.state?.['winnerName'];
}
```

`winner.html`:
```html
<div class="winner">
  @if (winnerName) {
    <h1>Ha vinto {{ winnerName }}!</h1>
  } @else {
    <p>Nessun vincitore da mostrare.</p>
  }
  <a routerLink="/players">Nuova partita</a>
</div>
```

`winner.css`:
```css
.winner {
  max-width: 600px;
  margin: 2rem auto;
  text-align: center;
}
```

---


---

## 11. Test e risorse di test (28-09-26 → 30-09-26)

### `src/test/resources/application-test.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fumanji_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```
Prerequisito: schema `fumanji_test` creato a mano (`CREATE DATABASE fumanji_test;`). Nota: le properties di profilo si **sommano** a quelle di `application.properties` (es. `logging.level.root=warn` resta attivo nei test).

### `src/test/resources/seed-og-board.sql`
Copia identica, senza modifiche, dello script di seed della sezione 9.

### `test/java/com/project/fumanji/entity/ControllableDice.java`
```java
package com.project.fumanji.entity;

/**
 * Dado di test: valore del prossimo tiro impostabile a piacere,
 * per coprire nei test scenari che dipendono dal risultato del dado.
 */
public class ControllableDice extends Dice {

    private int nextRoll = 1;

    public void setNextRoll(int nextRoll) {
        this.nextRoll = nextRoll;
    }

    @Override
    public int roll() {
        return nextRoll;
    }
}
```

### `test/java/com/project/fumanji/persistence/BoardAssemblerIT.java`
```java
package com.project.fumanji.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Square;
import com.project.fumanji.entity.squares.BivioSquare;
import com.project.fumanji.entity.squares.CheckpointSquare;
import com.project.fumanji.entity.squares.DrinkSquare;
import com.project.fumanji.entity.squares.ObligationSquare;
import com.project.fumanji.entity.squares.PortalSquare;
import com.project.fumanji.entity.squares.SnackSquare;
import com.project.fumanji.entity.squares.StartOverSquare;
import com.project.fumanji.repository.BoardRepository;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BoardAssemblerIT {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardAssembler boardAssembler;

    @Test
    void reconstructsOgBoardFromDatabase() {
        BoardEntity entity = boardRepository.findById(1L).orElseThrow();
        Board board = boardAssembler.toDomain(entity);

        assertThat(board.getFinishPosition()).isEqualTo(110);
        assertThat(board.isDirectionChoiceEnabled()).isTrue();
        assertThat(board.isMandatoryCheckpointEnabled()).isTrue();
        assertThat(board.isBackwardAllowedAfterCheckpoint()).isFalse();

        Square square6 = board.getSquares().get(6);
        assertThat(square6).isInstanceOf(BivioSquare.class);
        assertThat(square6.getNextPositions()).containsExactly(10, 40);

        Square square81 = board.getSquares().get(81);
        assertThat(square81).isInstanceOf(CheckpointSquare.class);
        assertThat(square81.isMandatoryStop()).isTrue();
    }

    @Test
    void reconstructsAllOgBoardSquaresCorrectly() {
        BoardEntity entity = boardRepository.findById(1L).orElseThrow();
        Board board = boardAssembler.toDomain(entity);
        List<Square> squares = board.getSquares();

        // caselle base: partenza, arrivo, i 6 gap "no smoke"
        List<Integer> plainPositions = List.of(0, 5, 24, 54, 85, 97, 101, 110);
        for (int position : plainPositions) {
            assertThat(squares.get(position)).as("position %d", position).isExactlyInstanceOf(Square.class);
        }

        // tipi delle 68 caselle piazzate
        Map<Integer, Class<? extends Square>> expectedTypes = new HashMap<>();
        expectedTypes.put(1, ObligationSquare.class);
        expectedTypes.put(2, ObligationSquare.class);
        expectedTypes.put(3, DrinkSquare.class);
        expectedTypes.put(4, StartOverSquare.class);
        expectedTypes.put(6, BivioSquare.class);
        expectedTypes.put(10, ObligationSquare.class);
        expectedTypes.put(11, ObligationSquare.class);
        expectedTypes.put(12, BivioSquare.class);
        expectedTypes.put(16, StartOverSquare.class);
        expectedTypes.put(17, BivioSquare.class);
        expectedTypes.put(22, DrinkSquare.class);
        expectedTypes.put(23, PortalSquare.class);
        expectedTypes.put(25, ObligationSquare.class);
        expectedTypes.put(26, ObligationSquare.class);
        expectedTypes.put(27, PortalSquare.class);
        expectedTypes.put(28, SnackSquare.class);
        expectedTypes.put(29, ObligationSquare.class);
        expectedTypes.put(30, ObligationSquare.class);
        expectedTypes.put(31, DrinkSquare.class);
        expectedTypes.put(32, PortalSquare.class);
        expectedTypes.put(33, BivioSquare.class);
        expectedTypes.put(40, ObligationSquare.class);
        expectedTypes.put(41, ObligationSquare.class);
        expectedTypes.put(42, BivioSquare.class);
        expectedTypes.put(46, StartOverSquare.class);
        expectedTypes.put(47, BivioSquare.class);
        expectedTypes.put(52, DrinkSquare.class);
        expectedTypes.put(53, PortalSquare.class);
        expectedTypes.put(55, ObligationSquare.class);
        expectedTypes.put(56, SnackSquare.class);
        expectedTypes.put(57, ObligationSquare.class);
        expectedTypes.put(58, PortalSquare.class);
        expectedTypes.put(59, ObligationSquare.class);
        expectedTypes.put(60, DrinkSquare.class);
        expectedTypes.put(61, ObligationSquare.class);
        expectedTypes.put(62, PortalSquare.class);
        expectedTypes.put(63, BivioSquare.class);
        expectedTypes.put(70, BivioSquare.class);
        expectedTypes.put(71, SnackSquare.class);
        expectedTypes.put(72, StartOverSquare.class);
        expectedTypes.put(73, BivioSquare.class);
        expectedTypes.put(80, BivioSquare.class);
        expectedTypes.put(81, CheckpointSquare.class);
        expectedTypes.put(82, ObligationSquare.class);
        expectedTypes.put(83, DrinkSquare.class);
        expectedTypes.put(84, SnackSquare.class);
        expectedTypes.put(86, ObligationSquare.class);
        expectedTypes.put(87, DrinkSquare.class);
        expectedTypes.put(88, StartOverSquare.class);
        expectedTypes.put(89, ObligationSquare.class);
        expectedTypes.put(90, ObligationSquare.class);
        expectedTypes.put(91, ObligationSquare.class);
        expectedTypes.put(92, ObligationSquare.class);
        expectedTypes.put(93, StartOverSquare.class);
        expectedTypes.put(94, DrinkSquare.class);
        expectedTypes.put(95, SnackSquare.class);
        expectedTypes.put(96, StartOverSquare.class);
        expectedTypes.put(98, ObligationSquare.class);
        expectedTypes.put(99, ObligationSquare.class);
        expectedTypes.put(100, ObligationSquare.class);
        expectedTypes.put(102, SnackSquare.class);
        expectedTypes.put(103, StartOverSquare.class);
        expectedTypes.put(104, DrinkSquare.class);
        expectedTypes.put(105, ObligationSquare.class);
        expectedTypes.put(106, StartOverSquare.class);
        expectedTypes.put(107, ObligationSquare.class);
        expectedTypes.put(108, ObligationSquare.class);
        expectedTypes.put(109, StartOverSquare.class);

        expectedTypes.forEach((position, type) ->
                assertThat(squares.get(position)).as("position %d", position).isExactlyInstanceOf(type));

        // next/previous dei 10 bivi
        assertBivio(squares, 6, List.of(10, 40), List.of(5));
        assertBivio(squares, 12, List.of(16, 22), List.of(11));
        assertBivio(squares, 17, List.of(70), List.of(16));
        assertBivio(squares, 33, List.of(80), List.of(32));
        assertBivio(squares, 42, List.of(46, 52), List.of(41));
        assertBivio(squares, 47, List.of(70), List.of(46));
        assertBivio(squares, 63, List.of(80), List.of(62));
        assertBivio(squares, 70, List.of(71), List.of(17, 47));
        assertBivio(squares, 73, List.of(80), List.of(72));
        assertBivio(squares, 80, List.of(81), List.of(33, 63, 73));

        // collegamenti dei 6 portali: ognuno verso le altre 5 posizioni
        assertPortalLinks(squares, 53, List.of(23, 32, 62, 27, 58));
        assertPortalLinks(squares, 23, List.of(53, 32, 62, 27, 58));
        assertPortalLinks(squares, 32, List.of(53, 23, 62, 27, 58));
        assertPortalLinks(squares, 62, List.of(53, 23, 32, 27, 58));
        assertPortalLinks(squares, 27, List.of(53, 23, 32, 62, 58));
        assertPortalLinks(squares, 58, List.of(53, 23, 32, 62, 27));

        // amount di Snack/Drink (default 1, x2 dove segnato "x2" sul tabellone)
        assertSnackAmount(squares, 28, 1);
        assertSnackAmount(squares, 56, 1);
        assertSnackAmount(squares, 71, 1);
        assertSnackAmount(squares, 84, 1);
        assertSnackAmount(squares, 95, 1);
        assertSnackAmount(squares, 102, 1);

        assertDrinkAmount(squares, 3, 1);
        assertDrinkAmount(squares, 22, 1);
        assertDrinkAmount(squares, 31, 1);
        assertDrinkAmount(squares, 52, 1);
        assertDrinkAmount(squares, 60, 1);
        assertDrinkAmount(squares, 83, 2);
        assertDrinkAmount(squares, 87, 1);
        assertDrinkAmount(squares, 94, 2);
        assertDrinkAmount(squares, 104, 2);
    }

    private void assertBivio(List<Square> squares, int position, List<Integer> next, List<Integer> previous) {
        Square square = squares.get(position);
        assertThat(square).as("position %d", position).isExactlyInstanceOf(BivioSquare.class);
        assertThat(square.getNextPositions()).as("next of %d", position).containsExactlyInAnyOrderElementsOf(next);
        assertThat(square.getPreviousPositions()).as("previous of %d", position).containsExactlyInAnyOrderElementsOf(previous);
    }

    private void assertPortalLinks(List<Square> squares, int position, List<Integer> otherPortals) {
        PortalSquare portal = (PortalSquare) squares.get(position);
        assertThat(portal.getLinkedPositions()).as("portal %d links", position)
                .containsExactlyInAnyOrderElementsOf(otherPortals);
    }

    private void assertSnackAmount(List<Square> squares, int position, int expectedAmount) {
        SnackSquare snack = (SnackSquare) squares.get(position);
        assertThat(snack.getAmount()).as("snack amount at %d", position).isEqualTo(expectedAmount);
    }

    private void assertDrinkAmount(List<Square> squares, int position, int expectedAmount) {
        DrinkSquare drink = (DrinkSquare) squares.get(position);
        assertThat(drink.getAmount()).as("drink amount at %d", position).isEqualTo(expectedAmount);
    }
}
```
Due test: `reconstructsOgBoardFromDatabase` (smoke test iniziale) e `reconstructsAllOgBoardSquaresCorrectly` (regressione completa: tipo esatto delle 68 caselle piazzate + caselle base attese, `next`/`previous` dei 10 bivi, collegamenti dei 6 portali, `amount` di tutte le Snack/Drink). Gli `amount` escludono le righe "effetto" dei bivi (id `1006` Snack, `1047` Drink): non sono posizionate sulla board (`board_id = NULL`), quindi non entrano mai in `board.getSquares()`.


### `test/java/com/project/fumanji/api/GameControllerIT.java`
```java
package com.project.fumanji.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.ControllableDice;
import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.repository.EndGameRepository;
import com.project.fumanji.service.GameRegistry;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class GameControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRegistry gameRegistry;

    @Autowired
    private EndGameRepository endGameRepository;

    @Autowired
    private ControllableDice dice;

    @TestConfiguration
    static class DiceTestConfig {

        @Bean
        @Primary
        public ControllableDice testDice() {
            return new ControllableDice();
        }
    }

    @BeforeEach
    void resetDice() {
        dice.setNextRoll(1);
    }

    private String createGame() throws Exception {
        String responseBody = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerNames": ["Mario", "Luigi"]}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return (String) new JsonPathExpectationsHelper("$.gameId").evaluateJsonPath(responseBody);
    }

    @Test
    void createsGameAndReturnsFirstPlayerAsCurrent() throws Exception {
        String requestBody = """
                {"playerNames": ["Mario", "Luigi"]}
                """;

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId", notNullValue()))
                .andExpect(jsonPath("$.currentPlayerName", is("Mario")));
    }

    @Test
    void rejectsEmptyPlayerNames() throws Exception {
        String requestBody = """
                {"playerNames": []}
                """;

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rollThenMovePassesTurnToNextPlayer() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Mario")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_DIRECTION")))
                .andExpect(jsonPath("$.lastRoll", allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(6))));

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Luigi")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_ROLL")))
                .andExpect(jsonPath("$.choiceOptions").doesNotExist());
    }

    @Test
    void rejectsMoveBeforeRoll() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsSecondRollInSameTurn() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsChoiceWithoutPendingChoice() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position": 10}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMoveWithoutDirection() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownGame() throws Exception {
        mockMvc.perform(post("/api/games/{gameId}/roll", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void savesEndGameAndRemovesGameFromRegistryWhenThereIsAWinner() throws Exception {
        dice.setNextRoll(5);
        Game game = new Game(new Board(5), List.of(new Player("Mario"), new Player("Luigi")), dice);
        UUID gameId = gameRegistry.register(game);
        long endGamesBefore = endGameRepository.count();

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winnerName", is("Mario")));

        assertThat(endGameRepository.count()).isEqualTo(endGamesBefore + 1);
        assertThat(gameRegistry.get(gameId)).isEmpty();
        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsCurrentStateOfExistingGame() throws Exception {
        String gameId = createGame();

        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Mario")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_ROLL")))
                .andExpect(jsonPath("$.players", hasSize(2)));
    }

    @Test
    void returnsNotFoundWhenReadingUnknownGame() throws Exception {
        mockMvc.perform(get("/api/games/{gameId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void choiceMovesPlayerAndContinuesRemainingSteps() throws Exception {
        String gameId = createGame();
        dice.setNextRoll(7);

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")))
                .andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(10, 40)));

        mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position": 10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Luigi")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_ROLL")))
                .andExpect(jsonPath("$.players[0].position", is(10)))
                .andExpect(jsonPath("$.choiceOptions").doesNotExist());
    }

    @Test
    void rejectsInvalidChoice() throws Exception {
        String gameId = createGame();
        dice.setNextRoll(7);

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")));

        mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position": 99}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsObligationMessageWhenLandingOnObligationSquare() throws Exception {
        String gameId = createGame();
        dice.setNextRoll(1);

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(1)))
                .andExpect(jsonPath("$.messages[0]", is("Mario deve dare un obbligo a qualcuno")));
    }

    @Test
    void clearsMessagesOnNextRoll() throws Exception {
        String gameId = createGame();
        dice.setNextRoll(1);

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages", hasSize(1)));

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").doesNotExist());
    }
}
```

I due test sulla scelta usano la casella reale della board OG (Bivio A, posizione 6, `next=[10,40]`); il tiro di 7 (fuori dal range reale 1-6) è un artificio di test per raggiungere la scelta dal via senza simulare più turni. I due test sui messaggi usano la casella 1 della board OG (obbligo generico, messaggio `{player} deve dare un obbligo a qualcuno`): con il dado a 1 la prima mossa dalla partenza atterra lì.


---

## 12. Stato delle verifiche

- **24-09-26 (dal vecchio main)**: dominio Java puro verificato con `javac` diretto in sandbox dopo lo spostamento di package; JPA/MapStruct verificati dall'utente in locale (schema Hibernate generato, nessun errore di compilazione); script SQL di seed della board OG verificato in sandbox su schema gemello MySQL/MariaDB + simulazione del grafo in Python.
- **JUnit (28-09-26 e 29-09-26)**: `BoardAssemblerIT` (smoke + regressione completa della board OG) e `GameControllerIT` (inclusi i due test sulla scelta a bivio) verificati verdi in locale. `GameController` testato con MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) contro lo schema `fumanji_test` seedato.
- **JUnit (30-09-26)**: i due test nuovi sui messaggi (`returnsObligationMessageWhenLandingOnObligationSquare`, `clearsMessagesOnNextRoll`) sono stati aggiunti a `GameControllerIT`, che ora contiene 15 test in totale (`BoardAssemblerIT`: 2); l'utente ha riportato che le modifiche per i messaggi funzionano, un esito esplicito dei due test non è stato riportato.
- **Postman**: `POST /api/games` verificato positivo sul DB reale. I test Postman di `roll`/`move`/`choice`/`GET` sono rimandati dall'utente, da considerare positivi finché non segnala problemi.
- **Sandbox (logica delle fasi del turno)**: verificata compilando il dominio Java puro dal dump con `Game` modificato, con 16 controlli tutti ok: stato iniziale; `move` senza `roll` rifiutato; `choice` senza scelta pendente rifiutata; doppio `roll` rifiutato; `move` che arriva a un bivio → `WAITING_CHOICE` senza avanzare il turno; `roll` con scelta pendente rifiutato; scelta non valida → `IllegalArgumentException` con fase invariata; scelta valida → turno al giocatore successivo; scelta indietro dal via; vittoria (turno non avanza, azioni successive rifiutate); `RollAgainSquare` a catena (4→7); portale con scelta (atterraggio esatto).
- **Frontend (30-09-26)**: l'utente ha verificato in locale benvenuto, inserimento nomi, creazione partita (proxy funzionante, `POST /api/games` restituisce il `gameId`), schermata di gioco (stato, tiro, direzione, scelta, messaggi), una partita completa fino alla fine e la schermata del vincitore ("funziona").
- **Non verificato**: `ng test` non è stato lanciato; gli spec generati dalla CLI per i nuovi componenti (`player-setup`, `game-screen`, `winner`) e per `GameApi` non sono stati eseguiti né adattati (`GameScreen`/`PlayerSetup` usano `Router`, `ActivatedRoute` e `GameApi`, quindi potrebbero richiedere provider di test). Il comportamento al refresh di `/winner` non è stato controllato esplicitamente.
- Il codice Spring/JPA e Angular di queste sessioni è stato verificato dall'utente in locale (compilazione, test, uso reale), non in sandbox.

