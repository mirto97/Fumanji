package com.project.fumanji.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private final boolean directionChoiceEnabled;   // se puoi scegliere di andare avanti o indietro
    private final boolean mandatoryCheckpointEnabled;   // se c'è l'obbligo di fermarsi al checkpoint
    private final boolean backwardAllowedAfterCheckpoint;   // se si può andare tornare indietro dopo aver raggiunto un cp
    private final List<List<Integer>> previousPositions;    // TODO perchè una lista nella lista?

    /**
     * metodo pubblico per creare board semplici
     * @param size
     */
    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = defaultSquares(size);
        this.directionChoiceEnabled = false;
        this.mandatoryCheckpointEnabled = false;
        this.backwardAllowedAfterCheckpoint = true;
        this.previousPositions = derivePreviousPositions(this.squares, size);
    }

    /**
     * metodo privato per creare board complesse
     * @param finishPosition
     * @param squares
     * @param directionChoiceEnabled
     * @param mandatoryCheckpointEnabled
     */
    private Board(int finishPosition, List<Square> squares, boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled, boolean backwardAllowedAfterCheckpoint) {
        this.finishPosition = finishPosition;
        this.squares = squares;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
        this.backwardAllowedAfterCheckpoint = backwardAllowedAfterCheckpoint;
        this.previousPositions = derivePreviousPositions(squares, finishPosition);
    }

    /**
     * TODO mi sa che mette le square di base
     * @param size
     * @return
     */
    private static List<Square> defaultSquares(int size) {
        List<Square> result = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            result.add(new Square(position));
        }
        return List.copyOf(result);
    }

    /**
     * qua invece è pubblico
     * @param size
     * @return
     */
    public static Builder builder(int size) {
        return new Builder(size);
    }

    // TODO studiare
    private static List<List<Integer>> derivePreviousPositions(List<Square> squares, int finishPosition) {
        List<List<Integer>> previous = new ArrayList<>();
        for (int position = 0; position <= finishPosition; position++) {
            previous.add(new ArrayList<>());
        }

        boolean[] reached = new boolean[finishPosition + 1];
        Deque<Integer> toVisit = new ArrayDeque<>();
        reached[0] = true;
        toVisit.add(0);

        while (!toVisit.isEmpty()) {
            int position = toVisit.poll();
            for (int next : squares.get(position).getNextPositions()) {
                if (next < 0 || next > finishPosition) {
                    continue;
                }
                previous.get(next).add(position);
                if (!reached[next]) {
                    reached[next] = true;
                    toVisit.add(next);
                }
            }
        }

        return previous.stream()
                .map(positions -> positions.stream().sorted().toList())
                .toList();
    }

    // getter

    public List<Integer> getNextPositions(int position) {
        return squares.get(position).getNextPositions();
    }

    public List<Integer> getPreviousPositions(int position) {
        return previousPositions.get(position);
    }

    public boolean isBackwardAllowedAfterCheckpoint() {
        return backwardAllowedAfterCheckpoint;
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }


    

    /**
     * classe per costruire le board
     * Builder
     */
    public static class Builder {

        private final int finishPosition;
        private final List<Square> squares;
        private boolean directionChoiceEnabled;
        private boolean mandatoryCheckpointEnabled;
        private boolean backwardAllowedAfterCheckpoint;

        /**
         * metodo per il Builder della board di base, TODO privato perchè?
         * @param size
         */
        private Builder(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Board size must be positive");
            }
            this.finishPosition = size;
            this.squares = new ArrayList<>();
            for (int position = 0; position <= size; position++) {
                this.squares.add(new Square(position));
            }
            this.directionChoiceEnabled = false;
            this.mandatoryCheckpointEnabled = false;
        }

        /**
         * metodo per posizionare la casella selezionata nella posizione selezionata
         * @param position
         * @param square
         * @return
         */
        public Builder square(int position, Square square) {
            // non posso creare cose prima della partenza e dopo il traguardo
            // TODO ma in futuro vorrei permetterlo
            if (position <= 0 || position >= finishPosition) {
                throw new IllegalArgumentException(
                        "Position must be between 1 and finishPosition - 1 (got " + position + ")");
            }
            squares.set(position, square);
            return this;
        }

        /**
         * metodo per attivare o disattivare la scelta del senso di marcia
         * @param enabled
         * @return
         */
        public Builder directionChoiceEnabled(boolean enabled) {
            this.directionChoiceEnabled = enabled;
            return this;
        }

        /**
         * metodo per aattivare o disattivare l'obbligo di fermarsi al cp
         * @param enabled
         * @return
         */
        public Builder mandatoryCheckpointEnabled(boolean enabled) {
            this.mandatoryCheckpointEnabled = enabled;
            return this;
        }

        public Builder backwardAllowedAfterCheckpoint(boolean enabled) {
            this.backwardAllowedAfterCheckpoint = enabled;
            return this;
        }

        /**
         * metodo che costruisce la board con le dovute informazioni
         * @return
         */
        public Board build() {
            return new Board(finishPosition, List.copyOf(squares), directionChoiceEnabled, mandatoryCheckpointEnabled, backwardAllowedAfterCheckpoint);
        }
    }
}