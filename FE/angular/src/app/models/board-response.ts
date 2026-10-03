import { SquareResponse } from './square-response';

export interface BoardResponse {
  finishPosition: number;
  squares: SquareResponse[];
}