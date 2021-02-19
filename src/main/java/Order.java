public class Order {

    private int orderId;

    public Order(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "Order " + orderId;
    }
}
