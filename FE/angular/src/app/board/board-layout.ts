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
export const BOARD_ROWS = 12;

const KEY_POINTS: Record<number, Point> = {
  // Partenza e bivio A
  0: { x: 0, y: 11 },
  7: { x: 7, y: 11 },
  // Ramo in su dal bivio A (12 = bivio B)
  10: { x: 7, y: 10 },
  12: { x: 7, y: 8 },
  // Ramo a sinistra dal bivio B, fino al crea (33)
  22: { x: 6, y: 8 },
  24: { x: 4, y: 8 },
  25: { x: 4, y: 7 },
  27: { x: 4, y: 5 },
  28: { x: 5, y: 5 },
  33: { x: 10, y: 5 },
  // Ramo a destra dal bivio B, bivio D (70) e coda fino al crea (73)
  16: { x: 8, y: 8 },
  17: { x: 9, y: 8 },
  70: { x: 10, y: 8 },
  72: { x: 12, y: 8 },
  73: { x: 12, y: 7 },
  // Ramo a destra dal bivio A (42 = bivio C), ramo in su (46-47) e ramo a destra fino al crea (63)
  40: { x: 8, y: 11 },
  42: { x: 10, y: 11 },
  46: { x: 10, y: 10 },
  47: { x: 10, y: 9 },
  52: { x: 11, y: 11 },
  56: { x: 15, y: 11 },
  57: { x: 15, y: 10 },
  62: { x: 15, y: 5 },
  63: { x: 14, y: 5 },
  // Checkpoint: angolo in alto a sinistra del blocco 3x3 occupato dal cerchio
  81: { x: 11, y: 4 },
  // Dal checkpoint all'arrivo: su, a sinistra, giù, a sinistra, su, a sinistra, giù
  82: { x: 12, y: 3 },
  84: { x: 12, y: 1 },
  85: { x: 12, y: 0 },
  89: { x: 8, y: 0 },
  90: { x: 8, y: 1 },
  92: { x: 8, y: 3 },
  93: { x: 7, y: 3 },
  95: { x: 5, y: 3 },
  96: { x: 5, y: 2 },
  98: { x: 5, y: 0 },
  99: { x: 4, y: 0 },
  102: { x: 1, y: 0 },
  103: { x: 1, y: 1 },
  110: { x: 1, y: 8 },
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
  { from: 82, to: 84 },
  { from: 85, to: 89 },
  { from: 90, to: 92 },
  { from: 93, to: 95 },
  { from: 96, to: 98 },
  { from: 99, to: 102 },
  { from: 103, to: 110 },
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