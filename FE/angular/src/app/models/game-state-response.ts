import { MovementResponse } from './movement-response';
import { PlayerStateResponse } from './player-state-response';
import { TurnPhase } from './turn-phase';

export interface GameStateResponse {
  currentPlayerName: string;
  turnPhase: TurnPhase;
  lastRoll: number;
  players: PlayerStateResponse[];
  // Assenti dalla risposta quando non ci sono (il backend omette i valori null)
  choiceOptions?: number[];
  // Passi ancora da fare partendo dal bivio (compreso quello della scelta); assente per i portali
  choiceRemainingSteps?: number;
  winnerName?: string;
  // Messaggi generati dalle caselle durante il turno; assenti dalla risposta quando non ce ne sono
  messages?: string[];
  // Indice del giocatore che si è mosso e posizioni attraversate durante l'azione; assenti se non c'è stato movimento
  movedPlayerIndex?: number;
  movements?: MovementResponse[];
}