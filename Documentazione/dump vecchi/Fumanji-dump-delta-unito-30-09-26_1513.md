# FUMANJI — Dump di sessione (delta unito)

> **Data e ora**: 30/09/2026 (Europe/Rome).
> **Natura del file**: è l'**unione dei tre delta** del 28-09-26 (04:29), del 29-09-26 e del 30-09-26. Contiene solo ciò che è stato fatto/deciso da quelle sessioni in poi, con la **versione finale** di ogni file (dove un file è stato toccato in più sessioni compare una sola volta, già aggiornato). **Non** contiene il dump principale del 24-09-26 (`Fumanji-main-dump-24-09-26.md`), che resta valido per tutto ciò che qui non compare e con cui questo file andrà unito in un secondo momento.
> Dump snello: cose cancellate/superate/obsolete non compaiono (es. il vecchio `FixedDice` dei test, lo stato di `GameStateResponse` senza `messages`, i TODO già chiusi). Archivio esteso, non riassunto: il codice riportato è quello **finale**.

---

## 1. Cosa è stato fatto (indice)

**Sessione 28-09-26**
- `BoardRepository` creato, in un nuovo package `com.project.fumanji.repository`.
- DB `fumanji` droppato e ricreato; script SQL della board OG lanciato sul DB reale.
- Infrastruttura di test: schema `fumanji_test`, profilo `test`, seed via `@Sql`; prima prova end-to-end (`BoardAssemblerIT`) verde.
- Fix `LazyInitializationException`: fetch `EAGER` su tutte le collection della persistenza.
- Stato di `Game` tra le richieste REST: **deciso** (in memoria, `GameRegistry`, salvataggio solo a fine partita).
- Layer applicativo e REST completo per una partita intera: `GameRegistry`, `GameService`, `EndGameService`, `GameController`, DTO, gestione errori, `GameStateAssembler`.
- Dominio: **fasi del turno** (`TurnPhase`) e metodi di turno in `Game`.
- Salvataggio a fine partita: `EndGameEntity` + `EndGameRepository` + `EndGameService`.
- Endpoint `GET` per rileggere lo stato di una partita.
- Test di integrazione JUnit (MockMvc) su tutti gli endpoint.

**Sessione 29-09-26**
- `Dice` reso iniettabile (bean Spring), con test dedicati.
- `playTurn()`: deciso di mantenerlo, non rimosso.
- `BoardAssemblerIT`: regressione completa della board OG (prima era solo uno smoke test).
- TTL sulle partite abbandonate (`GameRegistry`): confermato rimandato, non necessario ora.

**Sessione 30-09-26**
- Cartelle del repo riorganizzate: `BE/spring` e `FE/angular`.
- Frontend Angular avviato: progetto creato, versione verificata (Angular 21.2.24), proxy verso il backend.
- Quattro schermate funzionanti: benvenuto, inserimento nomi, gioco (stato + azioni del turno + messaggi), vincitore.
- Backend: messaggi delle caselle (obblighi e checkpoint) esposti nell'API invece di essere stampati su console.
- Partita completa giocata dal frontend, tutto come previsto.

---
## 2. Modalità di lavoro e ambiente — novità

