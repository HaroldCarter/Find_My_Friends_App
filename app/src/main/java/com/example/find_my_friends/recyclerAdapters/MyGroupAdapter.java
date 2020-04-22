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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyGroupAdapter extends FirestoreRecyclerAdapter<Group, MyGroupAdapter.GroupOverviewHolder> {

    private FirebaseFirestore db =  FirebaseFirestore.getInstance();
    private OnItemClickListener listener;
    //private GroupOverviewHolder groupOverviewHolder;
    //private Group group;

    public MyGroupAdapter(@NonNull FirestoreRecyclerOptions<Group> options) {
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


    }



    @NonNull
    @Override
    public GroupOverviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_my_group, parent, false);
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
            groupTitle = itemView.findViewById(R.id.GroupTitle_my_groupCV);
            groupDesc = itemView.findViewById(R.id.GroupDescription_my_groupCV);
            groupCreator = itemView.findViewById(R.id.HostedBy_my_groupCV);
            groupPhoto = itemView.findViewById(R.id.GroupPhoto_my_groupCV);
            groupCreatorPhoto = itemView.findViewById(R.id.ProfilePhoto_my_groupCV);
            groupDistance = itemView.findViewById(R.id.GroupDistance_my_groupCV);
            groupDate = itemView.findViewById(R.id.DateGroupTextview_my_groupCV);
            groupTime = itemView.findViewById(R.id.TimeGroupTextView_my_groupCV);
            moreDetailsBTN = itemView.findViewById(R.id.MoreDetailBTN_my_groupCV);
            handleMoreDetailsBTN();
        }

        private void handleMoreDetailsBTN(){
            moreDetailsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startActivity(new Intent(moreDetailsBTN.getContext(), GroupDetailsActivity.class));
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
}
