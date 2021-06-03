package com.example.fui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeUpload extends AppCompatActivity implements View.OnClickListener {

    AutoCompleteTextView autoCompleteText, foodType;
    TextInputEditText dish_name, servings, cook_time, description;
    TextInputLayout tl4, tl1, tl5, ftype, tl2, tl3, tl6;
    NachoTextView ingredients;
    Button btnFile, btnDelete;
    Bitmap photoBmp;
    ImageView image;
    LinearLayout layoutList;
    ImageButton btnAdd;
    List<String> stepsList;
    FirebaseUser user;
    public Uri imguri = null;
    StorageReference mStorageRef;
    String image_link;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ProgressDialog pd;

    private String DISH_NAME="dish_name", DESCRIPTION="description", RECIPE_TEXT="recipe_text", RATING="rating", DISH_NAME_SMALL="dish_name_small";
    private String INGREDIENTS="ingredients", IMAGE_URL="image_url", FOOD_TYPE="food_type", COOK_TIME="cook_time";
    private String CATEGORY="recipe_category", AUTHOR="author", SERVINGS="servings",CREATED_ON="createdOn";

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int MIC_PERMISSION = 10;

    private int serves = 1;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_upload);
        init();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.option_item, getResources().getStringArray(R.array.category));
        autoCompleteText.setAdapter(arrayAdapter);

        String[] option = {"Veg", "Non-Veg"};
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(this, R.layout.option_item, option);
        foodType.setAdapter(arrayAdapter1);



        servings.setText(String.valueOf(serves));
        tl4.setEndIconOnClickListener(view -> removeServe());
        tl4.setStartIconOnClickListener(view -> addServe());
        tl5.setEndIconOnClickListener(view -> getSpeechInput());

        addChipTerminators();
        btnDelete.setOnClickListener(this);
        btnFile.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            if(checkAndValidRead())
            {
                uploader();
            }
            else{
                Toast.makeText(this, "Missing fields", Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);

    }




    private void addChipTerminators() {
        ingredients.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        ingredients.enableEditChipOnTouch(false,false);
    }

    private void init()
    {
        autoCompleteText = findViewById(R.id.autoCompleteText);
        dish_name = findViewById(R.id.dish_name);
        servings = findViewById(R.id.servings);
        cook_time = findViewById(R.id.cook_time);
        description = findViewById(R.id.description);
        ingredients = findViewById(R.id.ingredients);
        tl4 = findViewById(R.id.tl4);
        tl1 = findViewById(R.id.tl1);
        tl5 = findViewById(R.id.tl5);
        tl2 = findViewById(R.id.tl2);
        tl3 = findViewById(R.id.tl3);
        tl6 = findViewById(R.id.tl6);
        ftype = findViewById(R.id.ftype);
        btnDelete = findViewById(R.id.btnDelete);
        btnFile = findViewById(R.id.btnFile);
        image = findViewById(R.id.dish_image);
        foodType = findViewById(R.id.foodType);
        layoutList = findViewById(R.id.layout_list);
        btnAdd = findViewById(R.id.btnAdd);
        user = FirebaseAuth.getInstance().getCurrentUser();
        stepsList = new ArrayList<>();
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        pd = new ProgressDialog(this);
    }

    private void getSpeechInput()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, 10);
        }else{
            Toast.makeText(RecipeUpload.this, "Device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }



    private void addServe()
    {
        serves++;
        servings.setText(String.valueOf(serves));
        tl4.setError(null);

    }
    private void removeServe()
    {
        if(serves <= 1)
        {
            tl4.setError("Servings cannot be 0!");
        }else{
            serves--;
            servings.setText(String.valueOf(serves));
            tl4.setError(null);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btnFile:
                fileChooser();
                break;

            case R.id.btnDelete:
                restoreImage();
                break;

            case R.id.btnAdd:
                addView();
                break;

        }
    }



    private void fileChooser()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            }else{
                pickImageFromGallery();
            }
        }else{
            pickImageFromGallery();
        }
    }

    private void pickImageFromGallery()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {

            case IMAGE_PICK_CODE:
                if(resultCode == RESULT_OK && data!=null && data.getData()!=null)
                {
                    Uri mImageCaptureUri = data.getData();
                    imguri = mImageCaptureUri;

                    if (mImageCaptureUri != null) {
                        try {
                            photoBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                            image.setImageBitmap(photoBmp);
                            btnDelete.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case MIC_PERMISSION:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    assert result != null;
                    description.setText(result.get(0));
                }
                break;

        }
    }

    private void restoreImage()
    {
        String uri = "@drawable/add_photo";
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        image.setImageDrawable(res);
        btnDelete.setVisibility(View.GONE);
        imguri = null;
    }

    private void addView()
    {
        final View stepView = getLayoutInflater().inflate(R.layout.row_add_step, null, false);
        TextInputEditText step_name = stepView.findViewById(R.id.step_name);
        step_name.setHint("Procedure step");
        TextInputLayout tli = stepView.findViewById(R.id.TIL);
        tli.setEndIconOnClickListener(view -> removeView(stepView));


        layoutList.addView(stepView);

    }

    private void removeView(View v)
    {
        layoutList.removeView(v);
    }


    public String getExtension(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    public void uploader()
    {
        showProgress("Uploading");
        final StorageReference filePath = mStorageRef.child(System.currentTimeMillis()+"."+getExtension(imguri));
        if(filePath != null){
            filePath.putFile(imguri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();
                        image_link = downUri.toString();
                        saveDataToFirestore();

                    }
                }
            });
        }
        // updateFirebaseData();

    }

    private void saveDataToFirestore()
    {
        stepsList.clear();
        for(int i=0;i<layoutList.getChildCount();i++)
        {
            View view = layoutList.getChildAt(i);
            TextInputEditText step_procedure = view.findViewById(R.id.step_name);
            String temp = step_procedure.getText().toString().trim();
            stepsList.add(temp);
        }
        if(stepsList.size() == 0)
        {
            Toast.makeText(this, "Please enter any steps", Toast.LENGTH_SHORT).show();
        }
        String DISH_NAME = dish_name.getText().toString().trim();
        boolean FOOD_TYPE = true;
        FOOD_TYPE = foodType.getText().toString().equalsIgnoreCase("veg");
        String RECIPE_CATEGORY = autoCompleteText.getText().toString();
        int COOK_TIME = Integer.parseInt(cook_time.getText().toString().trim());
        int SERVINGS = Integer.parseInt(servings.getText().toString().trim());
        String DESCRIPTION = description.getText().toString().trim();
        List<String> ingr = ingredients.getChipValues();
        String CREATED_ON = new Date().toString();
        String AUTHOR_NAME = user.getDisplayName();
        List<String> RECIPE_TEXT = stepsList;
        String IMAGE_URL=image_link;

        Map<String, Object> map = new HashMap<>();
        map.put(this.DISH_NAME, DISH_NAME);
        map.put(DISH_NAME_SMALL, DISH_NAME.toLowerCase());
        map.put(this.FOOD_TYPE, FOOD_TYPE);
        map.put(this.COOK_TIME, COOK_TIME);
        map.put(this.SERVINGS, SERVINGS);
        map.put(this.DESCRIPTION, DESCRIPTION);
        map.put(this.INGREDIENTS, ingr);
        map.put(this.CREATED_ON, CREATED_ON);
        map.put(this.AUTHOR, AUTHOR_NAME);
        map.put(this.RECIPE_TEXT, RECIPE_TEXT);
        map.put(this.IMAGE_URL, IMAGE_URL);

        firestore.collection(RECIPE_CATEGORY).document(DISH_NAME)
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(RecipeUpload.this, "Values uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(RecipeUpload.this, "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void showProgress(String message)
    {
        pd.setTitle("Please Wait!");
        pd.setMessage(message+"...");
        pd.show();
    }
    private void closeProgress()
    {
        pd.dismiss();
    }

    boolean res = true;
    private boolean checkAndValidRead()
    {


        //---------DISH NAME-------------------------------------------------//
        if(dish_name.getText().toString().trim().length() > 25)
        {
            tl1.setError("Dish name too long");
            res = false;
        }else if(dish_name.getText().toString().trim().length()==0 || dish_name.getText().toString().trim().equalsIgnoreCase(""))
        {
            tl1.setError("Dish name cannot be empty!");
            res = false;
        }
        else{
            tl1.setError(null);
        }
        //------------DISH NAME END------------------------------------------------//



        if(cook_time.getText().toString().trim().length()==0 || cook_time==null)
        {
            tl3.setError("Enter cook time!");
            res = false;
        }
        else if(cook_time.getText().toString().trim().length() > 3)
        {
            tl3.setError("Too big!");
            res = false;
        }
        else{
            tl3.setError(null);
        }

        if(description.getText().toString().trim().length() == 0 || description.getText().toString().equalsIgnoreCase(""))
        {
            tl5.setError("Please put a short description");
            res = false;
        }else{
            tl5.setError(null);
        }

        List<String> temp = ingredients.getChipValues();
        if(temp.size()==0)
        {
            tl6.setError("Put some ingredients!");
            res = false;
        }else{
            tl6.setError(null);
        }

        if(imguri == null)
        {
            Toast.makeText(this, "Please a put a nice image", Toast.LENGTH_SHORT).show();
            res = false;
        }



        return res;
    }

}
