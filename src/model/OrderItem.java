package model;

public class OrderItem {
    private int id;
    private int orderId;
    private String productCode;
    private String productName;
    private int amount;
    private double unitPrice;
    private double subtotal;
    private byte[] thumbnail;

    public OrderItem() {}

    public OrderItem(String productCode, String productName, int amount, double unitPrice, byte[] thumbnail) {
        this.productCode = productCode;
        this.productName = productName;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.subtotal = amount * unitPrice;
        this.thumbnail = thumbnail;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public int getAmount() { return amount; }
    public double getUnitPrice() { return unitPrice; }
    public double getSubtotal() { return subtotal; }
    public byte[] getThumbnail() { return thumbnail; }

    public void setId(int id) { this.id = id; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setAmount(int amount) {
        this.amount = amount;
        this.subtotal = amount * unitPrice;
    }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = amount * unitPrice;
    }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public void setThumbnail(byte[] thumbnail) { this.thumbnail = thumbnail; }
}
