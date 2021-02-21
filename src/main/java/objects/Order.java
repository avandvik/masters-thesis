package objects;

public class Order {

    private int orderId;
    private boolean isMandatory;
    private boolean isDelivery;
    private double orderSize;
    private int installationId;

    public Order(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "objects.Order " + orderId;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isDelivery() {
        return isDelivery;
    }

    public double getOrderSize() {
        return orderSize;
    }

    public int getInstallationId() {
        return installationId;
    }
}
