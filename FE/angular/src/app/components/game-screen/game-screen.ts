import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { GameStateResponse } from '../../models/game-state-response';
import { GameApi } from '../../services/game-api';

@Component({
  selector: 'app-game-screen',
  imports: [],
  templateUrl: './game-screen.html',
  styleUrl: './game-screen.css',
})
export class GameScreen implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly gameApi = inject(GameApi);

  // Identificativo della partita, preso dall'URL (/game/:gameId); la rotta lo garantisce sempre presente
  private readonly gameId = this.route.snapshot.paramMap.get('gameId')!;

  // Ultimo stato ricevuto dal backend; resta null finché non arriva la prima risposta
  readonly state = signal<GameStateResponse | null>(null);

  // Messaggio mostrato se lo stato non si riesce a caricare
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    // Lo stato si legge dal backend a ogni apertura della pagina, anche dopo un refresh
    this.gameApi.getGame(this.gameId).subscribe({
      next: state => this.state.set(state),
      error: (error: HttpErrorResponse) => this.errorMessage.set(
        error.status === 404 ? 'Partita non trovata.' : 'Impossibile caricare la partita.'),
    });
  }
}