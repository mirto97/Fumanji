import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-welcome',
  imports: [],
  templateUrl: './welcome.html',
  styleUrl: './welcome.css',
})
export class Welcome {
    private readonly router = inject(Router);

    goToPlayers(): void {
        this.router.navigate(['/players']);
    }
}
