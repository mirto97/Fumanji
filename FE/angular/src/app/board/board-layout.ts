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
export const BOARD_COLUMNS = 9;
export const BOARD_ROWS = 8;

// Punti chiave posizionati a mano (in celle): partenza e bivio A
const KEY_POINTS: Record<number, Point> = {
  0: { x: 1, y: 6 },
  6: { x: 7, y: 6 },
};

// Tratti da interpolare: le caselle intermedie si distribuiscono in modo uniforme tra gli estremi
const SEGMENTS: Segment[] = [
  { from: 0, to: 6 },
];

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