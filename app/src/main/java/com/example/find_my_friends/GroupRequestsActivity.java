package com.example.find_my_friends;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.MyGroupAdapter;
import com.example.find_my_friends.recyclerAdapters.UserGroupRequestsAdapter;
import com.example.find_my_friends.userUtil.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;
import static com.example.find_my_friends.util.Constants.currentUser;

public class GroupRequestsActivity extends AppCompatActivity {

    private UserGroupRequestsAdapter userAdapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");
    private Group group;
    private TextView messageToUserTextview;
    private DocumentReference docRef;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_requests);

        toolbar = findViewById(R.id.group_requests_activity_menubar);
        recyclerView = findViewById(R.id.group_requests_users_recycler);
        messageToUserTextview = findViewById(R.id.group_request_user_message_textview);


        toolbar.setNavigationIcon(R.drawable.svg_back_arrow_primary);
        toolbar.setTitle("Group Requests");


        this.setActionBar(toolbar);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        handleLoadingData();

    }


    public void handleLoadingData() {
        String documentID = getIntent().getStringExtra("documentID");
        if (documentID != null) {
            this.docRef = db.collection("Groups").document(documentID);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    group = documentSnapshot.toObject(Group.class);
                    setupRecyclerView();
                    toolbar.setTitle(group.getGroupTitle() + " requests");
                }
            });
        }
    }


    private void setupRecyclerView() {
        //get the logical query for all users, (doesn't download them, just a logical requirement).
        Query searchQuery = userRef.orderBy("uid");
        boolean matchFound = false;
        //no need to override method and hide users if no user is found because a group will always have at-least one member.
        if (group.getRequestedMemberIDS() == null) {
            messageToUserTextview.setText(R.string.message_no_new_requests);
            messageToUserTextview.setVisibility(View.VISIBLE);
            return;
        }
        if (group.getRequestedMemberIDS().toArray().length == 0) {
            messageToUserTextview.setText(R.string.message_no_new_requests);
            messageToUserTextview.setVisibility(View.VISIBLE);
        } else {
            for (String memberID : group.getRequestedMemberIDS()
            ) {
                //might return nothing when there are two members in a group as this logically reduces the dataset each time we call it.
                searchQuery = userRef.whereEqualTo("uid", memberID);
                matchFound = true;
            }


            if (matchFound) {
                // Query query = userRef.whereIn("uid", group.getMembersOfGroupIDS());
                FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>().setQuery(searchQuery, User.class).build();
                userAdapter = new UserGroupRequestsAdapter(options);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setAdapter(userAdapter);
                userAdapter.startListening();


                handleConfirmOnclick();
                handleDenyOnclick();

            }
        }

    }

    private void handleConfirmOnclick() {
        userAdapter.setConfrimOnItemClickListener(new UserGroupRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position, View view) {
                if (group != null && group.getMembersOfGroupIDS().toArray().length >= 10) {
                    Snackbar.make(recyclerView, "this Group is already full", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                String userUID = (String) documentSnapshot.get("uid");

                //add the user to the group.
                if (userUID != null && group != null) {
                    //if the conditions are valid
                    group.appendMemberGroupOnly(userUID);
                    User userSnap = documentSnapshot.toObject(User.class);
                    if (userSnap != null) {
                        userSnap.appendMembership(group.getGroupID(), documentSnapshot);
                    }
                    docRef.update("membersOfGroupIDS", group.getMembersOfGroupIDS());
                    Snackbar.make(recyclerView, "Member Added to group", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                }
                //remove the group request
                if (group != null && group.getRequestedMemberIDS() != null && (userUID != null)) {
                    group.getRequestedMemberIDS().remove(userUID);
                    //currentUser.removeRequestedMembership(group.getGroupID());
                    User userSnap = documentSnapshot.toObject(User.class);
                    if (userSnap != null) {
                        userSnap.removeMembershipRequest(group.getGroupID(), documentSnapshot);

                    }

                    docRef.update("requestedMemberIDS", group.getRequestedMemberIDS());
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

    private void handleDenyOnclick() {
        userAdapter.setDenyOnItemClickListener(new UserGroupRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position, View view) {
                String userUID = (String) documentSnapshot.get("uid");
                if (group != null && group.getRequestedMemberIDS() != null && (userUID != null)) {
                    group.getRequestedMemberIDS().remove(userUID);
                    //currentUser.removeRequestedMembership(group.getGroupID());
                    Snackbar.make(recyclerView, "Group request Removed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    docRef.update("requestedMemberIDS", group.getRequestedMemberIDS());
                    view.setVisibility(View.GONE);
                    User userSnap = documentSnapshot.toObject(User.class);
                    if (userSnap != null) {
                        userSnap.removeMembershipRequest(group.getGroupID(), documentSnapshot);
                    }
                }

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (userAdapter != null) {
            userAdapter.startListening();
        }


    }


    @Override
    protected void onStop() {
        super.onStop();
        if (userAdapter != null) {
            userAdapter.stopListening();
        }
    }

}
