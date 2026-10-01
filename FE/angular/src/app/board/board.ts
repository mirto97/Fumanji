import { Component, OnInit, computed, inject, input, signal } from '@angular/core';

import { BoardResponse } from '../models/board-response';
import { SquareResponse } from '../models/square-response';
import { BoardApi } from '../services/board-api';
import { BOARD_COLUMNS, BOARD_LAYOUT, BOARD_ROWS, CELL_SIZE, Point } from './board-layout';
import { SQUARE_LABELS } from './board-labels';
import { PlayerStateResponse } from '../models/player-state-response';
import { Pawn } from '../pawn/pawn';

// TODO commentare e studiare tipo peffoooooooooooooo

// Id della board OG: per ora una sola board fissa, stesso id usato dal backend
const OG_BOARD_ID = 1;

// Distanza verticale tra le righe di un'etichetta, in unità SVG
const LABEL_LINE_HEIGHT = 12;

// Raggio massimo di una pedina; con più pedine sulla stessa casella si restringe per farle stare affiancate
const PAWN_RADIUS = 8;

// Colori delle pedine, assegnati in base all'ordine di gioco (10 = massimo dei giocatori)
const PLAYER_COLORS = [
  '#d32f2f', '#1976d2', '#388e3c', '#f57c00', '#7b1fa2',
  '#00838f', '#5d4037', '#c2185b', '#455a64', '#827717',
];

// Riga di un'etichetta con la sua ordinata SVG (centro della riga)
interface LabelLine {
  text: string;
  y: number;
}

// Casella insieme alle coordinate SVG del suo angolo in alto a sinistra e alle righe dell'etichetta
interface PlacedSquare {
  square: SquareResponse;
  x: number;
  y: number;
  lines: LabelLine[];
}

// Pedina con le coordinate SVG del suo centro
interface PlacedPawn {
  index: number;
  x: number;
  y: number;
  radius: number;
  color: string;
  initial: string;
}

@Component({
  selector: 'app-board',
  imports: [Pawn],
  templateUrl: './board.html',
  styleUrl: './board.css',
})
export class Board implements OnInit {

  private readonly boardApi = inject(BoardApi);

  readonly cellSize = CELL_SIZE;
  readonly viewBox = `0 0 ${BOARD_COLUMNS * CELL_SIZE} ${BOARD_ROWS * CELL_SIZE}`;

  // Tabellone ricevuto dal backend; resta null finché non arriva la risposta
  readonly board = signal<BoardResponse | null>(null);

	// Stato dei giocatori, da cui si ricavano le pedine; lo fornisce la schermata di gioco
	readonly players = input<PlayerStateResponse[]>([]);

  // Messaggio mostrato se il tabellone non si riesce a caricare
  readonly errorMessage = signal<string | null>(null);

  // Posizione della casella cliccata, di cui si mostra il dettaglio
  readonly selectedPosition = signal<number | null>(null);

	readonly placedSquares = computed<PlacedSquare[]>(() =>
  	(this.board()?.squares ?? []).flatMap(square => {
			const cell = BOARD_LAYOUT.get(square.position);
			if (!cell) {
				return [];
			}
			// Da cella della griglia a coordinate SVG
			const x = cell.x * CELL_SIZE;
			const y = cell.y * CELL_SIZE;
			// Senza etichetta si mostra il numero di posizione; ogni elemento è una riga
			const rows = SQUARE_LABELS[square.position] ?? [String(square.position)];			
			return [{
				square,
				x,
				y,
				// Righe centrate verticalmente nella casella
				lines: rows.map((text, index) => ({
					text,
					y: y + CELL_SIZE / 2 + (index - (rows.length - 1) / 2) * LABEL_LINE_HEIGHT,
				})),
			}];
  	}
	));

	readonly pawns = computed<PlacedPawn[]>(() => {
		const players = this.players();

		// Quante pedine ci sono su ogni posizione, per dividere la casella fra loro
		const countByPosition = new Map<number, number>();
		for (const player of players) {
			countByPosition.set(player.position, (countByPosition.get(player.position) ?? 0) + 1);
		}

		// Quante pedine di ogni posizione sono già state sistemate
		const placedByPosition = new Map<number, number>();

		return players.flatMap((player, index) => {
			const cell = BOARD_LAYOUT.get(player.position);
			// Posizione senza coordinate: la pedina non si disegna
			if (!cell) {
				return [];
			}
			const slot = placedByPosition.get(player.position) ?? 0;
			placedByPosition.set(player.position, slot + 1);

			// Pedine affiancate nella parte bassa della casella, ognuna nella sua fascia
			const slotWidth = CELL_SIZE / countByPosition.get(player.position)!;
			return [{
				index,
				x: cell.x * CELL_SIZE + slotWidth * (slot + 0.5),
				y: (cell.y + 1) * CELL_SIZE - PAWN_RADIUS - 2,
				radius: Math.min(PAWN_RADIUS, slotWidth / 2),
				color: PLAYER_COLORS[index % PLAYER_COLORS.length],
				initial: player.name.charAt(0).toUpperCase(),
			}];
		});
	});

  // Casella selezionata, ricavata dalla posizione cliccata
  readonly selectedSquare = computed(() =>
    this.board()?.squares.find(square => square.position === this.selectedPosition()) ?? null);

  ngOnInit(): void {
    this.boardApi.getBoard(OG_BOARD_ID).subscribe({
      next: board => this.board.set(board),
      error: () => this.errorMessage.set('Impossibile caricare il tabellone.'),
    });
  }

  select(position: number): void {
    this.selectedPosition.set(position);
  }
}