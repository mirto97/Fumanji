import { SquareType } from './square-type';

export interface SquareResponse {
  position: number;
  type: SquareType;
  // Assenti dalla risposta quando non ci sono (il backend omette i valori null)
  description?: string;
  nextPositions: number[];
  previousPositions: number[];
  linkedPositions?: number[];
}