- **Dump snello**: cose cancellate/obsolete/superate non vanno nel dump. Il dump di fine conversazione è solo il delta della sessione, l'unione la fa l'utente a parte (o si fa su richiesta, a step: prima i delta fra loro, poi con il dump principale).
- **Sandbox — aggiornamento del "limite noto"**: nel sandbox non c'è il binario `javac`, ma il runtime contiene il modulo `jdk.compiler`, quindi il **Java puro** si compila con `java -m jdk.compiler/com.sun.tools.javac.Main ...`. Spring/JPA/MapStruct restano non compilabili (niente Maven Central): quella verifica la fa l'utente in locale.
- **Ambiente dell'utente**: VS Code (non IntelliJ/Eclipse), Windows + PowerShell. Maven **non** è installato globalmente: si usa il wrapper `.\mvnw.cmd` **dalla cartella del backend** (nella root `Fumanji` non esiste; dal 30-09-26 il percorso è `BE\spring`, prima era `BE spring`). I test JUnit si lanciano da VS Code (icone accanto ai `@Test` / pannello Test Results).
- **PowerShell + Maven**: `-Dincludes=g:a` con i due punti viene interpretato male da PowerShell; per cercare una dipendenza si redirige `dependency:tree` su file e si usa `Select-String`.
- **MySQL Workbench**: l'autocompletamento può duplicare il testo mentre si digita/incolla uno script (errore 1064 con `board_entityboard_entityboard_entity`): usare una tab di query nuova e incollare senza editare.
- **`ddl-auto=update` non rende nullable una colonna già esistente `NOT NULL`**: per applicare la modifica di `board_id` è servito droppare e ricreare il DB.
- **Preferenza dell'utente sui test**: vuole poter fissare il risultato del dado nei test per coprire vari casi → realizzato il 29-09-26 con `ControllableDice` (vedi sezione 4, "Dice iniettabile").
- **Angular/TypeScript**: l'utente non è ancora pratico (a differenza di Spring/Java). Non vuole la spiegazione di ogni pezzo di codice: chiede lui quando servono spiegazioni. Le cose vanno comunque verificate insieme spesso.
- **Commenti nel codice TypeScript/Angular**: sempre presenti sui pezzi di logica (italiano, asciutti, da codice di progetto), così l'utente può riguardare e studiare ciò che non conosce. Per il Java resta la regola precedente (commenti solo se richiesti).
- **Frontend**: si parte da componenti base al volo e poi si rifiniscono.
- **Libro di riferimento**: l'utente ha studiato Angular su un libro fino alla versione 20.3.13. Il progetto usa la 21.2.24: se qualcosa nel libro non corrisponde al progetto, si controlla insieme.
- **Versioni installate**: Angular CLI 21.2.24, Angular 21.2.24, TypeScript 5.9.3, Node 24.13.0, npm 11.6.2, rxjs 7.8.2, Vitest 4.1.11 (test runner di default in 21, al posto di Karma/Jasmine).
- **Zoneless**: `app.config.ts` non contiene nessun provider di zone.js/`provideZoneChangeDetection`, quindi il progetto è zoneless (default in 21). Lo stato che si vede a schermo sta in `signal()`/`computed()`. L'utente conosce i signal.
- **Comandi**: `ng ...` da `FE\angular`; `.\mvnw.cmd ...` da `BE\spring` (nuovo percorso). Dopo lo spostamento del backend il Java language server di VS Code aveva perso i package: si risolve con la palette comandi → **Java: Clean Java Language Server Workspace** (restart and delete), riaprendo VS Code sulla cartella radice `Fumanji`.
- **Proxy**: `ng serve` legge `proxy.conf.json` solo all'avvio, quindi dopo modifiche a `proxy.conf.json`/`angular.json` va riavviato. Il backend va avviato prima, con MySQL acceso e board OG seedata sul DB `fumanji`.
- Nessuna verifica in sandbox in questa sessione: codice Angular e Spring verificati dall'utente in locale.

---

## 3. Struttura cartelle (aggiornata)

