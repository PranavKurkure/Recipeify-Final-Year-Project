package com.example.fui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.fui.Fragments.FavoritesFragment;
import com.example.fui.Fragments.GroceryFragment;
import com.example.fui.Fragments.HomeFragment;
import com.example.fui.Fragments.ProfileFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import com.razorpay.PaymentResultListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, PaymentResultListener {

    AppCompatTextView userType, tvTitle;
    ImageButton btnMode;
    private int count = 1;


    private String userName, accType, contact, UID;
    private String KEY_NAME = "name", KEY_CONTACT = "contact", KEY_ACCTYPE = "isPremium", KEY_URL="image_url";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userRef;
    private FirebaseUser user;
    private int userFavorites;
    BottomNavigationView bottomNav;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null)
        {
            userName = user.getDisplayName();
            if(user.getEmail() == null)
            {
                contact = user.getPhoneNumber();
            }else
            {
                contact = user.getEmail();
            }
            UID = user.getUid();
            loadFromFirestore();

        }



        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);


        userType = findViewById(R.id.userType);
        tvTitle = findViewById(R.id.tvTitle);
        btnMode = findViewById(R.id.btnMode);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        bottomNav.getOrCreateBadge(R.id.navFav).setVisible(true);
        bottomNav.getBadge(R.id.navFav).setNumber(count);
        bottomNav.getBadge(R.id.navFav).setBadgeTextColor(getResources().getColor(R.color.white));

        checkForDynamicLinks();





        if(savedInstanceState != null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }


        SharedPreferences appSettingPrefs = getSharedPreferences("AppSettingsPref", 0);
        final SharedPreferences.Editor sharedPrefsEdit = appSettingPrefs.edit();
        final Boolean isNightMode = appSettingPrefs.getBoolean("NightMode", false);
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMode(isNightMode, sharedPrefsEdit);
            }
        });







        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        if(!isConnected(this))
        {
            showCustomDialog();
        }

//        updateBadgeCount(count);

    }


    private void changeMode(Boolean isNightMode, SharedPreferences.Editor sharedPrefsEdit) {
        if(isNightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            btnMode.setImageResource(R.drawable.ic_light_mode);
            btnMode.setActivated(false);
            sharedPrefsEdit.putBoolean("NightMode", false);
            sharedPrefsEdit.apply();

        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            btnMode.setImageResource(R.drawable.ic_dark_mode);
            btnMode.setActivated(true);
            sharedPrefsEdit.putBoolean("NightMode", true);
            sharedPrefsEdit.apply();
        }
    }


    //This method will load data from the Firebase Firestore
    private void loadFromFirestore() {
        userRef = db.document("Users/"+UID);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            boolean var = documentSnapshot.getBoolean("isPremium");
                            if(var)
                            {
                                userType.setText("PREMIUM");
                            }else{
                                userType.setText("");
                                userType.setVisibility(View.INVISIBLE);
                            }
                        }else{
                            Toast.makeText(MainActivity.this, "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error occurred\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    //showCustomDialog will be used to display ALERTBOX in case if the device is not connected to any network
    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please connect to internet to proceed further")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        AlertDialog.Builder bld = new AlertDialog.Builder(MainActivity.this);
                        bld.setMessage("Choose network")
                                .setCancelable(false)
                                .setPositiveButton("Mobile data", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent=new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Wi-Fi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                    }
                                });

                        bld.show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Sorry but you cannot access content without any network", Toast.LENGTH_LONG).show();
                    }
                })
        .setIcon(R.drawable.ic_signal_wifi_off);

        builder.show();
    }

    //isConnected method will check the WiFi/Data settings with the help of broadcast receivers and return boolean value accordingly
    private boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn != null && wifiConn.isConnected()) || (mobileConn!=null && mobileConn.isConnected()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFrag = null;

            if(item.getItemId() == R.id.navProfile)
            {
//                Toast.makeText(MainActivity.this, "Profile clicked", Toast.LENGTH_LONG).show();
                ProfileFragment frag = new ProfileFragment();
                frag.show(getSupportFragmentManager(), "MyFragment");
            }
            else if(item.getItemId() == R.id.navWrite)
            {
                Intent intent = new Intent(MainActivity.this, RecipeUpload.class);
                startActivity(intent);
            }
            else {
                switch (item.getItemId()) {
                    case R.id.navHome:
                        selectedFrag = new HomeFragment();
                        break;

                    case R.id.navGrocery:
                        selectedFrag = new GroceryFragment();
                        break;

                    case R.id.navFav:
                        selectedFrag = new FavoritesFragment();
                        break;

                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFrag).commit();
            }

            return true;

        }
    };

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null)
        {
            startLoginActivity();
            return;
        }
        firebaseAuth.getCurrentUser().getIdToken(true)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                    @Override
                    public void onSuccess(GetTokenResult getTokenResult) {

                    }
                });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginRegisterActivity.class);
        startActivity(intent);
        finish();
    }

//    onStart method gets called everytime the Activity is started
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

//    onStop method is called upon navigation to other activity by user
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onPaymentSuccess(String s) {
        makeUserPremium();
    }

    private void makeUserPremium() {
        Map<String , Object> mp = new HashMap<>();
        mp.put("isPremium", true);
        db.collection("Users")
                .document(user.getUid())
                .update(mp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        openDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void openDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Recipeify")
                .setMessage("Congrats on becoming a part of Recipeify family!\nApp restart required for upgrading changes")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Thank You!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        alert.setCancelable(false);
        alert.show();

    }


    //This method will display the error message if there is any with the transaction
    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(MainActivity.this, "payment failed due to :\n"+s, Toast.LENGTH_LONG).show();
    }

    private void checkForDynamicLinks() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
//                Toast.makeText(MainActivity.this, "We have a dynamic link", Toast.LENGTH_SHORT).show();
                Uri deepLink = null;
                if(pendingDynamicLinkData != null)
                {
                    deepLink = pendingDynamicLinkData.getLink();
                    String recipeLink = deepLink.toString();
                    Toast.makeText(MainActivity.this, "Dynamic link : "+recipeLink, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, RecipeDisplay.class);
                    intent.putExtra("recipeLink", recipeLink);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Oops! We didn't recieve any dynamic links\n"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




}
