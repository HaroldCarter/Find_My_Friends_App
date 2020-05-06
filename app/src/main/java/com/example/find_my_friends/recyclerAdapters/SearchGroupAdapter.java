package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.math.BigDecimal;
import java.math.MathContext;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.LocationUtils.distanceBetweenTwoPointMiles;

public class SearchGroupAdapter extends FirestoreRecyclerAdapter<Group, SearchGroupAdapter.GroupOverviewHolder> implements Filterable {
    //private User User = currentUser;
    private OnItemClickListener listener;
    //private GroupOverviewHolder groupOverviewHolder;
    //private Group group;

    public SearchGroupAdapter(@NonNull FirestoreRecyclerOptions<Group> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull GroupOverviewHolder holder, int position, @NonNull Group model) {
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

    }



    @NonNull
    @Override
    public GroupOverviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_group_overview, parent, false);
        return new GroupOverviewHolder(v);
    }

    class GroupOverviewHolder extends RecyclerView.ViewHolder{
        TextView groupTitle;
        TextView groupDesc;
        TextView groupCreator;
        ImageView groupPhoto;
        ImageView groupCreatorPhoto;
        Button moreDetailsBTN;
        TextView groupDistance;
        TextView groupDate;
        TextView groupTime;




        public GroupOverviewHolder(@NonNull View itemView) {
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
            handleMoreDetailsBTN();
        }

        private void handleMoreDetailsBTN(){
            moreDetailsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(viewGroupRequestsBTN.getContext(), GroupDetailsActivity.class));
                    int position = getAdapterPosition();
                    //String groupID = getItem(position).getGroupID();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
