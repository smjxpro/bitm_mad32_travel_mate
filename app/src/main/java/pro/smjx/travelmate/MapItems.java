package pro.smjx.travelmate;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MapItems implements ClusterItem {

    private LatLng latLng;
    private String title;
    private String snippet;

    public MapItems(LatLng latLng, String title, String snippet) {
        this.latLng = latLng;
        this.title = title;
        this.snippet = snippet;
    }

    public MapItems(LatLng latLng, String title) {
        this.latLng = latLng;
        this.title = title;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
