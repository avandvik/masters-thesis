package objects;

import data.Problem;

public class Order implements Comparable<Order> {

    private final int orderId;
    private final boolean isMandatory;
    private final boolean isDelivery;
    private final int size;
    private final int installationId;
    private double penalty;

    public Order(int orderId, boolean isMandatory, boolean isDelivery, int size, int installationId) {
        this.orderId = orderId;
        this.isMandatory = isMandatory;
        this.isDelivery = isDelivery;
        this.size = size;
        this.installationId = installationId;
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

    public double getNoisySize() {
        return this.size + Problem.random.nextDouble();
    }

    public int getInstallationId() {
        return installationId;
    }

    public double getPostponementPenalty() {
        return this.penalty;
    }

    public double getNoisyPenalty() {
        return this.penalty + Problem.random.nextDouble();
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
        if (this.orderId < o.orderId) {
            return -1;
        } else if (this.orderId > o.orderId) {
            return 1;
        } else {
            return 0;
        }
    }
}
