import { Component, computed, input } from '@angular/core';

// Pedina di un giocatore: si usa come attributo su un <g> dentro l'SVG del tabellone.
// Il centro è l'origine del <g>, spostato con transform: la transizione CSS la fa scivolare
@Component({
  selector: '[appPawn]',
  imports: [],
  templateUrl: './pawn.html',
  styleUrl: './pawn.css',
  host: {
    '[style.transform]': 'transform()',
  },
})
export class Pawn {

  readonly x = input.required<number>();
  readonly y = input.required<number>();
  readonly radius = input.required<number>();
  readonly color = input.required<string>();
  readonly initial = input.required<string>();

  // Posizione del centro della pedina in unità SVG
  readonly transform = computed(() => `translate(${this.x()}px, ${this.y()}px)`);
}