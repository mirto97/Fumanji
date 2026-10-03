import { MovementType } from './movement-type';

export interface MovementResponse {
  position: number;
  type: MovementType;
  // Assente dalla risposta quando non c'è (il backend omette i valori null)
  message?: string;
}