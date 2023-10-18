package main.java;

public class FitnessIndexPair {

    int index;
    int fitness;

    public FitnessIndexPair(int index, int fitness) {
        this.index = index;
        this.fitness = fitness;
    }

    public int getFitness() {
        return fitness;
    }

    public int getIndex() {
        return index;
    }
}
