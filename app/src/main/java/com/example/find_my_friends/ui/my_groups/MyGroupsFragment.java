package com.example.find_my_friends.ui.my_groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.AddGroupActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.MyGroupAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;

/**
 * the my groups fragment responsible for displaying the groups the signed in user is the creator of.
 *
 * @author Harold Carter
 * @version 3.0
 */
public class MyGroupsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private CollectionReference groupsRef;
    private MyGroupAdapter myGroupAdapter;
    private LinearLayoutManager linearLayoutManager;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here
     * @return the View for the fragment's UI, or null.
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_groups, container, false);
        Toolbar toolbar = root.findViewById(R.id.my_groups_menubar);
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("My Groups");
        db = FirebaseFirestore.getInstance();
        groupsRef = db.collection("Groups");
        linearLayoutManager = new LinearLayoutManager(this.getContext());
        if (getActivity() != null) {
            getActivity().setActionBar(toolbar);
        } else {
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).openDrawer();
                }
            }
        });
        recyclerView = root.findViewById(R.id.MyGroupsRecycler);
        setupRecyclerView();
        return root;
    }


    /**
     * initializes the recyclerview to user a firestoreRecyclerview, meaning this recyclerview is self updating and doesn't require a snapshot listener; this is because the transaction can be made with a single query.
     */
    private void setupRecyclerView() {
        Query query = groupsRef.whereEqualTo("groupCreatorUserID", mUser.getUid());
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        myGroupAdapter = new MyGroupAdapter(options);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myGroupAdapter);
        myGroupAdapter.setOnItemClickListener(new MyGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intent = new Intent(getContext(), AddGroupActivity.class);
                intent.putExtra("documentID", documentSnapshot.getId());
                startActivity(intent);
            }
        });

    }

    /**
     * overrides on start to alert the firestoreRecyclerview adapter to listen for changes
     */
    @Override
    public void onStart() {
        super.onStart();
        myGroupAdapter.startListening();
    }

    /**
     * overrides on stop to alert the firestoreRecyclerview adapter to stop listening for changes
     */
    @Override
    public void onStop() {
        super.onStop();
        myGroupAdapter.stopListening();
    }
}