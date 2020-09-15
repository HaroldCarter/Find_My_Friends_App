package com.example.find_my_friends.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static com.example.find_my_friends.groupUtil.GroupUtil.isUserAlreadyCompleted;
import static com.example.find_my_friends.userUtil.UserUtil.composeEmail;
import static com.example.find_my_friends.util.Constants.currentUser;

/**
 *  A class containing an adapter user for displaying the information of a User on a cardview
 *
 * @author Harold Carter
 * @version 1.0
 */
public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserviewHolder> {
    private Group group;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * default UserAdapter constructor which sets the super's internal variable to be equal to the passed collection/list of groups (handled by the firestoreRecyclerAdapter)
     *
     * @param options FirestoreRecyclerOptions<Group> the mutable List of groups that are being displayed in the recyclerview (used by FirestoreRecyclerAdapter adapters>
     * @param group Group that the user is a member of
     */
    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, Group group) {
        super(options);
        this.group = group;
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position  The position of the item within the adapter's data set.
     * @param model the mode (group) that is being displayed in this view instance.
     */
    @Override
    protected void onBindViewHolder(@NonNull UserviewHolder holder, int position, @NonNull User model) {
        //super.onBindViewHolder(holder,position);
        holder.userDisplayName.setText(model.getUsername());
        holder.userEmailAddress.setText(model.getUserEmailAddress());
        Glide.with(holder.userProfilePhoto.getContext()).load(model.getUserPhotoURL()).into(holder.userProfilePhoto);
        if (group != null && isUserAlreadyCompleted(group, model)) {
            holder.arrivalIcon.setVisibility(View.VISIBLE);
            holder.arrivalIcon.setImageResource(R.drawable.svg_star_primary);
        }
        holder.sendEmailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                composeEmail(holder.sendEmailIcon.getContext(), new String[]{model.getUserEmailAddress()}, ("New Message From " + currentUser.getUsername()));
            }
        });
        holder.sendEmailIcon.setImageResource(R.drawable.svg_send_primary);


    }

    /**
     * overrides the oncreate view holder function, called when each instance of the recyclerview's view card is is made, this links and inflates the layout from the resources and returns the reference ot the view.
     *
     * @param parent  The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return calls the default constructor for the UserviewHolder
     */
    @NonNull
    @Override
    public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_user_overview, parent, false);
        return new UserviewHolder(v);
    }

    /**
     * the UserviewHolder for the recyclerview, this subclass holds the information for the specific view model displayed in the recyclerview
     */
    class UserviewHolder extends RecyclerView.ViewHolder {
        TextView userDisplayName;
        TextView userEmailAddress;
        ImageView userProfilePhoto;
        ImageView arrivalIcon;
        ImageView sendEmailIcon;


        /**
         * default constructor for the UserviewHolder takes the itemview as a parameter and links its internal variables to that of the onscreen variables in the view.
         *
         * @param itemView the view that is representing the data held in this viewholder.
         */
        public UserviewHolder(@NonNull View itemView) {
            super(itemView);
            userDisplayName = itemView.findViewById(R.id.user_cardview_displayTextView);
            userEmailAddress = itemView.findViewById(R.id.user_cardview_display_email);
            userProfilePhoto = itemView.findViewById(R.id.user_cardview_profile_photo);
            arrivalIcon = itemView.findViewById(R.id.user_cardview_adminPhoto);
            sendEmailIcon = itemView.findViewById(R.id.user_cardview_emailUserIcon);

        }
    }
}
