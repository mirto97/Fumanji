import { Component, OnInit, computed, inject, input, signal } from '@angular/core';

import { BoardResponse } from '../models/board-response';
import { SquareResponse } from '../models/square-response';
import { BoardApi } from '../services/board-api';
import { BOARD_COLUMNS, BOARD_LAYOUT, BOARD_ROWS, CELL_SIZE, DEFAULT_FOOTPRINT, SQUARE_FOOTPRINTS, SquareShape, } from './board-layout';
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

// Fascia delle pedine in una casella tonda, in proporzione al lato: più stretta e più in alto, per restare dentro il cerchio
const CIRCLE_PAWN_ROW_WIDTH_RATIO = 0.8;
const CIRCLE_PAWN_ROW_HEIGHT_RATIO = 0.75;

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

// Casella insieme alle coordinate SVG del suo angolo in alto a sinistra, al lato, alla forma e alle righe dell'etichetta
interface PlacedSquare {
  square: SquareResponse;
  x: number;
  y: number;
  size: number;
  shape: SquareShape;
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

  readonly width = BOARD_COLUMNS * CELL_SIZE;
  readonly height = BOARD_ROWS * CELL_SIZE;
  readonly viewBox = `0 0 ${this.width} ${this.height}`;

  // Tabellone ricevuto dal backend; resta null finché non arriva la risposta
  readonly board = signal<BoardResponse | null>(null);

	// Stato dei giocatori, da cui si ricavano le pedine; lo fornisce la schermata di gioco
	readonly players = input<PlayerStateResponse[]>([]);

  // Messaggio mostrato se il tabellone non si riesce a caricare
  readonly errorMessage = signal<string | null>(null);

  // Posizione della casella cliccata, di cui si mostra il dettaglio
  readonly selectedPosition = signal<number | null>(null);

	  // Si disegnano solo le caselle che hanno già una coordinata: il tabellone si completa per pezzi
  readonly placedSquares = computed<PlacedSquare[]>(() =>
    (this.board()?.squares ?? []).flatMap(square => {
      const cell = BOARD_LAYOUT.get(square.position);
      if (!cell) {
        return [];
      }
      const { span, shape } = SQUARE_FOOTPRINTS[square.position] ?? DEFAULT_FOOTPRINT;
      const size = span * CELL_SIZE;
      // Da cella della griglia a coordinate SVG
      const x = cell.x * CELL_SIZE;
      const y = cell.y * CELL_SIZE;
      // Senza etichetta si mostra il numero di posizione; ogni elemento è una riga
      const rows = SQUARE_LABELS[square.position] ?? [String(square.position)];
      return [{
        square,
        x,
        y,
        size,
        shape,
        // Righe centrate verticalmente nella casella
        lines: rows.map((text, index) => ({
          text,
          y: y + size / 2 + (index - (rows.length - 1) / 2) * LABEL_LINE_HEIGHT,
        })),
      }];
    }));

  readonly pawns = computed<PlacedPawn[]>(() => {
    const players = this.players();

    const countByPosition = new Map<number, number>();
    for (const player of players) {
      const position = player.position;
      countByPosition.set(position, (countByPosition.get(position) ?? 0) + 1);
    }

    // Quante pedine di ogni casella sono già state sistemate
    const placedByPosition = new Map<number, number>();

    return players.flatMap((player, index) => {
      const position = player.position;
      const cell = BOARD_LAYOUT.get(position);
      // Posizione senza coordinate: la pedina non si disegna
      if (!cell) {
        return [];
      }
      const { span, shape } = SQUARE_FOOTPRINTS[position] ?? DEFAULT_FOOTPRINT;
      const size = span * CELL_SIZE;
      const originX = cell.x * CELL_SIZE;
      const originY = cell.y * CELL_SIZE;
      const slot = placedByPosition.get(position) ?? 0;
      placedByPosition.set(position, slot + 1);

      // Pedine affiancate in una fascia: in basso nelle caselle quadrate, più stretta e più in alto in quelle tonde
      const isCircle = shape === 'circle';
      const rowWidth = isCircle ? size * CIRCLE_PAWN_ROW_WIDTH_RATIO : size;
      const slotWidth = rowWidth / countByPosition.get(position)!;
      return [{
        index,
        x: originX + (size - rowWidth) / 2 + slotWidth * (slot + 0.5),
        y: isCircle ? originY + size * CIRCLE_PAWN_ROW_HEIGHT_RATIO : originY + size - PAWN_RADIUS - 2,
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