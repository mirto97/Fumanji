# FUMANJI — Dump di Progetto

> Archivio esteso, non riassunto. Contiene lo **stato corrente** del progetto: tutto ciò che è stato deciso e il codice così com'è oggi. Non contiene passaggi intermedi superati (es. `GameDemo2/3/4/5`, tutte superate; il vecchio `ObligationSquare` a messaggio fisso, sostituito da quello parametrico; il vecchio clamp del checkpoint backward di sessioni precedenti, già sostituito dal fermo simmetrico). Da caricare a inizio di ogni nuova conversazione per riprendere il lavoro da qui.

---

## 1. Scopo e contesto

Sviluppo di una web application: variante del "Gioco dell'Oca" classico, con caselle speciali che attivano effetti vari (alcuni "stupefacenti"). Stack: **Backend Spring Boot + Frontend Angular** (Angular non ancora iniziato). Il layer di dominio in **Java puro** è stato completato nelle sessioni precedenti; questa sessione ha integrato Spring Boot (scheletro + persistenza JPA) e mappato per intero la board OG reale.

**Repository**: `https://github.com/mirto97/Fumanji.git` (privato)

**Fase attuale del progetto** (decisa questa sessione): si lavora con **una sola board fissa** (la board OG, disegnata a mano da Salame — vedi sezione 9). Il gioco è pensato come multiplayer **locale su un solo dispositivo**: all'avvio si indicano solo il numero di giocatori e i loro nomi, **nessun account/login**. Creazione di board multiple/personalizzate da parte dei giocatori e sistema di autenticazione sono funzionalità future, rimandate a dopo aver completato un frontend funzionante su questa unica board.

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
- Esiste sempre un file di progetto con **"...dump..."** nel nome, contenente lo stato corrente del progetto. Se Claude non lo trova in una nuova chat, deve segnalarlo. Ogni nuova conversazione riparte da lì. A fine conversazione, Claude produce un dump aggiornato: stato corrente, non log storico.
- Prassi consolidata: quando possibile, Claude compila ed esegue il codice nel proprio ambiente sandbox per verificarne la correttezza **prima** di presentarlo, e ripete la regressione su tutto il comportamento già documentato dopo ogni modifica strutturale. **Limite noto** (emerso questa sessione): il sandbox **non ha accesso a Maven Central**, quindi il codice Spring/JPA/MapStruct non è compilabile lì — quella verifica la fa Salame in locale/IDE e riporta eventuali errori. Solo il Java puro (senza dipendenze Spring) resta compilabile in sandbox con `javac` diretto. Per gli script SQL, la verifica si fa invece ricreando lo schema reale (DDL generato da Hibernate) in un MySQL/MariaDB di sandbox e testando lì l'insert, più una simulazione del grafo di gioco.
- Quando una richiesta rivela una portata strutturale più ampia di quanto sembrasse, Claude deve fermarsi e spiegare chiaramente l'impatto reale prima di procedere, piuttosto che minimizzare o iniziare a scrivere codice parziale.
- **Le `GameDemoN` sono checkpoint usa-e-getta**: devono funzionare solo al momento in cui vengono create, per verificare che il codice giri. Quando si riprende a modificare il motore/le caselle, non importa se una `GameDemo` precedente smette di funzionare o di compilare — l'obiettivo è arrivare alla prossima `GameDemo` funzionante, non mantenere le vecchie retrocompatibili. **`GameDemo5` è stata rottura definitivamente questa sessione** (il cambio di firma di `ObligationSquare` la rompe, e va bene così — nessuna nuova demo la sostituisce per ora, si è passati direttamente all'integrazione Spring/persistenza).

---

## 3. Stack tecnologico

