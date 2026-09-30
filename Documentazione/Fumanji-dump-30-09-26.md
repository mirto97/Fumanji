# FUMANJI — Dump di sessione (delta)

> **Data**: 30/09/2026 (Europe/Rome).
> **Natura del file**: contiene solo ciò che è stato fatto/deciso in questa conversazione. Da unire a parte ai dump precedenti (`Fumanji-dump-24-09-26.md`, `Fumanji-dump-28-09-26_0429.md`, `Fumanji-dump-29-09-26.md`), che restano validi per tutto ciò che qui non compare.
> Dump snello: cose cancellate/superate/obsolete non compaiono. Archivio esteso, non riassunto: il codice riportato è quello **finale** di questa sessione.

---

## 1. Cosa è stato fatto in questa sessione (indice)

- Cartelle del repo riorganizzate: `BE/spring` e `FE/angular`.
- Frontend Angular avviato: progetto creato, versione verificata (Angular 21.2.24), proxy verso il backend.
- Quattro schermate funzionanti: benvenuto, inserimento nomi, gioco (stato + azioni del turno + messaggi), vincitore.
- Backend: messaggi delle caselle (obblighi e checkpoint) esposti nell'API invece di essere stampati su console.
- Partita completa giocata dal frontend, tutto come previsto.

---

## 2. Modalità di lavoro e ambiente — novità

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
│   └── spring/                     (era "BE spring"; build e test verificati dal nuovo percorso)
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

- I file generati dalla CLI 21 non hanno il suffisso `.component` (es. `welcome.ts`, classe `Welcome`; anche `app.ts` con classe `App`).
- Convenzione per i modelli: un'interfaccia per file, in `models/`, che rispecchia i DTO del backend (`dto`). Per il service: cartella `services/`, classe `GameApi`.
- `tsconfig.app.json` modificato dall'utente perché VS Code segnalava errori: contiene `"rootDir": "./src"` insieme a `outDir`, `types: []`, `include: src/**/*.ts`, `exclude: src/**/*.spec.ts`.

---

## 4. Decisioni di design — frontend

