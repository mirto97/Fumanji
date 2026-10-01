// Etichette mostrate nelle caselle, per posizione (board OG): dato visivo statico,
// da spostare nel backend insieme alle coordinate.
// Ogni elemento dell'array è una riga dell'etichetta
export const SQUARE_LABELS: Readonly<Partial<Record<number, readonly string[]>>> = {
  0: ['partenza'],
  1: ['obbligo'],
  2: ['turca'],
  3: ['h2o'],
  // StartOver: per ora "0" al posto del buco nero
  4: ['0'],
  5: ['no smoke'],
  // Bivio A: prende l'etichetta del suo effetto (snack)
  6: ['schimica'],
};