```
Fumanji/
├── BE/
│   └── spring/                              (era "BE spring"; build e test verificati dal nuovo percorso)
│       ├── pom.xml                          (aggiunta dipendenza test spring-boot-starter-webmvc-test)
│       └── src/
│           ├── main/java/com/project/fumanji/
│           │   ├── api/                     (NUOVO)
│           │   │   └── GameController.java
│           │   ├── config/                  (NUOVO)
│           │   │   └── GameConfig.java
│           │   ├── dto/                     (NUOVO — record)
│           │   │   ├── ChoiceRequest.java
│           │   │   ├── CreateGameRequest.java
│           │   │   ├── CreateGameResponse.java
│           │   │   ├── ErrorResponse.java
│           │   │   ├── GameStateResponse.java
│           │   │   ├── MoveRequest.java
│           │   │   └── PlayerStateResponse.java
│           │   ├── entity/                  (dominio: aggiunto TurnPhase; modificati Game, SnackSquare, DrinkSquare, PortalSquare, ObligationSquare, CheckpointSquare)
│           │   │   └── TurnPhase.java
│           │   ├── exception/               (NUOVO)
│           │   │   ├── GameNotFoundException.java
│           │   │   └── GlobalExceptionHandler.java
│           │   ├── mapper/                  (NUOVO)
│           │   │   └── GameStateAssembler.java
│           │   ├── persistence/             (aggiunto EndGameEntity; fetch EAGER in BoardEntity, BivioSquareEntity, PortalSquareEntity)
│           │   │   └── EndGameEntity.java
│           │   ├── repository/              (NUOVO)
│           │   │   ├── BoardRepository.java
│           │   │   └── EndGameRepository.java
│           │   └── service/                 (NUOVO)
│           │       ├── EndGameService.java
│           │       ├── GameCreationResult.java
│           │       ├── GameRegistry.java
│           │       └── GameService.java
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

- Convenzione di package che l'utente segue di solito (vista in un progetto passato, EventHub): `api`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, adattata caso per caso. Per Fumanji, oltre a `entity`/`entity.squares` (dominio) e `persistence`/`persistence.squares` (JPA): `repository` (interfacce `JpaRepository`), `service` (`GameRegistry`, `GameService`, `EndGameService`, `GameCreationResult`), `api` (controller), `dto` (record), `exception`, `mapper` (`GameStateAssembler`, scritto a mano come `BoardAssembler`, non MapStruct: non è mappatura dichiarativa pura), `config` (`GameConfig`).
- I DTO sono `record` (contenitori dati immutabili per JSON, accessor `campo()` invece di `getCampo()`, Jackson li gestisce nativamente).
- I file generati dalla CLI 21 non hanno il suffisso `.component` (es. `welcome.ts`, classe `Welcome`; anche `app.ts` con classe `App`).
- Convenzione per i modelli frontend: un'interfaccia per file, in `models/`, che rispecchia i DTO del backend (`dto`). Per il service: cartella `services/`, classe `GameApi`.
- `tsconfig.app.json` modificato dall'utente perché VS Code segnalava errori: contiene `"rootDir": "./src"` insieme a `outDir`, `types: []`, `include: src/**/*.ts`, `exclude: src/**/*.spec.ts`.

---

## 4. Decisioni di design — backend

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

### Package (convenzione dell'utente)
- Convenzione che l'utente segue di solito (vista in un progetto passato, EventHub): `api`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, adattata caso per caso.
- Per Fumanji, oltre a `entity`/`entity.squares` (dominio) e `persistence`/`persistence.squares` (JPA): `repository` (interfacce `JpaRepository`), `service` (`GameRegistry`, `GameService`, `EndGameService`, `GameCreationResult`), `api` (controller), `dto` (record), `exception`, `mapper` (`GameStateAssembler`, scritto a mano come `BoardAssembler`, non MapStruct: non è mappatura dichiarativa pura).
- I DTO sono `record` (contenitori dati immutabili per JSON, accessor `campo()` invece di `getCampo()`, Jackson li gestisce nativamente).

### Test di integrazione
- Schema dedicato `fumanji_test` (da creare a mano una volta, stesso server MySQL), profilo `test`, `ddl-auto=create-drop`.
- Seed via `@Sql("/seed-og-board.sql")` (`src/test/resources`, copia identica dello script di seed della board OG, sezione 9 del dump principale).
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

---

## 5. Decisioni di design — frontend

### Routing e flusso
- Navigazione con il **router Angular**, per poter ricaricare la pagina senza perdere la partita (il `GET /api/games/{gameId}` serve a questo).
- Rotte: `''` (benvenuto), `players` (nomi), `game/:gameId` (partita), `winner` (vincitore).
- Flusso: benvenuto → nomi → (all'invio) creazione partita sul backend → `/game/:gameId` → alla vittoria `/winner`.

### Collegamento al backend: proxy
- Scelto il **proxy del dev server Angular** (`proxy.conf.json`, prassi abituale dell'utente) invece del CORS in Spring: frontend su `localhost:4200`, backend su `8080`, il frontend chiama percorsi relativi `/api/...`.
- Il proxy vale solo per `ng serve`: come verrà servito il frontend in produzione non è ancora deciso.

### Schermata di benvenuto
- Click ovunque nella pagina per proseguire (contenitore `position: fixed; inset: 0` con `(click)`). Estetica non prioritaria: per ora conta solo che funzioni.

### Schermata dei nomi (`player-setup`)
- Stato con signal + `(input)`, senza moduli form.
- All'apertura **2 box**; **minimo 2** e **massimo 10** giocatori (limiti solo nel frontend: il backend accetta qualsiasi lista non vuota); pulsante per aggiungere box e pulsante "Rimuovi" per box.
- L'ordine dei box è l'ordine di gioco.
- "Inizia" disabilitato finché un nome è vuoto/blank (il backend risponderebbe `400`) e mentre una richiesta è in corso (evita partite doppie con click ripetuti). I nomi vengono `trim()`-ati prima dell'invio.
- **Nomi doppi non bloccati**: l'uso previsto è dell'utente e di pochi amici fidati, si conta sull'intelligenza di chi usa l'app purché non si rompa. L'utente proverà a romperla in ogni modo più avanti, per sistemarla e migliorarla insieme.
- In caso di errore alla creazione, messaggio provvisorio sotto i pulsanti.

### Schermata di gioco (`game-screen`)
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

### Schermata del vincitore (`winner`)
- Mostra "Ha vinto X!" e un link "Nuova partita" verso `/players`.
- Il nome arriva con lo **stato della navigazione del router** (`state: { winnerName }`) e si legge da `history.state`. Motivo: alla vittoria il backend toglie la partita dal registro, quindi il vincitore non è più rileggibile con il `GET`. Aperta direttamente senza nome, la schermata mostra "Nessun vincitore da mostrare."
- Il comportamento al refresh della pagina `/winner` non è stato verificato esplicitamente.
- Per ora basta questo: altro (per esempio le posizioni finali) si aggiungerà dopo.

---

## 6. Codice backend — versioni finali

### `pom.xml` — dipendenza aggiunta (dentro `<dependencies>`, insieme alle altre di test)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
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

### `entity/Game.java` — solo le aggiunte/modifiche (nient'altro è stato toccato)

Campi, insieme agli altri:
```java
private TurnPhase turnPhase = TurnPhase.WAITING_ROLL;
private final List<String> turnMessages = new ArrayList<>();
```

Metodi, subito prima di `roll(Player player)`:
```java
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
```

Getter e metodi messaggi, subito prima di `getBoard()`:
```java
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
```


### Dominio — modifiche alle caselle

`entity/squares/PortalSquare.java` — nuovo getter:
```java
public List<Integer> getLinkedPositions() {
    return linkedPositions;
}
```

`entity/squares/SnackSquare.java` e `entity/squares/DrinkSquare.java` — nuovo getter (in entrambe):
```java
public int getAmount() {
    return amount;
}
```

`entity/squares/ObligationSquare.java` — `applyEffect` (invece di stampare su console, aggiunge il messaggio a `Game`):
```java
@Override
public void applyEffect(Player player, Game game) {
    game.addMessage(buildMessage(player, game));
}
```

`entity/squares/CheckpointSquare.java` — `applyEffect`:
```java
@Override
public void applyEffect(Player player, Game game) {
    game.updateCheckpoint(player);
    game.addMessage(buildMessage(player));
}
```


### Persistenza — modifiche EAGER (solo righe cambiate)

`persistence/BoardEntity.java`:
```java
import jakarta.persistence.FetchType;
```
```java
@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
private List<SquareEntity> squares = new ArrayList<>();
```

`persistence/squares/BivioSquareEntity.java`:
```java
import jakarta.persistence.FetchType;
```
```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "bivio_square_next", joinColumns = @JoinColumn(name = "square_id"))
@Column(name = "next_position")
private List<Integer> nextPositions = new ArrayList<>();

