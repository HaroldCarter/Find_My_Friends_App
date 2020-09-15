package com.example.find_my_friends.groupUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.find_my_friends.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * a class which enables the display of Group and User information on window adapters for the Map Overview.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;

    /**
     * default constructor for the GroupInfoWindowAdapter which sets the context of the adapters internal variable
     * @param contextInput Context of the activity/app which is implementing this adapter
     */
    public InfoWindowAdapter(Context contextInput) {
        this.context = contextInput;
    }

    /**
     * override from the original implemented adapter however the marker is not stored within this class as it heavily complicates later code, therefore this function is purely to satisfy the implemented methods
     * @param marker Marker this info window is being setup for
     * @return returns null as nothing is being processed in this function
     */
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * the the view which the infowindow will display, this function inflates the resource file for the content window and populates its contents with the infowindow data object, this returns the populated view which is then rendered as an image, therefore to update the image the entire view needs to be re-rendered
     * @param marker Marker being representing in the infowindow
     * @return View to be rendered into an image to be displayed on the map
     */
    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.group_overview_map_overview, null);
        ImageView imageViewProfilePhoto = view.findViewById(R.id.ProfilePhoto_map_overview);
        ImageView imageViewEmailIcon = view.findViewById(R.id.sendEmail_map_overview_imageView);
        TextView hostNameTextView = view.findViewById(R.id.HostedBy_map_overview);
        TextView groupTitleTextView = view.findViewById(R.id.GroupTitle_map_overview);
        TextView textViewETAUser = view.findViewById(R.id.ETA_textview_user);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.FAB_info_adapter);
        floatingActionButton.setVisibility(View.INVISIBLE);
        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();
        if (infoWindowData != null) {
            groupTitleTextView.setText(infoWindowData.getWindowTitle());
            Glide.with(context).load(infoWindowData.getWindowPhotoURL()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    updateMarkerAdapter(marker);
                    return false;
                }
            }).into(imageViewProfilePhoto);
            if (infoWindowData.getWindowLocation() != null && infoWindowData.getWindowModeOfTransportForUser() != null && infoWindowData.getTravelDuration() != null) {
                hostNameTextView.setText((infoWindowData.getWindowSecondaryTitle()));
                textViewETAUser.setVisibility(View.VISIBLE);
                imageViewEmailIcon.setVisibility(View.VISIBLE);
                switch (infoWindowData.getWindowModeOfTransportForUser()) {
                    case "Car":
                        textViewETAUser.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.svg_car_primary, 0, 0);
                        break;
                    case "Bike":
                        textViewETAUser.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.svg_bike_primary, 0, 0);
                        break;
                    default:
                        textViewETAUser.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.svg_person_black, 0, 0);
                        //person;
                }
                textViewETAUser.setText(("ETA " + (infoWindowData.getTravelDuration() / 60) + "Mins"));

            } else {
                floatingActionButton.setVisibility(View.VISIBLE);
                hostNameTextView.setText(("Hosted By " + infoWindowData.getWindowSecondaryTitle()));
            }
            return view;
        }
        return null;
    }

    /**
     * this function simply hide's and reveals the window window for a given marker, this inturn re-renders the image render for the infowindow view, this effectively updates the adapter
     * @param marker Marker that is having it's infowindow updated.
     */
    private void updateMarkerAdapter(Marker marker) {
        if (marker != null && marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
            marker.showInfoWindow();
        }


    }
}

