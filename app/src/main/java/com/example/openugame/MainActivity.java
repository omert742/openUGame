package com.example.openugame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    int a = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        Button b1 = (Button) findViewById(R.id.button);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

// Add a new document with a generated ID
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(false);
        DatabaseReference myRef = database.getReference();
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("Omer", "connected");
                } else {
                    Log.d("Omer", "not connected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Omer", "Listener was cancelled");
            }
        });
        // Read from the database
        Log.d("Omer", "myDB: "+myRef);
        Log.d("Omer", "myDB2: "+myRef.getDatabase().getApp());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("Omer", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Omer", "Failed to read value.", error.toException());
            }
        });
        Task t1 = myRef.setValue("Hello, World!");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                a = a + 1;
                Log.d("Omer", "myDB1: "+t1.isCanceled());
                Log.d("Omer", "myDB2: "+t1.isComplete());
                Log.d("Omer", "myDB3: "+t1.isSuccessful());
                Log.e("Omer","Pl OK ");
                String a_str = String.valueOf(a);
                myRef.setValue(a_str).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("Omer","Test1");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Omer","Test2");
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.e("Omer","Test3");

                    }
                });
            }
        });
        myRef.push();

    }
}