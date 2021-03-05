package objects;

import java.util.Objects;

public class Vessel implements Comparable<Vessel>{

    private int id;
    private String name;
    private double capacity;
    private int returnTime;

    public Vessel(int id, String name, double capacity, int returnTime) {
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

    public int getReturnTime() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vessel vessel = (Vessel) o;
        return id == vessel.id;
    }
}
