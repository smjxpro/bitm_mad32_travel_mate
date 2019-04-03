package pro.smjx.travelmate.events;

public class Event {
    private String id, name, startingLocation, destination;
    private long departureDate, creatingDate, endingDate;
    private double budget, expenses;

    public Event() {
    }

    public Event(String name, String startingLocation, String destination, long departureDate, long creatingDate, long endingDate, double budget, double expenses) {
        this.name = name;
        this.startingLocation = startingLocation;
        this.destination = destination;
        this.departureDate = departureDate;
        this.creatingDate = creatingDate;
        this.endingDate = endingDate;
        this.budget = budget;
        this.expenses = expenses;
    }

    public Event(String id, String name, String startingLocation, String destination, long departureDate, long creatingDate, long endingDate, double budget, double expenses) {
        this.id = id;
        this.name = name;
        this.startingLocation = startingLocation;
        this.destination = destination;
        this.departureDate = departureDate;
        this.creatingDate = creatingDate;
        this.endingDate = endingDate;
        this.budget = budget;
        this.expenses = expenses;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public String getDestination() {
        return destination;
    }

    public long getDepartureDate() {
        return departureDate;
    }

    public long getCreatingDate() {
        return creatingDate;
    }

    public long getEndingDate() {
        return endingDate;
    }

    public double getBudget() {
        return budget;
    }

    public double getExpenses() {
        return expenses;
    }
}
