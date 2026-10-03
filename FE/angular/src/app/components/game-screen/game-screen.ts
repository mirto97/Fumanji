import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { GameStateResponse } from '../../models/game-state-response';
import { GameApi } from '../../services/game-api';
import { Observable } from 'rxjs';
import { Board } from '../../board/board';
import { PlayerStateResponse } from '../../models/player-state-response';

// Pausa tra una posizione e la successiva durante il movimento: poco più della transizione CSS della pedina (0,4 s)
const STEP_DELAY_MS = 450;

// Attesa asincrona di `ms` millisecondi
const sleep = (ms: number) => new Promise<void>(resolve => setTimeout(resolve, ms));

// Pedina in movimento durante la riproduzione: indice del giocatore e posizione mostrata
interface AnimatedPawn {
  playerIndex: number;
  position: number;
}

@Component({
  selector: 'app-game-screen',
  imports: [Board],
  templateUrl: './game-screen.html',
  styleUrl: './game-screen.css',
})
export class GameScreen implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly gameApi = inject(GameApi);
  private readonly router = inject(Router);

  // Pedina in movimento durante la riproduzione dell'azione; null quando non c'è nulla da animare
  readonly animatedPawn = signal<AnimatedPawn | null>(null);

  // Messaggio dello start over mostrato durante la pausa, in attesa del "Continua"
  readonly pauseMessage = signal<string | null>(null);

  // Riprende la riproduzione dopo la pausa; è null quando non c'è nessuna pausa
  private resumeReplay: (() => void) | null = null;

  // Giocatori come li vede il tabellone: la pedina in movimento mostra la posizione intermedia al posto di quella finale
  readonly boardPlayers = computed<PlayerStateResponse[]>(() => {
    const players = this.state()?.players ?? [];
    const animated = this.animatedPawn();
    if (!animated) {
      return players;
    }
    return players.map((player, index) =>
      index === animated.playerIndex ? { ...player, position: animated.position } : player);
  });

  // Identificativo della partita, preso dall'URL (/game/:gameId); la rotta lo garantisce sempre presente
  private readonly gameId = this.route.snapshot.paramMap.get('gameId')!;

  // Ultimo stato ricevuto dal backend; resta null finché non arriva la prima risposta
  readonly state = signal<GameStateResponse | null>(null);

  // Messaggio mostrato se lo stato non si riesce a caricare
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
      // La risposta si applica a movimento concluso: isBusy resta attivo fino ad allora
      next: state => {
        void this.play(state).finally(() => this.isBusy.set(false));
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(this.toErrorMessage(error));
        this.isBusy.set(false);
      },
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

  // Riproduce il movimento dell'azione cella per cella e poi applica lo stato finale della risposta
  private async play(state: GameStateResponse): Promise<void> {
    const playerIndex = state.movedPlayerIndex;
    if (playerIndex !== undefined) {
      for (const movement of state.movements ?? []) {
        // Il ritorno dello start over parte solo dopo il "Continua" del giocatore
        if (movement.type === 'RESET' && movement.message) {
          await this.waitForContinue(movement.message);
        }
        this.animatedPawn.set({ playerIndex, position: movement.position });
        await sleep(STEP_DELAY_MS);
      }
      this.animatedPawn.set(null);
    }

    this.state.set(state);
    // Alla vittoria si passa alla schermata del vincitore, a movimento concluso: la partita non è più rileggibile dal backend
    if (state.winnerName) {
      this.router.navigate(['/winner'], { state: { winnerName: state.winnerName } });
    }
  }

  // Mostra il messaggio e resta in attesa del "Continua"
  private waitForContinue(message: string): Promise<void> {
    return new Promise<void>(resolve => {
      this.pauseMessage.set(message);
      this.resumeReplay = () => {
        this.pauseMessage.set(null);
        this.resumeReplay = null;
        resolve();
      };
    });
  }

  continueReplay(): void {
    this.resumeReplay?.();
  }
}