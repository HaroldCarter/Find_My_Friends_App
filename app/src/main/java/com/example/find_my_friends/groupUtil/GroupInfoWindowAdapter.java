package com.example.find_my_friends.groupUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.find_my_friends.R;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.find_my_friends.util.Constants.currentUser;

public class GroupInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public GroupInfoWindowAdapter(Context contextInput) {
        this.context = contextInput;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.group_overview_map_overview, null);

        ImageView imageViewProfilePhoto = view.findViewById(R.id.ProfilePhoto_map_overview);
        //ImageView imageViewGroupPhoto = view.findViewById(R.id.GroupPhoto_map_overview);
        TextView hostNameTextView = view.findViewById(R.id.HostedBy_map_overview);
        TextView groupTitleTextView = view.findViewById(R.id.GroupTitle_map_overview);
        TextView textViewETAUser = view.findViewById(R.id.ETA_textview_user);


        FloatingActionButton floatingActionButton= view.findViewById(R.id.FAB_info_adapter);
        //this is the custom wrapper class used for transferring data.
        GroupInfoWindowData infoWindowData = (GroupInfoWindowData) marker.getTag();

        //if the extra tag did exist then load it.
        if(infoWindowData != null) {
            hostNameTextView.setText(("Hosted By " + infoWindowData.getGroupCreatorDisplayName()));
            groupTitleTextView.setText(infoWindowData.getGroupTitle());


            Glide.with(context).load(infoWindowData.getGroupCreatorUserPhotoURL()).listener(new RequestListener<Drawable>() {
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


            //if we are not the creator because then
            if(!infoWindowData.getGroupCreatorUID().equals(currentUser.getUID())) {
                db.collection("Users").document(infoWindowData.getGroupCreatorUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User temp = documentSnapshot.toObject(User.class);
                        if (temp != null) {
                            Glide.with(context).load(temp.getUserPhotoURL()).listener(new RequestListener<Drawable>() {
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
                            hostNameTextView.setText(("Hosted by " + temp.getUsername()));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Glide.with(holder.groupCreatorPhoto.getContext()).load(temp.getUserPhotoURL()).into(holder.groupCreatorPhoto);
                        hostNameTextView.setText(("Hosted by " + "Deleted User"));
                    }
                });
            }else{
                Glide.with(context).load(currentUser.getUserPhotoURL()).listener(new RequestListener<Drawable>() {
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
            }

            if(infoWindowData.getUserLocation() != null && infoWindowData.getModeOfTransportUser() != null && infoWindowData.getTravelDuration() != null){
                hostNameTextView.setText(infoWindowData.getGroupCreatorDisplayName());
                floatingActionButton.setVisibility(View.INVISIBLE);
                textViewETAUser.setVisibility(View.VISIBLE);

                switch (infoWindowData.getModeOfTransportUser()){
                    case "Car":
                        //doesn't actually load drawable to the screen
                        textViewETAUser.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.svg_car_primary,0 ,0 );
                        break;
                    case "Bike":
                        textViewETAUser.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.svg_bike_primary,0 ,0 );
                        break;
                    default:
                        textViewETAUser.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.svg_person_black,0 ,0 );
                        //person;
                }
                textViewETAUser.setText(("ETA " + (infoWindowData.getTravelDuration()/60) + "Mins"));

            }

            return view;
        }
        return null;
    }




    private void updateMarkerAdapter(Marker marker) {
        //if the marker is currently being viewed then update it.
        if (marker != null && marker.isInfoWindowShown())
        {
            marker.hideInfoWindow(); // Calling only showInfoWindow() throws an error
            marker.showInfoWindow();
        }


    }
}

