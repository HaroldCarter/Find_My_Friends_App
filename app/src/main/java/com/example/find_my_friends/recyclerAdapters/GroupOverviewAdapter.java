package com.example.find_my_friends.recyclerAdapters;

import android.net.Uri;
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

import com.example.find_my_friends.userUtil.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

public class GroupOverviewAdapter extends FirestoreRecyclerAdapter<Group, GroupOverviewAdapter.GroupOverviewHolder> {

    private FirebaseFirestore db =  FirebaseFirestore.getInstance();
    //private GroupOverviewHolder groupOverviewHolder;
    //private Group group;

    public GroupOverviewAdapter(@NonNull FirestoreRecyclerOptions<Group> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull GroupOverviewHolder holder, int position, @NonNull Group model) {
        //super.onBindViewHolder(holder,position);
        final GroupOverviewHolder groupOverviewHolder = holder;
        //this.group = model;
        holder.groupTitle.setText(model.getGroupTitle());
        holder.groupDesc.setText(model.getGroupDesc());
        Glide.with(holder.groupPhoto.getContext()).load(model.getGroupPhotoURI()).into(holder.groupPhoto);

        DocumentReference docRef =  db.collection("Users").document(model.getGroupCreatorUserID());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if(user != null) {
                    Glide.with(groupOverviewHolder.groupCreatorPhoto.getContext()).load(user.getUserPhotoURL()).into(groupOverviewHolder.groupCreatorPhoto);
                    groupOverviewHolder.groupCreator.setText(("Hosted by " + user.getUsername()));
                }
            }
        });
        holder.groupDate.setText(model.getGroupMeetDate());
        holder.groupTime.setText(model.getGroupMeetTime());


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
        }
    }
}
