package objects;

public class Order implements Comparable<Order> {

    private int orderId;
    private boolean isMandatory;
    private boolean isDelivery;
    private int size;
    private int installationId;
    private double penalty;

    public Order(int orderId, boolean isMandatory, boolean isDelivery, int size, int installationId, double penalty) {
        this.orderId = orderId;
        this.isMandatory = isMandatory;
        this.isDelivery = isDelivery;
        this.size = size;
        this.installationId = installationId;
        this.penalty = penalty;
    }

    public int getOrderId() {
        return orderId;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isDelivery() {
        return isDelivery;
    }

    public int getSize() {
        return size;
    }

    public int getInstallationId() {
        return installationId;
    }

    public double getPostponementPenalty() {
        return this.penalty;
    }

    public void setPostponementPenalty(double penalty) {
        this.penalty = penalty;
    }

    @Override
    public String toString() {
        return "(O" + orderId + "-" + (this.isMandatory ? "M" : "O") + (this.isDelivery ? "D" : "P") +
                "-I" + this.installationId + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId;
    }

    @Override
    public int compareTo(Order o) {
        if (this.size < o.size) {
            return -1;
        } else if (this.size > o.size) {
            return 1;
        } else {
            return 0;
        }
    }
}
