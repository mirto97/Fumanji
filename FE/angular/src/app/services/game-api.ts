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