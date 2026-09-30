import { Routes } from '@angular/router';
import { Welcome } from './components/welcome/welcome';
import { PlayerSetup } from './components/player-setup/player-setup';
import { GameScreen } from './components/game-screen/game-screen';
import { Winner } from './components/winner/winner';

export const routes: Routes = [
    { path: '', component: Welcome },
    { path: 'players', component: PlayerSetup },
    { path: 'game/:gameId', component: GameScreen },
    { path: 'winner', component: Winner },
];
