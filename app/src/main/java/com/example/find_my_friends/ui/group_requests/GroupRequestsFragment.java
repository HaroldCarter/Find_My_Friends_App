package com.example.find_my_friends.ui.group_requests;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.GroupRequestsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupRequestsOverviewAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;

public class GroupRequestsFragment extends Fragment {



    private GroupRequestsViewModel groupRequestsViewModel;
    private GroupRequestsOverviewAdapter groupRequestsOverviewAdapter;
    private RecyclerView recyclerView;
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LinearLayoutManager linearLayoutManager;
    private CollectionReference groupsRef = db.collection("Groups");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        groupRequestsViewModel =
                ViewModelProviders.of(this).get(GroupRequestsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_group_requests, container, false);
        Toolbar toolbar = root.findViewById(R.id.groups_request_menubar);


        linearLayoutManager = new LinearLayoutManager(this.getContext());

        recyclerView = root.findViewById(R.id.group_requests_recyclerView);

        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("Group Requests");


        if(getActivity() != null) {
            getActivity().setActionBar(toolbar);
        }else{
            //display an error saying the program has lost reference to itself.
            Log.e(FIND_FRIENDS_KEY, "onCreateView: Lost reference to activity, application halted");
            getActivity().finish();
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null) {
                    ((MainActivity) getActivity()).openDrawer();
                }
            }
        });

        setupRecyclerView();
        return root;
    }

    private void setupRecyclerView(){
        Query query = groupsRef.whereEqualTo("groupCreatorUserID", mUser.getUid());
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        groupRequestsOverviewAdapter = new GroupRequestsOverviewAdapter(options);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(groupRequestsOverviewAdapter);



        groupRequestsOverviewAdapter.setOnItemClickListener(new GroupRequestsOverviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intent = new Intent(getContext(), GroupRequestsActivity.class);
                intent.putExtra("documentID",documentSnapshot.getId());
                startActivity(intent);
            }
        });



    }


    @Override
    public void onStart() {
        super.onStart();
        groupRequestsOverviewAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        groupRequestsOverviewAdapter.stopListening();
    }


}