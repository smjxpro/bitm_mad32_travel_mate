package pro.smjx.travelmate.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.placedetails.Review;

public class ReviewAdapter extends ArrayAdapter<Review> {
    private Context context;
    private List<Review> reviews;
    private SimpleDateFormat dateFormat;
    private Date date = new Date();

    public ReviewAdapter(@NonNull Context context, List<Review> reviews) {
        super(context, R.layout.review_row, reviews);
        this.reviews = reviews;
        this.context = context;
        dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm:ss a");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.review_row, parent, false);
        ImageView imageView = convertView.findViewById(R.id.imageView);
        TextView nameTv = convertView.findViewById(R.id.nameTv);
        TextView ratingTv = convertView.findViewById(R.id.ratingTv);
        TextView dateTv = convertView.findViewById(R.id.dateTv);
        if (reviews.get(position).getProfilePhotoUrl() != null) {
            Picasso.get().load(Uri.parse(reviews.get(position).getProfilePhotoUrl())).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
        nameTv.setText(reviews.get(position).getAuthorName());
        ratingTv.setText(String.valueOf(reviews.get(position).getRating()));
        date.setTime(Long.parseLong(reviews.get(position).getTime() + "000"));
        dateTv.setText(dateFormat.format(date));

        return convertView;
    }
}
