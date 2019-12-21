package com.myappcompany.hardi.shoppinglist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private TextView totalsumResult;


    //Global variable

    private String type;
    private int amount;
    private String note;
    private String post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar=findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Shopping List");

        totalsumResult=findViewById(R.id.total_amount);


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


        //Total sum number

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int totalAmount = 0;

                for(DataSnapshot snap:dataSnapshot.getChildren()){
                    Data data=snap.getValue(Data.class);
                    totalAmount+=data.getAmount();

                    String sTOTAL=String.valueOf(totalAmount);
                    totalsumResult.setText(sTOTAL+"$");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



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
            protected void populateViewHolder(MyViewHolder viewHolder, final Data model, final int position) {

                viewHolder.setDate(model.getDate());
                viewHolder.setType(model.getType());
                viewHolder.setNote(model.getNote());
                viewHolder.setAmount(model.getAmount());

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key=getRef(position).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();

                        updateData();
                    }
                });

            }
        };

        recyclerView.setAdapter(adapter);
    }

    public void updateData(){
        AlertDialog.Builder myDialog=new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
        View mView=inflater.inflate(R.layout.update_inputfield,null);

        final AlertDialog dialog=myDialog.create();
        dialog.setView(mView);

        final EditText edit_type=mView.findViewById(R.id.edit_type_update);
        final EditText edit_amount=mView.findViewById(R.id.edit_ammount_update);
        final EditText edit_note=mView.findViewById(R.id.edit_note_update);

        edit_type.setText(type);
        edit_type.setSelection(type.length());

        edit_amount.setText(String.valueOf(amount));
        edit_amount.setSelection(String.valueOf(amount).length());

        edit_note.setText(note);
        edit_note.setSelection(note.length());


        Button btnUpdate=mView.findViewById(R.id.btn_update);
        Button btnDelete=mView.findViewById(R.id.btn_delete);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type=edit_type.getText().toString().trim();
                String mAmount=String.valueOf(amount);
                mAmount=edit_amount.getText().toString().trim();
                note=edit_note.getText().toString().trim();

                int intAmount=Integer.parseInt(mAmount);

                String date=DateFormat.getDateInstance().format(new Date());

                Data data=new Data(type,intAmount,note,date,post_key);

                mDatabase.child(post_key).setValue(data);


                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(post_key).removeValue();

                dialog.dismiss();
            }
        });


        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
