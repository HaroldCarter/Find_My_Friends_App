package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.LocationUtils.distanceBetweenTwoPointMiles;

public class CurrentGroupAdapter extends RecyclerView.Adapter<CurrentGroupAdapter.CurrentGroupHolder> {
    public ArrayList<Group> groups;
    private CurrentGroupAdapter.OnItemClickListener listener;

    public ArrayList<Group> getGroups() {
        return groups;
    }

    static class CurrentGroupHolder extends RecyclerView.ViewHolder {
        private TextView groupTitle;
        private TextView groupDesc;
        private TextView groupCreator;
        private ImageView groupPhoto;
        private ImageView groupCreatorPhoto;
        private Button moreDetailsBTN;
        private TextView groupDistance;
        private TextView groupDate;
        private TextView groupTime;

        public CurrentGroupHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.GroupTitleCV);
            groupDesc = itemView.findViewById(R.id.GroupDescriptionCV);
            groupCreator = itemView.findViewById(R.id.HostedByCV);
            groupPhoto = itemView.findViewById(R.id.GroupPhotoCV);
            groupCreatorPhoto = itemView.findViewById(R.id.ProfilePhotoCV);
            groupDistance = itemView.findViewById(R.id.GroupDistanceCV);
            groupDate = itemView.findViewById(R.id.DateGroupTextviewCV);
            groupTime = itemView.findViewById(R.id.TimeGroupTextViewCV);
            moreDetailsBTN = itemView.findViewById(R.id.MoreDetailBTNCV);
            handleMoreDetailsBTN(listener);
        }

        private void handleMoreDetailsBTN(final OnItemClickListener listener){
            moreDetailsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(viewGroupRequestsBTN.getContext(), GroupDetailsActivity.class));
                    int position = getAdapterPosition();
                    //String groupID = getItem(position).getGroupID();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public CurrentGroupAdapter(ArrayList<Group> currentGroups) {
        groups = currentGroups;
    }

    @Override
    public CurrentGroupHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_group_overview, parent, false);
        return new CurrentGroupHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentGroupHolder holder, int position) {
        if(groups !=null && groups.size() != 0 && groups.get(position) != null) {
            Group model = groups.get(position);
            holder.groupTitle.setText(model.getGroupTitle());
            holder.groupDesc.setText(model.getGroupDesc());
            Glide.with(holder.groupPhoto.getContext()).load(model.getGroupPhotoURI()).into(holder.groupPhoto);
            Glide.with(holder.groupCreatorPhoto.getContext()).load(model.getGroupCreatorUserPhotoURL()).into(holder.groupCreatorPhoto);
            holder.groupCreator.setText(("Hosted by " + model.getGroupCreatorDisplayName()));
            holder.groupDate.setText(model.getGroupMeetDate());
            holder.groupTime.setText(model.getGroupMeetTime());
            BigDecimal distance = new BigDecimal(distanceBetweenTwoPointMiles(currentUser.getUserLat(), currentUser.getUserLong(), model.getGroupLatitude(), model.getGroupLongitude()));
            distance = distance.round(new MathContext(2));
            holder.groupDistance.setText( (distance + "Mile"));
        }else{
            //else the list of groups is empty or the group referred too is deleted.
            holder.groupCreator.getRootView().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(CurrentGroupAdapter.OnItemClickListener listenerInput){
        listener = listenerInput;
    }
}
