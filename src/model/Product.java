package model;

public class Product {
    private int id;
    private String code;
    private String name;
    private int amount;
    private double price;
    private byte[] thumbnail;

    public Product() {}

    public Product(String code, String name, int amount, double price, byte[] thumbnail) {
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    public Product(int id, String code, String name, int amount, double price, byte[] thumbnail) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.thumbnail = thumbnail;
    }

    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getAmount() { return amount; }
    public double getPrice() { return price; }
    public byte[] getThumbnail() { return thumbnail; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setPrice(double price) { this.price = price; }
    public void setThumbnail(byte[] thumbnail) { this.thumbnail = thumbnail; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", code='" + code + "', name='" + name +
               "', amount=" + amount + ", price=" + price + "}";
    }
}
