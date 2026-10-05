import { Routes } from '@angular/router';
import { Welcome } from './Components/welcome/welcome';
import { PlayerSetup } from './Components/player-setup/player-setup';
import { GameScreen } from './Components/game-screen/game-screen';
import { Winner } from './Components/winner/winner';

export const routes: Routes = [
    { path: '', component: Welcome },
    { path: 'players', component: PlayerSetup },
    { path: 'game/:gameId', component: GameScreen },
    { path: 'winner', component: Winner },
];