| Aspetto | Scelta |
|---|---|
| Linguaggio/runtime | Java 21 |
| Build tool | Maven |
| Framework backend | **Spring Boot 4.1.1** — integrato questa sessione (scheletro + persistenza JPA; controller REST non ancora scritti) |
| Packaging | **WAR** (deploy-ready su servlet container esterno; gestito da `ServletInitializer`, generato da Spring Initializr — vedi sezione 6) |
| Maven group/artifact/package | `com.project` / `fumanji` / `com.project.fumanji` |
| Database | MySQL, via Spring Data JPA (integrato questa sessione) |
| Stile API | REST puro (**no WebSocket**) — nessun controller scritto ancora |
| Frontend | Angular — non ancora iniziato. Quando BE e FE saranno pronti, valutare **Electron** per le fasi successive |
| Autenticazione | Non serve per la fase attuale (vedi sezione 1). Meccanismo (JWT vs sessione) non ancora deciso, ma la libreria **jjwt** è già nel pom |
| Altre dipendenze pom | Lombok, MapStruct, springdoc-openapi (Swagger, path custom `/swagger` e `/apidocs`, entrambi **disattivati** per ora), Spring Security (presente nel pom, **commentata**, "da attivare") |

---

## 4. Struttura cartelle e package

```
Fumanji/
├── BE spring/                      (progetto Maven/Spring)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/project/fumanji/
│       │   │   ├── FumanjiApplication.java
│       │   │   ├── ServletInitializer.java
│       │   │   ├── entity/                      (package: com.project.fumanji.entity — DOMINIO, Java puro)
│       │   │   │   ├── Board.java
│       │   │   │   ├── Dice.java
│       │   │   │   ├── Game.java
│       │   │   │   ├── InteractiveSquare.java
│       │   │   │   ├── Player.java
│       │   │   │   ├── Square.java
│       │   │   │   └── squares/                 (package: com.project.fumanji.entity.squares)
│       │   │   │       ├── BivioSquare.java
│       │   │   │       ├── CheckpointSquare.java
│       │   │   │       ├── DrinkSquare.java
│       │   │   │       ├── MoveBackSquare.java
│       │   │   │       ├── MoveForwardSquare.java
│       │   │   │       ├── ObligationSquare.java
│       │   │   │       ├── PortalSquare.java
│       │   │   │       ├── RollAgainSquare.java
│       │   │   │       ├── SnackSquare.java
│       │   │   │       └── StartOverSquare.java
│       │   │   └── persistence/                 (package: com.project.fumanji.persistence — JPA, dominio separato)
│       │   │       ├── SquareEntity.java
│       │   │       ├── BoardEntity.java
│       │   │       ├── SquareMapper.java
│       │   │       ├── BoardAssembler.java
│       │   │       └── squares/                 (package: com.project.fumanji.persistence.squares)
│       │   │           ├── BivioSquareEntity.java
│       │   │           ├── CheckpointSquareEntity.java
│       │   │           ├── DrinkSquareEntity.java
│       │   │           ├── MoveBackSquareEntity.java
│       │   │           ├── MoveForwardSquareEntity.java
│       │   │           ├── ObligationSquareEntity.java
│       │   │           ├── PortalSquareEntity.java
│       │   │           ├── RollAgainSquareEntity.java
│       │   │           ├── SnackSquareEntity.java
│       │   │           └── StartOverSquareEntity.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/java/com/project/fumanji/...
└── Documentazione/
```

Note:
- Il dominio (`entity`/`entity.squares`) è la stessa gerarchia Java pura delle sessioni precedenti, solo **spostata** sotto `com.project.fumanji` — verificato con `javac` diretto (nessuna dipendenza Spring necessaria per queste classi).
- Il package `demo` (`GameDemo1..5`) **non fa più parte dello stato corrente** — nessuna demo lo sostituisce per ora.
- `persistence` è un package **nuovo di questa sessione**: entity JPA separate dal dominio (vedi sezione 5 per il perché), stessa suddivisione `squares` a specchio del dominio.
- I metodi "di orchestrazione" di `Game` restano **public** per convenzione consolidata (visibilità ampia/future-proof). Eccezioni mirate per vero incapsulamento (`Player.setPosition`/`setCheckpoint`, package-private).

---

## 5. Decisioni di design e logica di dominio

### Board — builder, completamente immutabile, con il nuovo flag di questa sessione

