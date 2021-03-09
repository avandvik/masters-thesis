package alns.heuristics;

public abstract class Heuristic {

    private String name;
    private boolean destroy;
    private boolean repair;
    private double weight;
    private double score;
    private int selections;

    public Heuristic(String name, boolean destroy, boolean repair) {
        this.name = name;
        this.destroy = destroy;
        this.repair = repair;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return this.weight;
    }

    public void addToScore(double points) {
        this.score += points;
    }

    public void resetScoreAndUpdateWeight() {
        this.smoothenWeights();
        this.score = 0.0;
    }

    public void incrementSelections() {
        this.selections++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    public boolean isRepair() {
        return repair;
    }

    public void setRepair(boolean repair) {
        this.repair = repair;
    }

    // TODO: Parameterize r (replace 0.8 with 1 - r and 0.2 with r)
    private void smoothenWeights() {
        this.weight = 0.8 * this.weight + 0.2 * (this.score / this.selections);
    }
}
