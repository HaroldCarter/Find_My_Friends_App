package com.example.find_my_friends;

import android.os.Bundle;

import com.example.find_my_friends.groupUtil.Group;
import com.example.find_my_friends.recyclerAdapters.GroupOverviewAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;

public class SearchGroupsActivity extends AppCompatActivity {
    private Button backBTN;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference groupsRef = db.collection("Groups");

    private GroupOverviewAdapter groupOverviewAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_groups);
        backBTN = findViewById(R.id.BackButtonSearchPage);
        recyclerView = findViewById(R.id.SearchGroupRecycler);
        handleBackBTN();

        setupRecyclerView();


    }

    private void setupRecyclerView(){
        Query query = groupsRef.orderBy("groupTitle");
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>().setQuery(query, Group.class).build();
        groupOverviewAdapter = new GroupOverviewAdapter(options);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupOverviewAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupOverviewAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupOverviewAdapter.stopListening();
    }

    private void handleBackBTN(){
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