@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "bivio_square_previous", joinColumns = @JoinColumn(name = "square_id"))
@Column(name = "previous_position")
private List<Integer> previousPositions = new ArrayList<>();
```

`persistence/squares/PortalSquareEntity.java`:
```java
import jakarta.persistence.FetchType;
```
```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "portal_square_link", joinColumns = @JoinColumn(name = "square_id"))
@Column(name = "linked_position")
private List<Integer> linkedPositions = new ArrayList<>();
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

## 7. Codice frontend — versioni finali

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

## 8. Test e risorse di test

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
Copia identica, senza modifiche, dello script di seed della sezione 9 del dump principale.

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

## 9. Stato delle verifiche

- **JUnit (28-09-26 e 29-09-26)**: `BoardAssemblerIT` (smoke + regressione completa della board OG) e `GameControllerIT` (inclusi i due test sulla scelta a bivio) verificati verdi in locale. `GameController` testato con MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) contro lo schema `fumanji_test` seedato.
- **JUnit (30-09-26)**: i due test nuovi sui messaggi (`returnsObligationMessageWhenLandingOnObligationSquare`, `clearsMessagesOnNextRoll`) sono stati aggiunti a `GameControllerIT`, che ora contiene 15 test in totale (`BoardAssemblerIT`: 2); l'utente ha riportato che le modifiche per i messaggi funzionano, un esito esplicito dei due test non è stato riportato.
- **Postman**: `POST /api/games` verificato positivo sul DB reale. I test Postman di `roll`/`move`/`choice`/`GET` sono rimandati dall'utente, da considerare positivi finché non segnala problemi.
- **Sandbox (logica delle fasi del turno)**: verificata compilando il dominio Java puro dal dump con `Game` modificato, con 16 controlli tutti ok: stato iniziale; `move` senza `roll` rifiutato; `choice` senza scelta pendente rifiutata; doppio `roll` rifiutato; `move` che arriva a un bivio → `WAITING_CHOICE` senza avanzare il turno; `roll` con scelta pendente rifiutato; scelta non valida → `IllegalArgumentException` con fase invariata; scelta valida → turno al giocatore successivo; scelta indietro dal via; vittoria (turno non avanza, azioni successive rifiutate); `RollAgainSquare` a catena (4→7); portale con scelta (atterraggio esatto).
- **Frontend (30-09-26)**: l'utente ha verificato in locale benvenuto, inserimento nomi, creazione partita (proxy funzionante, `POST /api/games` restituisce il `gameId`), schermata di gioco (stato, tiro, direzione, scelta, messaggi), una partita completa fino alla fine e la schermata del vincitore ("funziona").
- **Non verificato**: `ng test` non è stato lanciato; gli spec generati dalla CLI per i nuovi componenti (`player-setup`, `game-screen`, `winner`) e per `GameApi` non sono stati eseguiti né adattati (`GameScreen`/`PlayerSetup` usano `Router`, `ActivatedRoute` e `GameApi`, quindi potrebbero richiedere provider di test). Il comportamento al refresh di `/winner` non è stato controllato esplicitamente.
- Il codice Spring/JPA e Angular di queste sessioni è stato verificato dall'utente in locale (compilazione, test, uso reale), non in sandbox.

