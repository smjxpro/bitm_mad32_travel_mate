package pro.smjx.travelmate.nearbyplaces;

public class PlacesNearby {
    private String placeName;
    private String placeCode;

    public PlacesNearby(String placeName, String placeCode) {
        this.placeName = placeName;
        this.placeCode = placeCode;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceCode() {
        return placeCode;
    }
}
