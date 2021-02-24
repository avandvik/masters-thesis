package objects;

public class Order {

    private int orderId;
    private boolean isMandatory;
    private boolean isDelivery;
    private double size;
    private int installationId;

    public Order(int orderId) {
        this.orderId = orderId;
    }

    public Order(int orderId, boolean isMandatory, boolean isDelivery, double size, int installationId) {
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

    public double getSize() {
        return size;
    }

    public int getInstallationId() {
        return installationId;
    }

    @Override
    public String toString() {
        return "Order " + orderId;
    }
}
