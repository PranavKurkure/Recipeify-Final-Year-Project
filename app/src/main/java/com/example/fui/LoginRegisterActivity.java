package com.example.fui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String TAG = "LoginRegisterActivity";
    int AUTHUI_REQUEST_CODE = 10001;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    public void handleLoginRegister(View view)
    {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls("https://example.com","https://example.com" )
                .setLogo(R.drawable.recipeify)
                .setAlwaysShowSignInMethodScreen(true)
                .build();

        startActivityForResult(intent, AUTHUI_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AUTHUI_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                //we have signed in the user or we have new user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                Toast.makeText(this, "Logged in as : "+ user.getEmail(), Toast.LENGTH_LONG).show();
                if(user.getMetadata().getCreationTimestamp() == user.getMetadata().getLastSignInTimestamp())
                {
                    Toast.makeText(this, "Welcome new user", Toast.LENGTH_SHORT).show();
                    saveDataToFirestore(user);
                }
                else
                {
                    Toast.makeText(this, "Welcome "+ user.getDisplayName(), Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                this.finish();
            }
            else{
                //signing in failed
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if(response == null){
                    Log.d(TAG, "onActivityResult: the user has cancelled the sign in request");
                }else{
                    Log.d(TAG, "onActivityResult: ", response.getError());
                }
            }
        }
    }

    private void saveDataToFirestore(FirebaseUser user) {
        String KEY_NAME = "name";
        String KEY_ACCTYPE = "isPremium";
        String KEY_URL="image_url";
        String KEY_VEGETABLES="Vegetables";
        String KEY_FAV="Favorites";
        Map<String , Object> mp = new HashMap<>();
        String UID = user.getUid();
        String userName = user.getDisplayName();
        if(userName == null)
        {
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginRegisterActivity.this);
            if(acct != null)
            {
                userName = acct.getDisplayName();
            }
        }
        String contact;
        if(user.getEmail() == null)
        {
            contact = user.getPhoneNumber();
        }else{
            contact = user.getEmail();
        }

        mp.put(KEY_NAME, userName);
        mp.put(KEY_ACCTYPE, false);
        String KEY_CONTACT = "contact";
        mp.put(KEY_CONTACT, contact);
        mp.put(KEY_URL, null);



        //-------BELOW PART WILL ADD Vegetables and Favorites into Map-----------------//
        mp.put(KEY_VEGETABLES, new HashMap<String, Object>());
        Map<String, Object> Favorites = new HashMap<>();
        Favorites.put("Appetizers", new ArrayList<>());
        Favorites.put("Main Dish", new ArrayList<>());
        Favorites.put("Salads", new ArrayList<>());
        Favorites.put("Drinks", new ArrayList<>());
        Favorites.put("Dessert", new ArrayList<>());
        mp.put(KEY_FAV, Favorites);
        //----------------------------------------------------------------------------------//



        db.collection("Users")
                .document(UID)
                .set(mp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LoginRegisterActivity.this, "Signed in!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginRegisterActivity.this, "Upload failed due to\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
