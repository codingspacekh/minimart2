package model;

public class Order {
    private int id;
    private String cashierId;
    private double total;
    private String createdAt;

    public Order() {}

    public Order(String cashierId, double total) {
        this.cashierId = cashierId;
        this.total = total;
    }

    public Order(int id, String cashierId, double total, String createdAt) {
        this.id = id;
        this.cashierId = cashierId;
        this.total = total;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getCashierId() { return cashierId; }
    public double getTotal() { return total; }
    public String getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setCashierId(String cashierId) { this.cashierId = cashierId; }
    public void setTotal(double total) { this.total = total; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}