package com.example.travelmatics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText etTitle;
    EditText etPrice;
    EditText etDescription;
    TravelDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        etTitle = (EditText) findViewById(R.id.etTitle);
        etPrice = (EditText) findViewById(R.id.etPrice);
        etDescription = (EditText) findViewById(R.id.etDescription);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getParcelableExtra("Deal");
        if(deal == null) {
            this.deal = new TravelDeal();
        }else{
            this.deal = deal;
            etTitle.setText(deal.getTitle());
            etPrice.setText(deal.getPrice());
            etDescription.setText(deal.getDescription());
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Travel Deal Saved", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            case R.id.delete_deal:
                deleteDeal();
                Toast.makeText(this, "Travel Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            case R.id.logout:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>(){
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User Logged Out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void clean() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
    }

    private void saveDeal() {
        deal.setTitle(etTitle.getText().toString());
        deal.setPrice(etPrice.getText().toString());
        deal.setDescription(etDescription.getText().toString());

        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        }else{
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal(){
        if(deal != null){
            mDatabaseReference.child(deal.getId()).removeValue();
        }
        else {
            Toast.makeText(this, "Please save deal before deleting", Toast.LENGTH_LONG).show();
        }
        return;
    }
    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_deal).setVisible(true);
            enableText(true);
        }
        else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_deal).setVisible(false);
            enableText(false);
        }
        return true;
    }
    private void enableText(boolean isEnabled){
        etTitle.setEnabled(isEnabled);
        etDescription.setEnabled(isEnabled);
        etPrice.setEnabled(isEnabled);
    }
}