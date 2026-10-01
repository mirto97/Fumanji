import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { BoardResponse } from '../models/board-response';

@Injectable({
  providedIn: 'root',
})
export class BoardApi {

  private readonly http = inject(HttpClient);

  // Percorso relativo: in sviluppo lo inoltra il proxy del dev server al backend
  private readonly baseUrl = '/api/boards';

  getBoard(boardId: number): Observable<BoardResponse> {
    return this.http.get<BoardResponse>(`${this.baseUrl}/${boardId}`);
  }
}