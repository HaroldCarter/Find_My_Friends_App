package com.example.find_my_friends.groupUtil;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class GroupInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;

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
        ImageView imageViewGroupPhoto = view.findViewById(R.id.GroupPhoto_map_overview);
        TextView hostNameTextView = view.findViewById(R.id.HostedBy_map_overview);
        TextView groupTitleTextView = view.findViewById(R.id.GroupTitle_map_overview);

        //this is the custom wrapper class used for transferring data.
        GroupInfoWindowData infoWindowData = (GroupInfoWindowData) marker.getTag();

        //if the extra tag did exist then load it.
        if(infoWindowData != null) {
            hostNameTextView.setText(("Hosted By " + infoWindowData.getGroupCreatorDisplayName()));
            groupTitleTextView.setText(infoWindowData.getGroupTitle());
            Glide.with(context).load(infoWindowData.getGroupPhotoURI()).into(imageViewGroupPhoto);
            Glide.with(context).load(infoWindowData.getGroupCreatorUserPhotoURL()).into(imageViewProfilePhoto);
            return view;
        }
        return null;
    }
}
