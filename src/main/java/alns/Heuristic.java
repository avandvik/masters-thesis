package alns;

public abstract class Operator {

    private double weight;
    private double score;
    private int selections;

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return this.weight;
    }

    public void addToScore(double points) {
        this.score += points;
    }

    public void incrementSelections() {
        this.selections++;
    }

    // TODO: Parameterize r (replace 0.8 with 1 - r and 0.2 with r)
    public void smoothenWeights() {
        this.weight = 0.8 * this.weight + 0.2 * (this.score / this.selections);
    }
}
