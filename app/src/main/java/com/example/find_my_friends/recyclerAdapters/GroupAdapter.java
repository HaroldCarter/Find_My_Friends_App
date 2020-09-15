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
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static com.example.find_my_friends.util.Constants.currentUser;
import static com.example.find_my_friends.util.LocationUtils.distanceBetweenTwoPointMiles;

/**
 * A class containing an adapter user for displaying the information of a group on a cardview
 *
 * @author Harold Carter
 * @version v2.0
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.CurrentGroupHolder> {
    public ArrayList<Group> groups;
    private GroupAdapter.OnItemClickListener listener;

    public ArrayList<Group> getGroups() {
        return groups;
    }

    /**
     * the group holder for the recyclerview, this static subclass holds the information for the specific view model displayed in the recyclerview
     */
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

        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        /**
         * default constructor for the CurrentGroupHolder takes the view of the item being displayed in the recyclerview, and the onclick listener being set by the calling class, this listener will be triggered upon the more details button being triggered.
         * sets all the internal variables for the onscreenvariables to be equal to their counterparts
         * @param itemView View, the view that has been inflated and onscreen variable resources are being fetched from
         * @param listener onItemClickListener the listener that will be triggered upon the more details button being clicked.
         */
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

        /**
         * handles the click of the more detail button for every group adapter in the recycler view, this alerts the internal listener and passes the position to the listener so reference to the group being clicked can be made
         * @param listener Listener to trigger once interaction with the moredetailsBTN has been made.
         */
        private void handleMoreDetailsBTN(final OnItemClickListener listener) {
            moreDetailsBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    /**
     * the group adapters default constructor, takes the list of groups that are being displayed in the recyclerview  (passed by reference)
     * @param currentGroups ArrayList Groups containing the groups that are displayed in the recyclerview (made by query)
     */
    public GroupAdapter(ArrayList<Group> currentGroups) {
        groups = currentGroups;
    }

    /**
     * overrides the oncreate view holder function, called when each instance of the recyclerview's view card is is made, this links and inflates the layout from the resources and returns the reference ot the view.
     *
     * @param parent  The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return calls the default constructor for the CurrentGroupHolder
     */
    @Override
    @NonNull
    public CurrentGroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_group_overview, parent, false);
        return new CurrentGroupHolder(v, listener);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CurrentGroupHolder holder, int position) {
        if (groups != null && groups.size() != 0 && groups.get(position) != null) {
            attachDataToViewModel(holder, position);
        } else {
            holder.groupCreator.getRootView().setVisibility(View.GONE);
        }
    }

    /**
     * attaches the data given in the model to the viewholder, this is achieved in a another function to promote easy reading of code, however this code is effectively the functionality of the onbindviewholder override function, utilizes callback to request the most uptodate information regarding the users creator.
     *
     * @param holder The data holder Which should be loaded into and represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set.
     */
    private void attachDataToViewModel(CurrentGroupHolder holder, int position) {
        Group model = groups.get(position);
        holder.groupTitle.setText(model.getGroupTitle());
        holder.groupDesc.setText(model.getGroupDesc());
        Glide.with(holder.groupPhoto.getContext()).load(model.getGroupPhotoURI()).into(holder.groupPhoto);
        holder.db.collection("Users").document(model.getGroupCreatorUserID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User temp = documentSnapshot.toObject(User.class);
                if (temp != null) {
                    Glide.with(holder.groupCreatorPhoto.getContext()).load(temp.getUserPhotoURL()).into(holder.groupCreatorPhoto);
                    holder.groupCreator.setText(("Hosted by " + temp.getUsername()));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder.groupCreator.setText(("Hosted by " + "Deleted User"));
            }
        });
        Glide.with(holder.groupCreatorPhoto.getContext()).load(model.getGroupCreatorUserPhotoURL()).into(holder.groupCreatorPhoto);
        holder.groupCreator.setText(("Hosted by " + model.getGroupCreatorDisplayName()));
        holder.groupDate.setText(model.getGroupMeetDate());
        holder.groupTime.setText(model.getGroupMeetTime());

        int distance = (int) distanceBetweenTwoPointMiles(currentUser.getUserLat(), currentUser.getUserLong(), model.getGroupLatitude(), model.getGroupLongitude());
        if (distance <= 1) {
            holder.groupDistance.setText((distance + "Mile"));
        } else {
            holder.groupDistance.setText((distance + " Miles"));
        }
    }

    /**
     * gets the current size of the arraylist which contains the groups being displayed.
     *
     * @return Int, the size of the internal groups arrayList
     */
    @Override
    public int getItemCount() {
        return groups.size();
    }

    /**
     * public interface for the onclick listener, passed a position as a parameter to get reference to which group was interacted with.
     *
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * sets the adapters onclick listener to the inputted onclick listener passed as a parameter by reference
     *
     * @param listenerInput GroupAdapter.OnItemClickListener, listener to set as this adapters listener will be triggered when the details button is clicked.
     */
    public void setOnItemClickListener(GroupAdapter.OnItemClickListener listenerInput) {
        listener = listenerInput;
    }
}
