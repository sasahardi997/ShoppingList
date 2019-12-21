package com.myappcompany.hardi.shoppinglist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myappcompany.hardi.shoppinglist.model.Data;
import com.myappcompany.hardi.shoppinglist.recycler.MyViewHolder;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private FloatingActionButton fab_btn;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar=findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping List");



        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uId=mUser.getUid();

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Shopping List").child(uId);
        mDatabase.keepSynced(true);



        recyclerView=findViewById(R.id.recycler_home);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



        fab_btn=findViewById(R.id.fab);

        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog();
            }
        });

    }

    private void customDialog(){
        AlertDialog.Builder myDialog=new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater= LayoutInflater.from(HomeActivity.this);
        View myView=inflater.inflate(R.layout.input_data,null);

        final AlertDialog dialog=myDialog.create();
        dialog.setView(myView);

        final EditText type=myView.findViewById(R.id.edit_type);
        final EditText ammount=myView.findViewById(R.id.edit_ammount);
        final EditText note=myView.findViewById(R.id.edit_note);
        Button btnSave=myView.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mType=type.getText().toString().trim();
                String mAmount=ammount.getText().toString().trim();
                String mNote=note.getText().toString().trim();

                int amountInt=Integer.parseInt(mAmount);

                if(TextUtils.isEmpty(mType)){
                    type.setError("Required Field..");
                    return;
                }
                if(TextUtils.isEmpty(mAmount)) {
                    ammount.setError("Required Field..");
                    return;
                }
                if(TextUtils.isEmpty(mNote)){
                    note.setError("Required Field..");
                    return;
                }

                String id=mDatabase.push().getKey();
                String date=DateFormat.getDateInstance().format(new Date());

                Data data=new Data(mType,amountInt,mNote,date,id);

                mDatabase.child(id).setValue(data);


                Toast.makeText(getApplicationContext(),"Data Add",Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });



        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Data, MyViewHolder> adapter=new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                      Data.class,
                      R.layout.item_data,
                      MyViewHolder.class,
                      mDatabase
                )
        {
            @Override
            protected void populateViewHolder(MyViewHolder viewHolder, Data model, int position) {

                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setAmount(model.getAmount());

            }
        };

        recyclerView.setAdapter(adapter);

    }
}
