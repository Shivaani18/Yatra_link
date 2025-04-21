public class TicketType {
    private int typeId;
    private String typeName;
    private double price;
    private String description;
    private boolean isActive;

    public TicketType(int typeId, String typeName, double price, String description, boolean isActive) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.price = price;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters
    public int getTypeId() { return typeId; }
    public String getTypeName() { return typeName; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return typeName + " - ₹" + String.format("%.2f", price);
    }
}