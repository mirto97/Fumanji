import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-winner',
  imports: [RouterLink],
  templateUrl: './winner.html',
  styleUrl: './winner.css',
})
export class Winner {

  // Nome del vincitore passato con lo stato della navigazione: history.state sopravvive al refresh della pagina
  readonly winnerName: string | undefined = history.state?.['winnerName'];
}