- Campi `finishPosition`, `squares`, `directionChoiceEnabled`, `mandatoryCheckpointEnabled`, **`backwardAllowedAfterCheckpoint`** (nuovo): tutti **final**. `Board` è completamente immutabile una volta costruita.
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
     - `.directionChoiceEnabled(boolean)` / `.mandatoryCheckpointEnabled(boolean)` / **`.backwardAllowedAfterCheckpoint(boolean)`** (nuovo): uniche vie per impostare le regole.
- `getSquares()` ritorna lista immutabile (`List.copyOf`).

### Nuova regola — `backwardAllowedAfterCheckpoint`

- Introdotta questa sessione per la board OG, che richiede: una volta raggiunto il checkpoint, non si può più tornare indietro **oltre la posizione del checkpoint stesso** (si può ancora scegliere "indietro", ma il movimento non supera mai quel punto).
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

### Game — invariato nella struttura, un solo metodo toccato

- `players` chiusa con `List.copyOf` nel costruttore (sessione precedente, invariato).
- Guardrail anti-loop (`activatedPositions`/`chainInProgress`), movement trace, meccanismo bivio/portale unificato (`isAwaitingChoice`/`getChoiceOptions`/`chooseDestination`/`requestChoice`): tutto invariato rispetto alle sessioni precedenti.
- L'unico cambiamento è in `moveBackwardByInternal`, descritto sopra.

### Dado — invariato
- 1 dado per turno, valori 1-6, `ThreadLocalRandom`.

### Checkpoint — invariato (fermo simmetrico avanti/indietro, sessione precedente)
- Forward e backward si fermano sulla prima casella `isMandatoryStop()` incontrata quando `mandatoryCheckpointEnabled` è attivo. Il proprio checkpoint si aggiorna sempre all'ultimo su cui si è atterrati (`CheckpointSquare.applyEffect`).
- **Nuovo dettaglio di comportamento per la board OG** (non codice, conseguenza dei dati): il checkpoint di questa board è unico, quindi `player.getCheckpoint() > 0` identifica in modo affidabile "il giocatore l'ha già raggiunto" — soluzione cucita su "un solo checkpoint", da rivedere se una board futura ne avesse più di uno in sequenza.

### Direction choice — invariato
- `Game.roll`, `Game.rollAndMove`, `Game.moveByChosenDirection` invariati, fallback silenzioso ad avanti se la regola non è attiva.

### Caselle interattive (`InteractiveSquare`, Bivio, Portale) — invariato nel meccanismo, `BivioSquare` esteso

