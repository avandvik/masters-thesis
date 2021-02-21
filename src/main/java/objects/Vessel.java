package objects;

public class Vessel implements Comparable<Vessel>{

    private int id;
    private String name;
    private double capacity;
    private double returnTime;

    public Vessel(int id, String name, double capacity, double returnTime) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.returnTime = returnTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getReturnTime() {
        return returnTime;
    }

    @Override
    public String toString() {
        return "Vessel " + name;
    }

    @Override
    public int compareTo(Vessel o) {
        if (this.id < o.id) {
            return -1;
        } else if (this.id > o.id) {
            return 1;
        }
        return 0;
    }
}
