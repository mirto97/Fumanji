
Fumanji-dump-28-09-26_0429.md
File
dimmi al volo come testare tutti gli endpoint su postman

Certo. Nel tuo backend hai 5 endpoint REST da provare con Postman, e il flusso corretto è questo.

1. Creazione partita

POST

http://localhost:8080/api/games

Body → raw → JSON:

{
  "playerNames": ["Mario", "Luigi"]
}

Devi ottenere 200 OK, con qualcosa tipo:

{
  "gameId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "currentPlayerName": "Mario"
}

Copia il gameId.

2. Leggere lo stato

GET

http://localhost:8080/api/games/{gameId}

Esempio:

http://localhost:8080/api/games/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

Devi vedere almeno:

{
  "currentPlayerName": "Mario",
  "turnPhase": "WAITING_ROLL",
  "lastRoll": 0,
  "players": [...]
}

3. Tirare il dado

POST

http://localhost:8080/api/games/{gameId}/roll

Nessun body.

Risultato atteso: 200 OK e:

{
  "currentPlayerName": "Mario",
  "turnPhase": "WAITING_DIRECTION",
  "lastRoll": 4,
  "players": [...]
}

Il valore del dado sarà casuale 1–6.

4. Muovere

POST

http://localhost:8080/api/games/{gameId}/move

Header:

Content-Type: application/json

Body:

{
  "forward": true
}

Dopo la mossa normalmente il turno passa a Luigi e la fase torna:

"turnPhase": "WAITING_ROLL"

5. Choice — solo quando capita un bivio/portale

POST

http://localhost:8080/api/games/{gameId}/choice

Body:

{
  "position": 40
}

Ma questo endpoint funziona solo se la risposta precedente ha turnPhase: "WAITING_CHOICE" e ti mostra choiceOptions.

Quindi:

roll
  ↓
move
  ↓
se WAITING_CHOICE
  ↓
choice
I test negativi da fare subito

Sono molto utili per verificare davvero l'API:

move senza roll       → 409
roll due volte        → 409
choice senza scelta   → 409
gameId inesistente    → 404
move con {}           → 400

Questi comportamenti sono già previsti e sono anche coperti dai test JUnit.

Nota importante: il test Postman già fatto è POST /api/games; quelli di roll, move, choice e GET sono ancora da eseguire manualmente.

Sequenza pratica in Postman
POST /api/games
        ↓
GET /api/games/{gameId}
        ↓
POST /api/games/{gameId}/roll
        ↓
POST /api/games/{gameId}/move
        ↓
GET /api/games/{gameId}
        ↓
ripeti roll → move
        ↓
quando compare WAITING_CHOICE
        ↓
POST /api/games/{gameId}/choice

Per il tuo caso, non serve complicarsi con autenticazione o collection particolari: basta partire con queste 5 request.

DGlen Srl

AI progettata per la produzione

Sistemi AI affidabili con integrazione, validazione, observability e controllo dei costi.

Annuncio
Altre opzioni
ok, puoi trascrivermi tutta questa conversazione in un file .md? perchè è andato tutto come hai descritto, chiamalo RisultatiTestPostman data e ora.