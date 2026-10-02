package com.project.fumanji.model;

/**
 * Marcatore per le caselle che richiedono una scelta del giocatore per proseguire
 * (es. Bivio, Portale con piu' uscite). Nessun metodo: i due meccanismi di innesco
 * restano diversi (a meta' step per il Bivio, solo su atterraggio esatto per il Portale),
 * la risoluzione della scelta e' invece unificata in Game (isAwaitingChoice/getChoiceOptions/chooseDestination).
 */
public interface InteractiveSquare {
}