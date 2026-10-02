// Coordinate in celle della griglia: x = colonna, y = riga (la riga 0 è in alto)
export interface Point {
  x: number;
  y: number;
}

// Tratto di caselle consecutive (da `from` a `to`, estremi inclusi) disposte in linea retta tra i due punti chiave
interface Segment {
  from: number;
  to: number;
}

// Lato di una casella in unità SVG e dimensioni della griglia in celle
export const CELL_SIZE = 50;
export const BOARD_COLUMNS = 16;
export const BOARD_ROWS = 11;

// Punti chiave posizionati a mano (in celle)
const KEY_POINTS: Record<number, Point> = {
  // Partenza e bivio A
  0: { x: 0, y: 10 },
  7: { x: 7, y: 10 },
  // Ramo in su dal bivio A (12 = bivio B)
  10: { x: 7, y: 9 },
  12: { x: 7, y: 7 },
  // Ramo a sinistra dal bivio B, fino al crea (33)
  22: { x: 6, y: 7 },
  24: { x: 4, y: 7 },
  25: { x: 4, y: 6 },
  27: { x: 4, y: 4 },
  28: { x: 5, y: 4 },
  33: { x: 10, y: 4 },
  // Ramo a destra dal bivio B, bivio D (70) e coda fino al crea (73)
  16: { x: 8, y: 7 },
  17: { x: 9, y: 7 },
  70: { x: 10, y: 7 },
  72: { x: 12, y: 7 },
  73: { x: 12, y: 6 },
  // Ramo a destra dal bivio A (42 = bivio C), ramo in su (46-47) e ramo a destra fino al crea (63)
  40: { x: 8, y: 10 },
  42: { x: 10, y: 10 },
  46: { x: 10, y: 9 },
  47: { x: 10, y: 8 },
  52: { x: 11, y: 10 },
  56: { x: 15, y: 10 },
  57: { x: 15, y: 9 },
  62: { x: 15, y: 4 },
  63: { x: 14, y: 4 },
  // Checkpoint: angolo in alto a sinistra del blocco 3x3 occupato dal cerchio
  81: { x: 11, y: 3 },
};

// Tratti da interpolare: le caselle intermedie si distribuiscono in modo uniforme tra gli estremi
const SEGMENTS: Segment[] = [
  { from: 0, to: 7 },
  { from: 10, to: 12 },
  { from: 22, to: 24 },
  { from: 25, to: 27 },
  { from: 28, to: 33 },
  { from: 40, to: 42 },
  { from: 52, to: 56 },
  { from: 57, to: 62 },
  { from: 70, to: 72 },
];

// Forma di una casella
export type SquareShape = 'square' | 'circle';

// Ingombro di una casella: lato in celle e forma
export interface SquareFootprint {
  span: number;
  shape: SquareShape;
}

// Le caselle non indicate in SQUARE_FOOTPRINTS sono quadrate di una cella
export const DEFAULT_FOOTPRINT: SquareFootprint = { span: 1, shape: 'square' };

export const SQUARE_FOOTPRINTS: Readonly<Partial<Record<number, SquareFootprint>>> = {
  // Checkpoint: cerchio grande
  81: { span: 3, shape: 'circle' },
};

// Costruisce la mappa posizione -> coordinate: punti chiave più caselle interpolate dei tratti
function buildLayout(): ReadonlyMap<number, Point> {
  const layout = new Map<number, Point>();

  for (const [position, point] of Object.entries(KEY_POINTS)) {
    layout.set(Number(position), point);
  }

  for (const { from, to } of SEGMENTS) {
    const start = KEY_POINTS[from];
    const end = KEY_POINTS[to];
    for (let position = from + 1; position < to; position++) {
      const ratio = (position - from) / (to - from);
      layout.set(position, {
        x: start.x + (end.x - start.x) * ratio,
        y: start.y + (end.y - start.y) * ratio,
      });
    }
  }

  return layout;
}

export const BOARD_LAYOUT = buildLayout();