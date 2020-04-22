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
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.find_my_friends.AddGroupActivity;
import com.example.find_my_friends.GroupDetailsActivity;
import com.example.find_my_friends.MainActivity;
import com.example.find_my_friends.R;
import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupOverviewAdapter;
import com.example.find_my_friends.recyclerAdapters.MyGroupAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import static com.example.find_my_friends.util.Constants.FIND_FRIENDS_KEY;

public class MyGroupsFragment extends Fragment {
    private RecyclerView recyclerView;
    private MyGroupsViewModel myGroupsViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private CollectionReference groupsRef;
    private MyGroupAdapter myGroupAdapter;
    private LinearLayoutManager linearLayoutManager;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myGroupsViewModel =
                ViewModelProviders.of(this).get(MyGroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_my_groups, container, false);
        Toolbar toolbar = root.findViewById(R.id.my_groups_menubar);
        toolbar.setNavigationIcon(R.drawable.svg_menu_primary);
        toolbar.setTitle("My Groups");

        db = FirebaseFirestore.getInstance();
         groupsRef = db.collection("Groups");
        linearLayoutManager = new LinearLayoutManager(this.getContext());


        //testFunction();

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
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        recyclerView = root.findViewById(R.id.MyGroupsRecycler);
       setupRecyclerView();
        return root;
    }


    private void testFunction(){
        DocumentReference docRef = db.collection("Groups").document("a918c058-b2c8-4eec-86ac-8bd02c9336cd");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //latlng crashes the app, its not saving it correct, being saved as a map.
                Group group = documentSnapshot.toObject(Group.class);
                //group.
            }
        });
    }




    //none of the recylcerView setups actually display anything, guide uses V7 app compat recycler and cards im using android x for both, probably that.
    private void setupRecyclerView(){
        Query query = groupsRef.whereEqualTo("groupCreatorUserID", mUser.getUid());
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        myGroupAdapter = new MyGroupAdapter(options);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myGroupAdapter);


        myGroupAdapter.setOnItemClickListener(new MyGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Intent intent = new Intent(getContext(), AddGroupActivity.class);
                intent.putExtra("documentID",documentSnapshot.getId());
                startActivity(intent);
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        myGroupAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        myGroupAdapter.stopListening();
    }
}