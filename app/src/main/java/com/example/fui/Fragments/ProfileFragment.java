package com.example.fui.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.fui.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends DialogFragment implements View.OnClickListener {

    private String KEY_NAME = "name", KEY_CONTACT = "contact", KEY_URL = "image_url", KEY_ACCTYPE = "isPremium";
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    private Uri imguri;
    private String image_link;

    private Button btnSignOut, btnClose, getPremium;
    private TextView txtName, txtContact, txtAccType;
    private String url;
    private String UID;
    private ImageView profileimage;
    private ImageButton btnAdd, btnRemove;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userRef;
    private StorageReference mStorageRef;

    ProgressDialog pd;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading");
        pd.setTitle("Making user premium");
        pd.setCancelable(false);
        setParameters();
//       saveToFirestore();
        btnSignOut.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        getPremium.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        return view;
    }

    private void init(View view) {
        btnSignOut = view.findViewById(R.id.btnSignOut);
        btnClose = view.findViewById(R.id.btnClose);
        getPremium = view.findViewById(R.id.getPremium);
        txtName = view.findViewById(R.id.txtName);
        txtContact = view.findViewById(R.id.txtContact);
        txtAccType = view.findViewById(R.id.txtAccType);
        profileimage = view.findViewById(R.id.profileImage);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnRemove = view.findViewById(R.id.btnRemove);
        mStorageRef = FirebaseStorage.getInstance().getReference("Users");
    }

    private void setParameters()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            UID = user.getUid();
        }
        userRef = db.document("Users/"+UID);
        //reading values from Firebase Firestore
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            txtName.setText(documentSnapshot.getString(KEY_NAME));
                            txtContact.setText(documentSnapshot.getString(KEY_CONTACT));
                            url = documentSnapshot.getString(KEY_URL);
                            boolean flag = documentSnapshot.getBoolean(KEY_ACCTYPE);
                            if(flag)
                            {
                                txtAccType.setText("PREMIUM");
                            }else
                            {
                                txtAccType.setText("NON-PREMIUM");
                            }
                            if(url != null)
                            {
                                Glide.with(getContext()).load(url).into(profileimage);
                            }


                        }else{
                            Toast.makeText(getContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error occurred\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }



    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnAdd:
//                Toast.makeText(getContext(), "Add clicked!", Toast.LENGTH_LONG).show();
                file_chooser();
                break;

            case R.id.btnRemove:
//                Toast.makeText(getContext(), "Remove Clicked", Toast.LENGTH_LONG).show();
                deleteProfilePic();
                break;

            case R.id.getPremium:
//                Toast.makeText(getContext(), "Premium Clicked", Toast.LENGTH_LONG).show();
                if (txtAccType.getText().toString().equals("PREMIUM"))
                {
                    Toast.makeText(getContext(), "You are already a PREMIUM user!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                else{
                    dismiss();
                    openPaymentFragment();
                }


                break;

            case R.id.btnClose:
                dismiss();
                break;

            case R.id.btnSignOut:
                Toast.makeText(getContext(), "Sign out clicked", Toast.LENGTH_LONG).show();
                AuthUI.getInstance().signOut(getContext());
                break;


        }
    }

    private void openPaymentFragment() {
        PaymentFragment pf = new PaymentFragment();
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_right_exit).replace(R.id.fragment_container, pf).addToBackStack(null).commit();
    }


    private void deleteProfilePic() {
        Map<String , Object> mp = new HashMap<>();
        mp.put(KEY_URL, null);

        db.collection("Users").document(UID)
                .update(mp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        profileimage.setImageResource(R.drawable.ic_add_a_photo);
                        deleteFromStorage(url);
                        url = null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Action failed :\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteFromStorage(String url) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        if(url!=null) {
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);
            storageReference.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Picture Deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Failed to delete image\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }else{
            Toast.makeText(getContext(), "url is empty/null", Toast.LENGTH_LONG).show();
        }
    }

    private void file_chooser()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
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
                    imguri = data.getData();
                    profileimage.setImageURI(imguri);
                    pd.show();
                    uploader();
                }
                break;


        }
    }


    private String getExtension(Uri uri)
    {
        ContentResolver cr = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploader() {
        final StorageReference filePath = mStorageRef.child(UID+"."+getExtension(imguri));
        if(filePath != null)
        {
            filePath.putFile(imguri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                //Here we are uploading our image to database and then we get the link for uploaded image
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Uri downUri = task.getResult();
                        image_link = downUri.toString();
                        Toast.makeText(getContext(), "image_link: "+image_link, Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    updateFirestoreData();
                }
            });
        }

    }

    private void updateFirestoreData() {
        Map<String , Object> mp = new HashMap<>();
        mp.put(KEY_URL, image_link);

        db.collection("Users").document(UID)
                .update(mp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Updated firestore image link", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed due to\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0]>PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }else{
                    Toast.makeText(getContext(), "Permission Denied!\nPlease try again!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
