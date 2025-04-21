public class TicketBooking {
    private String devoteName;
    private String timeSlot;
    private int numTickets;
    private double amount;

    public TicketBooking(String name, String time, int tickets, double amount) {
        this.devoteName = name;
        this.timeSlot = time;
        this.numTickets = tickets;
        this.amount = amount;
    }

    // Getters
    public String getDevoteName() { return devoteName; }
    public String getTimeSlot() { return timeSlot; }
    public int getNumTickets() { return numTickets; }
    public double getAmount() { return amount; }
}