public class DonationRecord {
    private String donorName;
    private String paymentMode;
    private double amount;
    private String purpose;

    public DonationRecord(String donorName, String paymentMode, double amount, String purpose) {
        this.donorName = donorName;
        this.paymentMode = paymentMode;
        this.amount = amount;
        this.purpose = purpose;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public double getAmount() {
        return amount;
    }

    public String getPurpose() {
        return purpose;
    }
}
