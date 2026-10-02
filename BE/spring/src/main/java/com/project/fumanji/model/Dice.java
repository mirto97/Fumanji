package com.project.fumanji.model;

import java.util.concurrent.ThreadLocalRandom;

public class Dice {
    
    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 6;

    public int roll() {
        return ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE + 1);

        // ThreadLocalRandom è una classe di java.util.concurrent, alternativa a java.util.Random. Ogni thread ha la propria istanza, 
        // quindi niente contesa in scenari multi-thread (per un dado va benissimo, ma è comunque la scelta consigliata oggi rispetto a Random/Math.random()).

        // .current() → restituisce l'istanza associata al thread corrente
        // .nextInt(origin, bound) → genera un intero random nell'intervallo [origin, bound), cioè origin incluso, bound escluso

        // Per questo passo MAX_VALUE + 1 (cioè 7) invece di MAX_VALUE (6): altrimenti 6 non uscirebbe mai, dato che bound è escluso.
    
    }
}
