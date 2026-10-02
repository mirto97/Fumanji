// Etichette mostrate nelle caselle, per posizione (board OG): dato visivo statico,
// da spostare nel backend insieme alle coordinate.
// Ogni elemento dell'array è una riga dell'etichetta
export const SQUARE_LABELS: Readonly<Partial<Record<number, readonly string[]>>> = {
  0: ['partenza'],
  1: ['2 tiri'],
  2: ['obbligo'],
  3: ['turca'],
  4: ['h2o'],
  // StartOver: per ora "0" al posto del buco nero
  5: ['0'],
  6: ['no smoke'],
  // Bivio A: prende l'etichetta del suo effetto (snack)
  7: ['schimica'],
  // Ramo in su: 12 è il Bivio B, prende l'etichetta del suo effetto (obbligo)
  10: ['2 tiri'],
  11: ['turca'],
  12: ['obbligo'],
  // Ramo a destra: 42 è il Bivio C, prende l'etichetta del suo effetto (turca)
  40: ['2 tiri'],
  41: ['obbligo'],
  42: ['turca'],
  // Ramo a destra dal bivio B (70 = bivio D, prende l'etichetta del suo effetto)
  16: ['0'],
  17: ['no smoke'],
  70: ['0'],
  71: ['schimica'],
  72: ['0'],
  73: ['crea'],
  // Ramo a sinistra dal bivio B (23, 27, 32 = portali; 33 = bivio con effetto crea)
  22: ['h2o'],
  23: ['0 2'],
  24: ['no smoke'],
  25: ['turca'],
  26: ['obbligo'],
  27: ['0 5'],
  28: ['schimica'],
  29: ['2 tiri'],
  30: ['obbligo'],
  31: ['h2o'],
  32: ['0 3'],
  33: ['crea'],
  // Ramo in su dal bivio C (47 = bivio con effetto drink)
  46: ['0'],
  47: ['h2o'],
  // Ramo a destra dal bivio C (53, 58, 62 = portali; 63 = bivio con effetto crea)
  52: ['h2o'],
  53: ['0 1'],
  54: ['no smoke'],
  55: ['turca'],
  56: ['schimica'],
  57: ['2 tiri'],
  58: ['0 6'],
  59: ['obbligo'],
  60: ['h2o'],
  61: ['turca'],
  62: ['0 4'],
  63: ['crea'],
};