### 4.1 Routing e flusso
- Navigazione con il **router Angular**, per poter ricaricare la pagina senza perdere la partita (il `GET /api/games/{gameId}` serve a questo).
- Rotte: `''` (benvenuto), `players` (nomi), `game/:gameId` (partita), `winner` (vincitore).
- Flusso: benvenuto → nomi → (all'invio) creazione partita sul backend → `/game/:gameId` → alla vittoria `/winner`.

### 4.2 Collegamento al backend: proxy
- Scelto il **proxy del dev server Angular** (`proxy.conf.json`, prassi abituale dell'utente) invece del CORS in Spring: frontend su `localhost:4200`, backend su `8080`, il frontend chiama percorsi relativi `/api/...`.
- Il proxy vale solo per `ng serve`: come verrà servito il frontend in produzione non è ancora deciso.

### 4.3 Schermata di benvenuto
- Click ovunque nella pagina per proseguire (contenitore `position: fixed; inset: 0` con `(click)`). Estetica non prioritaria: per ora conta solo che funzioni.

### 4.4 Schermata dei nomi (`player-setup`)
- Stato con signal + `(input)`, senza moduli form.
- All'apertura **2 box**; **minimo 2** e **massimo 10** giocatori (limiti solo nel frontend: il backend accetta qualsiasi lista non vuota); pulsante per aggiungere box e pulsante "Rimuovi" per box.
- L'ordine dei box è l'ordine di gioco.
- "Inizia" disabilitato finché un nome è vuoto/blank (il backend risponderebbe `400`) e mentre una richiesta è in corso (evita partite doppie con click ripetuti). I nomi vengono `trim()`-ati prima dell'invio.
- **Nomi doppi non bloccati**: l'uso previsto è dell'utente e di pochi amici fidati, si conta sull'intelligenza di chi usa l'app purché non si rompa. L'utente proverà a romperla in ogni modo più avanti, per sistemarla e migliorarla insieme.
- In caso di errore alla creazione, messaggio provvisorio sotto i pulsanti.

### 4.5 Schermata di gioco (`game-screen`)
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

### 4.6 Schermata del vincitore (`winner`)
- Mostra "Ha vinto X!" e un link "Nuova partita" verso `/players`.
- Il nome arriva con lo **stato della navigazione del router** (`state: { winnerName }`) e si legge da `history.state`. Motivo: alla vittoria il backend toglie la partita dal registro, quindi il vincitore non è più rileggibile con il `GET`. Aperta direttamente senza nome, la schermata mostra "Nessun vincitore da mostrare."
- Il comportamento al refresh della pagina `/winner` non è stato verificato esplicitamente.
- Per ora basta questo: altro (per esempio le posizioni finali) si aggiungerà dopo.

---

## 5. Decisioni di design — backend: messaggi delle caselle

- **Problema**: `ObligationSquare` (e `CheckpointSquare`) stampavano il messaggio solo su console, quindi il frontend non lo riceveva.
- **Piano approvato**: `Game` tiene la lista dei messaggi del turno; `ObligationSquare` e `CheckpointSquare` li aggiungono lì invece di stampare su console; l'API li espone in `GameStateResponse.messages` (campo omesso quando non ci sono messaggi, coerente con `default-property-inclusion=non_null`).
- **Lista e non stringa singola**: il motore permette catene di caselle (es. `RollAgainSquare`), quindi un'azione può in teoria produrre più messaggi.
- **Durata**: i messaggi restano visibili **fino al tiro successivo**. La lista viene svuotata all'inizio di `performRoll()` (dopo il controllo di fase, così un `roll` fuori fase non cancella nulla). `roll()`, `moveByChosenDirection` e `chooseDestination` non sono stati toccati.
- Il messaggio del checkpoint segue lo stesso meccanismo; il bivio con effetto (`BivioSquare.effect`) funziona senza modifiche perché delega a `applyEffect`.
- `addMessage` e `getTurnMessages` sono `synchronized`: il `GET` legge la lista mentre un `roll`/`move` può scriverla, e i metodi di turno usano già lo stesso lock.
- Il messaggio si costruisce lato server come stringa (`buildMessage` invariato, placeholder `{player}` sostituito col nome).

---

## 6. Codice frontend — versioni finali

### 6.1 `proxy.conf.json` (nuovo, in `FE/angular`, accanto ad `angular.json`)
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

### 6.2 `angular.json` — aggiunta dentro il target `serve` (accanto a `builder`)
```json
"options": {
  "proxyConfig": "proxy.conf.json"
},
```

### 6.3 `app.config.ts`
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

### 6.4 `app.routes.ts`
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

### 6.5 `app.html`
```html
<router-outlet />
```
*(`app.ts` invariato; il signal `title` non è più usato e la sua rimozione, insieme all'import di `signal`, è facoltativa.)*

### 6.6 `app.spec.ts`
Rimosso il test `should render title` (cercava un `h1` che in `App` non esiste più). Resta solo `should create the app`.

### 6.7 Models (`src/app/models/`)

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

### 6.8 `services/game-api.ts`
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

### 6.9 `welcome/`

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

### 6.10 `player-setup/`

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

### 6.11 `game-screen/`

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

### 6.12 `winner/`

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

## 7. Codice backend — modifiche di questa sessione

### 7.1 `entity/Game.java` — solo le aggiunte/modifiche

Campo, insieme agli altri:
```java
private final List<String> turnMessages = new ArrayList<>();
```

Metodi nuovi, subito prima di `getBoard()`:
```java
public synchronized void addMessage(String message) {
    turnMessages.add(message);
}

public synchronized List<String> getTurnMessages() {
    return List.copyOf(turnMessages);
}
```

`performRoll()` (una riga aggiunta dopo il controllo di fase):
```java
public synchronized int performRoll() {
    checkTurnAction(TurnPhase.WAITING_ROLL);
    turnMessages.clear();
    int rollValue = roll(getCurrentPlayer());
    turnPhase = TurnPhase.WAITING_DIRECTION;
    return rollValue;
}
```

### 7.2 `entity/squares/ObligationSquare.java` — `applyEffect`
```java
@Override
public void applyEffect(Player player, Game game) {
    game.addMessage(buildMessage(player, game));
}
```

### 7.3 `entity/squares/CheckpointSquare.java` — `applyEffect`
```java
@Override
public void applyEffect(Player player, Game game) {
    game.updateCheckpoint(player);
    game.addMessage(buildMessage(player));
}
```

### 7.4 `dto/GameStateResponse.java`
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

### 7.5 `mapper/GameStateAssembler.java` — `toResponse`
```java
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
```

### 7.6 `GameControllerIT` — due test nuovi
```java
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
```
Usano la casella 1 della board OG (obbligo generico, messaggio `{player} deve dare un obbligo a qualcuno`): con il dado a 1 la prima mossa dalla partenza atterra lì.

---

## 8. Stato delle verifiche

- **Frontend**: l'utente ha verificato in locale benvenuto, inserimento nomi, creazione partita (proxy funzionante, `POST /api/games` restituisce il `gameId`), schermata di gioco (stato, tiro, direzione, scelta, messaggi), una partita completa fino alla fine e la schermata del vincitore ("funziona").
- **Backend**: l'utente ha riportato che le modifiche per i messaggi funzionano; i due test nuovi sono in `GameControllerIT`.
- **Non verificato**: `ng test` non è stato lanciato; gli spec generati dalla CLI per i nuovi componenti (`player-setup`, `game-screen`, `winner`) e per `GameApi` non sono stati eseguiti né adattati (`GameScreen`/`PlayerSetup` usano `Router`, `ActivatedRoute` e `GameApi`, quindi potrebbero richiedere provider di test). Il comportamento al refresh di `/winner` non è stato controllato esplicitamente.

---

## 9. TODO / prossimi passi (voci nuove o cambiate in questa sessione)

1. **Tabellone** (prossimo pezzo grosso del frontend): servono un endpoint nuovo con caselle e collegamenti (oggi nessun endpoint li restituisce) e la decisione su come disegnare 110 caselle con rami, bivi e portali.
2. **Schermata del vincitore**: aggiungere altro (per esempio posizioni finali di tutti i giocatori).
3. **Spec generati dalla CLI** (`ng test`, Vitest): eseguirli e adattarli o rimuoverli.
4. **Provvisori da rifinire**: fase mostrata come valore grezzo, testi dell'interfaccia, messaggi d'errore, estetica in generale.
5. **Nomi doppi**: non bloccati per scelta; da riconsiderare quando l'utente proverà a "rompere" l'app.
6. **Proxy solo per sviluppo**: da decidere come verrà servito il frontend in produzione.

Invariati e ancora aperti dai dump precedenti: TTL per le partite abbandonate in `GameRegistry`, storico/statistiche a fine partita, rivalutare EAGER e `OG_BOARD_ID` fisso con board multiple, test Postman di `roll`/`move`/`choice`/`GET` a carico dell'utente.
