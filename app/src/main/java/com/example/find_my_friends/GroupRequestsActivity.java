package com.example.find_my_friends;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.UserGroupRequestsAdapter;
import com.example.find_my_friends.userUtil.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class GroupRequestsActivity extends AppCompatActivity {

    private UserGroupRequestsAdapter userAdapter;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("Users");
    private Group group;
    private ArrayList<User> users = new ArrayList<>();
    private TextView messageToUserTextview;
    private DocumentReference docRef;
    private Toolbar toolbar;
    private int count =0;


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
                    //setupRecyclerView();
                    if(group != null) {
                        toolbar.setTitle(group.getGroupTitle() + " requests");
                    }
                    loadUsers("load");
                }
            });
        }
    }


    private void loadUsers(final String mode){
            if (group.getRequestedMemberIDS() != null && group.getRequestedMemberIDS().size() != 0) {
                count = 0;
                for (String s : group.getRequestedMemberIDS()
                ) {
                    userRef.document(s).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User user = documentSnapshot.toObject(User.class);
                            users.add(user);
                            count++;
                            if (mode.equals("update")) {
                                updateList();
                            } else {
                                loadList();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //document was deleted half way through loading, should still count it as loaded as an attempt was made.
                            count++;
                            if (mode.equals("update")) {
                                updateList();
                            } else {
                                loadList();
                            }
                        }
                    });

                }
            }else{
                if(mode.equals("update")){
                    users.clear();
                    updateRecyclerView();
                }else{
                    setupRecyclerView();
                }
            }
        }


    private void loadList(){
        if(count >= group.getRequestedMemberIDS().size()){
            count = 0;
            //continue to load the groups arraylist in to the recyclerView.
            setupRecyclerView();
            //now listen out for changes to the document once loaded.

            //docRef.add
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("TAG", "listen:error", e);
                        return;
                    }

                    if(documentSnapshot != null && documentSnapshot.exists()) {
                        group = documentSnapshot.toObject(Group.class);

                        users.clear();
                        //now update the view.
                        loadUsers("update");
                    }
                }
            });


        }
    }

    private void updateList(){
        if(count >= group.getRequestedMemberIDS().size()){
            count = 0;
            //continue to load the groups arraylist in to the recyclerView.
            updateRecyclerView();

        }
    }

    private void updateRecyclerView(){
        userAdapter.notifyDataSetChanged();
    }



    private void setupRecyclerView() {
        //get the logical query for all users, (doesn't download them, just a logical requirement).
        //Query searchQuery = userRef.orderBy("uid");


        if(group.getRequestedMemberIDS() == null) {
            messageToUserTextview.setText(R.string.message_no_new_requests);
            messageToUserTextview.setVisibility(View.VISIBLE);
            return;
        }
        else if(group.getRequestedMemberIDS().toArray().length ==0){
            messageToUserTextview.setText(R.string.message_no_new_requests);
            messageToUserTextview.setVisibility(View.VISIBLE);
        }else{
            messageToUserTextview.setVisibility(View.INVISIBLE);
        }
        userAdapter = new UserGroupRequestsAdapter(users);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //not displaying the list group members, even through the list is being handed correctly.
        recyclerView.setAdapter(userAdapter);
        handleConfirmOnclick();
        handleDenyOnclick();
    }



    private void handleConfirmOnclick() {
        userAdapter.setConfrimOnItemClickListener(new UserGroupRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (group != null && group.getMembersOfGroupIDS().toArray().length >= 10) {
                    Snackbar.make(recyclerView, "this Group is already full", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                User user = users.get(position);
                db.collection("Users").document(user.getUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userUID = (String) documentSnapshot.get("uid");
                        //remove the group request
                        if (group != null && group.getRequestedMemberIDS() != null && (userUID != null)) {
                            group.getRequestedMemberIDS().remove(userUID);
                            //currentUser.removeRequestedMembership(group.getGroupID());
                            User userSnap = documentSnapshot.toObject(User.class);
                            if (userSnap != null) {
                                userSnap.removeMembershipRequest(group.getGroupID(), documentSnapshot);

                            }

                            docRef.update("requestedMemberIDS", group.getRequestedMemberIDS());
                        }
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

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });
    }

    private void handleDenyOnclick() {
        userAdapter.setDenyOnItemClickListener(new UserGroupRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                User user = users.get(position);
                db.collection("Users").document(user.getUID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String userUID = (String) documentSnapshot.get("uid");
                        if (group != null && group.getRequestedMemberIDS() != null && (userUID != null)) {
                            group.getRequestedMemberIDS().remove(userUID);
                            //currentUser.removeRequestedMembership(group.getGroupID());
                            Snackbar.make(recyclerView, "Group request Removed", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            docRef.update("requestedMemberIDS", group.getRequestedMemberIDS());
                            User userSnap = documentSnapshot.toObject(User.class);
                            if (userSnap != null) {
                                userSnap.removeMembershipRequest(group.getGroupID(), documentSnapshot);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(recyclerView, "Failed to deny member", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

}
