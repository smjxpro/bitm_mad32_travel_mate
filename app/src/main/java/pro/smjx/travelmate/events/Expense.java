package pro.smjx.travelmate.events;

public class Expense {
    private String id, cause;
    private double amount;
    private long time;

    public Expense() {
    }

    public Expense(String id, String cause, double amount, long time) {
        this.id = id;
        this.cause = cause;
        this.amount = amount;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getCause() {
        return cause;
    }

    public double getAmount() {
        return amount;
    }

    public long getTime() {
        return time;
    }
}