- Meccanismo di innesco (Bivio a metà mossa, Portale solo su atterraggio esatto) e risoluzione unificata in `Game`: invariati.
- **`BivioSquare` — nuovo campo opzionale `effect`** (questa sessione): un `Square` "delegato" usato **solo** per il suo `applyEffect`, mai per posizione/routing. Nasce dall'esigenza della board OG, dove alcuni bivi (visti sul tabellone fisico) attivano anche un obbligo/snack/drink/startover oltre a far scegliere la direzione — cosa che la struttura precedente non supportava (`BivioSquare` non sovrascriveva mai `applyEffect`).
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
          System.out.println(buildMessage(player, game));
      }

      public String buildMessage(Player player, Game game) {
          return message.replace("{player}", player.getName());
      }
  }
  ```
- **Effetto collaterale accettato**: `GameDemo5` (che chiamava `new ObligationSquare(13)` senza messaggio) non compila più — non è stata aggiornata, coerente con la filosofia "usa e getta" delle demo.
- `CheckpointSquare` resta invariata — nessun template, messaggio fisso già confermato nelle sessioni precedenti.
- I 6 template concreti usati sulla board OG sono elencati in sezione 9.

### addSnack / addDrink — invariati
- `Game.addSnack(player, amount)` / `Game.addDrink(player, amount)`, clamp a 0, nessuna variante "per tutti i giocatori" (il chiamante itera su `game.getPlayers()` se serve). Pensati per essere richiamati direttamente dal futuro FE.

### Persistenza JPA — dominio separato (scelta di questa sessione)

- **Il problema**: annotare direttamente `Square`/`Board` (le classi di dominio) con JPA confligge con l'immutabilità costruita apposta nelle sessioni precedenti (builder, campi `final`, niente setter). Il punto delicato non sono i campi singoli (Hibernate li popola comunque via reflection con un costruttore no-arg protetto) ma le **collection**: `Board.squares` è una `List.copyOf(...)` immutabile assegnata una volta sola — con `@OneToMany` Hibernate ha bisogno di gestire lui la collection (lazy loading, dirty checking), cosa che un campo `final` puntato a una lista immutabile non permette in modo pulito. Stesso discorso per le `List<Integer>` di `BivioSquare`/`PortalSquare` mappate con `@ElementCollection`.
- **Due strade valutate**:
  - **A — stesse classi, meno immutabili**: le collection diventano mutabili, più un costruttore no-arg protetto. Nessuna classe nuova, ma il refactor di immutabilità di `Board` fatto nelle sessioni precedenti va parzialmente ammorbidito.
  - **B — dominio separato da JPA** (scelta): `Square`/`Board`/le 10 sottoclassi **restano esattamente come sono**, zero compromessi. Si aggiunge una gerarchia di entity JPA a parte (package `persistence`), con un mapper MapStruct per convertire in dominio. **Motivazione esplicita di Salame**: è anche il passo già previsto in futuro per il factory pattern delle caselle (JSON `type` → istanza `Square`), quindi tanto vale prepararlo da subito.
- **Struttura scelta**: `SquareEntity` **abstract** (base, `@Inheritance(JOINED)`, discriminator `square_type`) — abstract perché non esiste mai una riga "casella normale" in tabella (l'assenza di riga *è* l'informazione "casella base", stesso principio del builder che sovrascrive solo le caselle speciali). 10 sottoclassi entity a specchio del dominio. `BoardEntity` con `@OneToMany(mappedBy="board", cascade=ALL, orphanRemoval=true)`.
- **Assemblaggio Board↔BoardEntity**: non è mappatura dichiarativa pura (ricostruire `Board` richiede passare dal builder), quindi vive in una **classe separata** (`BoardAssembler`, non un `default` method dentro l'interfaccia MapStruct) — scelta esplicita di Salame, per tenere nette le interfacce MapStruct (`SquareMapper`) come puramente dichiarative.
- **Mapping delle sottoclassi** (`SquareMapper`, `@SubclassMapping` per ognuna delle 10): MapStruct usa l'unico costruttore di ogni classe dominio e abbina i parametri per nome ai getter dell'entity corrispondente — funziona perché dominio ed entity sono compilati nello stesso modulo (annotation processing legge i nomi reali dal sorgente, non serve il flag `-parameters`).
- **Warning innocuo noto**: MapStruct segnala "Unmapped target properties: nextPositions, previousPositions" per le 9 sottoclassi che non sono `BivioSquare`, perché `Square.getNextPositions()`/`getPreviousPositions()` sono metodi pubblici definiti sulla classe base (calcolano `position±1` di default) ed ereditati da tutte le sottoclassi — MapStruct li vede come "proprietà" leggibili anche dove non c'è nulla da mappare. Il codice generato resta corretto (quei getter continuano a calcolare da `position` come sempre). Silenziabile con `unmappedTargetPolicy = ReportingPolicy.IGNORE` sul `@Mapper` (facoltativo, mai applicato).
- **`BivioSquare.effect` in persistenza**: `BivioSquareEntity` ha un `@ManyToOne(cascade = CascadeType.ALL) effect` verso un'altra riga di `SquareEntity`. Quella riga "effetto" **non è posizionata su nessuna board** (`board_id = NULL`, quindi mai vista da `BoardAssembler`/dalla collection `@OneToMany` di `Board`) — per questo `square_entity.board_id` è **nullable** (non più `NOT NULL` come alla primissima stesura). Il cascade `ALL` sulla relazione `effect` garantisce che, cancellando un `BivioSquareEntity` (che a sua volta viene cancellato in cascata insieme alla sua board), venga cancellata anche la sua riga-effetto — altrimenti resterebbe orfana. **Convenzione id/posizione per queste righe**: `id = 1000 + posizione del bivio proprietario` (es. `1006` = effetto del bivio in posizione 6), `position = 0` come placeholder inerte (mai letto per queste righe).

### Debiti tecnici noti (aggiornato)
- `playTurn()` non gestisce la pausa di `isAwaitingChoice()` — per design, resta un problema da risolvere quando si deciderà il flusso "vero" lato API (dove servirà esporre lo stato di attesa al frontend).
- Guardrail anti-loop: limite noto con pausa Bivio a metà catena (sessioni precedenti) — accettato.
- **Nuovo limite noto**: la regola `backwardAllowedAfterCheckpoint` vive in `moveBackwardByInternal`, quindi copre sia la scelta volontaria sia le caselle che spingono indietro con la forza (`MoveBackSquare`) — ma **solo** quelle. Non esiste, e per ora non serve, un controllo analogo lato "avanti" (non avrebbe senso: non si può "superare" il checkpoint andando avanti, lo si raggiunge e basta).
- **Stato di `Game` tra le richieste REST** (stateless) — ancora non deciso, discusso e rimandato più volte, ora **bloccante** per scrivere i controller (vedi TODO, sezione 7).
- `BoardRepository` — non esiste ancora nessuna `JpaRepository<BoardEntity, Long>`, quindi nulla va ancora a leggere `BoardEntity` dal database (vedi TODO).

---

## 6. Codice sorgente — versione aggiornata

Il dominio (`entity`/`entity.squares`) è stato verificato con `javac` diretto in sandbox dopo lo spostamento di package (nessuna rottura). Il codice Spring/JPA/MapStruct **non è compilabile in sandbox** (niente Maven Central) — scritto con cura ma la verifica reale è quella fatta da Salame in locale (schema Hibernate generato con successo, nessun errore di compilazione riportato).

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
            // secondo pavimento (nuovo questa sessione): se attivo, non si supera mai (indietro)
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
        System.out.println(buildMessage(player, game));
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
        System.out.println(buildMessage(player));
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

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
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
import jakarta.persistence.JoinColumn;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class PortalSquareEntity extends SquareEntity {

    @ElementCollection
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class BivioSquareEntity extends SquareEntity {

    @ElementCollection
    @CollectionTable(name = "bivio_square_next", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "next_position")
    private List<Integer> nextPositions = new ArrayList<>();

    @ElementCollection
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

---

## 7. TODO / Prossimi passi (aggiornato)

### Priorità molto alta (confermate questa sessione, in ordine)
1. **Lanciare lo script SQL della board OG** (sezione 9) sul database `fumanji` reale — finora verificato solo su uno schema gemello ricreato in sandbox.
2. **`BoardRepository`** — non esiste ancora nessuna `JpaRepository<BoardEntity, Long>`: `BoardAssembler` sa trasformare un `BoardEntity` in `Board`, ma nessun pezzo di codice lo va ancora a *prendere* dal database.
3. **Prova end-to-end** — verificare che `BoardRepository` + `BoardAssembler` insieme ricostruiscano davvero la `Board` giusta dai dati appena seedati, prima di fidarcene per il resto.
4. **Controller REST** — zero endpoint scritti finora. Riporta a galla la domanda rimandata più volte: **stato di `Game` tra le richieste REST** (stateless) — persistito su MySQL ad ogni mossa, tenuto in memoria lato server (cache per gameId), o un mix — a questo punto diventa bloccante, non più rimandabile.

### Prossimo passo pianificato dopo le priorità sopra
- Frontend Angular (schermata iniziale: numero giocatori + nomi, nessun login per questa fase).

### Rimandato a dopo la base BE+FE funzionante
- **Board multiple / creabili dai giocatori** — per ora una sola board fissa (vedi sezione 1). Quando arriverà: il factory pattern per le caselle (JSON con attributo `type` → istanza `Square`), riusando probabilmente `SquareMapper`/`BoardAssembler` come base.
- **Bivio con percorsi non riconvergenti** — per ora tutti i percorsi si ricongiungono prima del traguardo (vedi board OG, sezione 9); in futuro percorsi che non si ricongiungono mai, col rimbalzo di fine tabellone che in quel caso cambierebbe solo il senso di marcia.
- **Board con più caselle di arrivo** — conseguenza diretta del punto sopra.
- **Autenticazione (JWT vs sessione)** — non serve per la fase attuale (nessun account).
- **Valutare Electron** per le fasi successive, quando BE e FE saranno pronti.
- **Obbligo con bersaglio specifico** — per ora `ObligationSquare` non nomina mai un giocatore bersaglio (solo messaggi generici/parametrici su chi ha attivato la casella); in futuro potrebbe scegliere un bersaglio tra `game.getPlayers()`.

### Deciso di non fare / rimosso dal backlog (per chiarezza storica — non voci attive)
- Altri tipi di caselle speciali oltre ai 10 attuali — non in programma per ora.
- `Game.getLastRoll()` esteso a mostrare gli step intermedi di una catena — deciso che non serve.
- Portale con limitazioni oltre alla lista esplicita — già risolto, ogni istanza ha la propria lista.
- Frontend Angular come voce di backlog a sé — resta nel piano generale (sezione 3).
- Controllo di `backwardAllowedAfterCheckpoint` anche per la scelta volontaria in `moveByChosenDirection` — non era quello richiesto (vedi sezione 5, distinzione importante chiarita questa sessione), rimane solo in `moveBackwardByInternal`.

### Fatto in questa sessione (non più TODO)
- ~~Integrazione Spring Boot~~ → scheletro completo (WAR, `FumanjiApplication`/`ServletInitializer`, `application.properties`), persistenza JPA per `Board`/`Square` (dominio separato + MapStruct + `BoardAssembler`). Restano solo repository e controller (vedi priorità alta sopra).
- ~~Costruire la board OG~~ → mappata per intero (sezione 9) e script di seed scritto e verificato.
- ~~`BivioSquare` con effetto~~ → campo opzionale `effect`, retrocompatibile.
- ~~Regola "non si torna indietro oltre il checkpoint"~~ → `Board.backwardAllowedAfterCheckpoint`, implementata in `moveBackwardByInternal`.
- ~~`ObligationSquare` con messaggi diversi~~ → messaggio parametrico per istanza con placeholder `{player}`.
- ~~Definire la fase/scopo attuale del progetto~~ → una sola board fissa, multiplayer locale su singolo dispositivo, niente account (sezione 1).

---

## 8. Decisioni "minori" ma da ricordare

- Traduzione "obbligo" → `ObligationSquare` (nome definitivo, invariato).
- Package dominio: `com.project.fumanji.entity` e `.entity.squares`. Package persistenza: `com.project.fumanji.persistence` e `.persistence.squares` (nuovo questa sessione, scelto per non confondersi col nome "entity" già usato dal dominio).
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
- Sandbox: niente Maven Central raggiungibile — Spring/JPA/MapStruct verificati da Salame in locale, non da Claude. Script SQL invece verificabili in sandbox ricreando lo schema Hibernate reale in MySQL/MariaDB + simulazione del grafo in Python.

---

## 9. Board OG — dati completi (mappata questa sessione da un tabellone disegnato a mano da Salame)

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

Verificato ricreando lo schema esatto generato da Hibernate (incluse le modifiche di questa sessione: `board_id` nullable, `effect_square_id`, `message`, `backward_allowed_after_checkpoint`) in un MySQL/MariaDB di sandbox, più una simulazione in Python del grafo di gioco (BFS da 0, segue `next`/`bivio_square_next` come farebbe il motore): raggiunge 110, tutte le 68 caselle piazzate vengono toccate, i 6 gap "no smoke" tornano dove previsto, nessuna incoerenza `next`/`previous` sui 10 bivi, ogni portale ha esattamente gli altri 5 collegamenti.

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
