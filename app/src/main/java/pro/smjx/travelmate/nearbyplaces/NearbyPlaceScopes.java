package pro.smjx.travelmate.nearbyplaces;

import java.io.Serializable;

public class NearbyPlaceScopes implements Serializable {
    private String id, placeId, address, name, photoRef;
    private double lat, lng;

    public NearbyPlaceScopes(String id, String placeId, String address, String name, String photoRef, double lat, double lng) {
        this.id = id;
        this.placeId = placeId;
        this.address = address;
        this.name = name;
        this.photoRef = photoRef;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getPhotoRef() {
        return photoRef;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
