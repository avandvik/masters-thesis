package objects;

public class Installation implements Comparable<Installation>{

    private final int id;
    private final String name;
    private final int openingHour;
    private final int closingHour;
    private final double latitude;
    private final double longitude;

    public Installation(int id, String name, int openingHour, int closingHour, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOpeningHour() {
        return openingHour;
    }

    public int getClosingHour() {
        return closingHour;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Installation " + this.name;
    }


    @Override
    public int compareTo(Installation o) {
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
        Installation that = (Installation) o;
        return id == that.id;
    }
}
