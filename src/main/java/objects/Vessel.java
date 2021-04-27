package objects;

public class Vessel implements Comparable<Vessel>{

    private final int id;
    private final String name;
    private final int capacity;
    private final double fcDesignSpeed;
    private final int returnTime;

    public Vessel(int id, String name, int capacity, double fcDesignSpeed, int returnTime) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.fcDesignSpeed = fcDesignSpeed;
        this.returnTime = returnTime;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getFcDesignSpeed() {
        return fcDesignSpeed;
    }

    public int getReturnTime() {
        return returnTime;
    }

    @Override
    public String toString() {
        return name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vessel vessel = (Vessel) o;
        return id == vessel.id;
    }
}
