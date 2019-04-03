package pro.smjx.travelmate.nearbyplaces;

public class RadiusNearby {
    private String radiusTitle;
    private String radius;

    public RadiusNearby(String radiusTitle, String radius) {
        this.radiusTitle = radiusTitle;
        this.radius = radius;
    }

    public String getRadiusTitle() {
        return radiusTitle;
    }

    public String getRadius() {
        return radius;
    }
}
