import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { GameApi } from '../../services/game-api';

const MIN_PLAYERS = 2;
const MAX_PLAYERS = 10;

@Component({
  selector: 'app-player-setup',
  imports: [],
  templateUrl: './player-setup.html',
  styleUrl: './player-setup.css',
})
export class PlayerSetup {

  // Nomi dei giocatori nell'ordine di gioco; si parte con il numero minimo di caselle vuote
  readonly names = signal<string[]>(Array<string>(MIN_PLAYERS).fill(''));
  private readonly gameApi = inject(GameApi);
  private readonly router = inject(Router);

  // True mentre la richiesta di creazione è in corso: evita partite doppie con click ripetuti
  readonly isSubmitting = signal(false);

  // Messaggio mostrato se la creazione della partita fallisce
  readonly errorMessage = signal<string | null>(null);

  // Si può aggiungere un giocatore finché non si raggiunge il massimo
  readonly canAdd = computed(() => this.names().length < MAX_PLAYERS);

  // Si può togliere un giocatore finché non si scende sotto il minimo
  readonly canRemove = computed(() => this.names().length > MIN_PLAYERS);

    // L'invio è abilitato solo se nessun nome è vuoto (il backend rifiuta i nomi blank) e nessuna richiesta è in corso
  readonly canSubmit = computed(() => !this.isSubmitting() && this.names().every(name => name.trim().length > 0));

  addPlayer(): void {
    if (!this.canAdd()) {
      return;
    }
    // L'array non si modifica sul posto: update() ne restituisce uno nuovo, così il signal si accorge del cambio
    this.names.update(names => [...names, '']);
  }

  removePlayer(index: number): void {
    if (!this.canRemove()) {
      return;
    }
    // Si tengono tutti i nomi tranne quello alla posizione indicata
    this.names.update(names => names.filter((_, i) => i !== index));
  }

  updateName(index: number, value: string): void {
    // Sostituisce solo il nome alla posizione indicata, gli altri restano invariati
    this.names.update(names => names.map((name, i) => (i === index ? value : name)));
  }

	submit(): void {
		if (!this.canSubmit()) {
			return;
		}
		this.isSubmitting.set(true);
		this.errorMessage.set(null);

		// La richiesta parte solo con subscribe(): se va a buon fine si passa alla schermata di gioco, altrimenti si mostra l'errore
		this.gameApi.createGame(this.names().map(name => name.trim())).subscribe({
			next: response => this.router.navigate(['/game', response.gameId]),
			error: () => {
				this.errorMessage.set('Impossibile creare la partita. Riprova.');
				this.isSubmitting.set(false);
			},
			complete: () => this.isSubmitting.set(false),
		});
	}
}