---

## 10. TODO / prossimi passi (aggiornato al 30-09-26)

### Aperti
1. **Tabellone** (prossimo pezzo grosso del frontend): servono un endpoint nuovo con caselle e collegamenti (oggi nessun endpoint li restituisce) e la decisione su come disegnare 110 caselle con rami, bivi e portali.
2. **Schermata del vincitore**: aggiungere altro (per esempio posizioni finali di tutti i giocatori).
3. **Spec generati dalla CLI** (`ng test`, Vitest): eseguirli e adattarli o rimuoverli.
4. **Provvisori da rifinire**: fase mostrata come valore grezzo, testi dell'interfaccia, messaggi d'errore, estetica in generale.
5. **Nomi doppi**: non bloccati per scelta; da riconsiderare quando l'utente proverà a "rompere" l'app.
6. **Proxy solo per sviluppo**: da decidere come verrà servito il frontend in produzione.
7. **Ordine dei turni lato frontend**: oggi l'ordine dei box è l'ordine di gioco; in futuro si potrebbe far impostare l'ordine esplicitamente ("Giocatore 1", "Giocatore 2"…, dove i giocatori scrivono il nome di chi inizia, poi chi è secondo, ecc.).
8. **Test Postman** di `roll`/`move`/`choice`/`GET`: a carico dell'utente, li fa appena ha tempo e scrive solo se sono negativi.

### Rimandati (backend)
- **TTL** per le partite abbandonate in `GameRegistry` (`@Scheduled`) — non necessario ora (nessun deploy con uptime lungo/utenti concorrenti reali).
- **Storico/statistiche complete a fine partita**: ridisegnare `Game.movementTrace` per-giocatore, definire quali statistiche tracciare, estendere `EndGameService` (che già riceve l'intero `Game`).
- **Rivalutare EAGER** e l'`OG_BOARD_ID` fisso in `GameService` (`createGame` e `closeIfFinished`) — quando si supporteranno board multiple.

### Chiusi
- ~~`BoardRepository`, script SQL sul DB reale, prova end-to-end~~ → fatto (28-09-26).
- ~~Stato di `Game` tra le richieste REST, controller REST~~ → fatto (28-09-26).
- ~~Rendere `Dice` iniettabile~~ → `GameConfig` + `GameService` + `ControllableDice` (29-09-26).
- ~~`playTurn()`: rimuovere o mantenere~~ → mantenuto (29-09-26).
- ~~`BoardAssemblerIT`: regressione completa e `executionPhase`~~ → fatto (29-09-26).
- ~~Test JUnit con dado fisso per il caso `WAITING_CHOICE` end-to-end~~ → coperto a livello HTTP in `GameControllerIT` (29-09-26).
- ~~Frontend Angular: schermata iniziale con numero di giocatori + nomi, gestione delle risposte `404`/`409`/`400`~~ → fatto, quattro schermate funzionanti (30-09-26).
- ~~Messaggi delle caselle (obblighi/checkpoint) esposti nell'API~~ → fatto (30-09-26).
