package com.example.fui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fui.Model.Recipe;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.protobuf.Int32Value;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeDisplay extends AppCompatActivity implements View.OnClickListener, RatingDialog.RatingDialogListener{

    private int STORAGE_PERMISSION_CODE=1;

    private String dish_name, description, image_url, recipe_category, author;
    private List<String> ingredients, recipe_text;
    private int cooktime, servings;
    private float rating;

    private String DISH_NAME="dish_name", DESCRIPTION="description", RECIPE_TEXT="recipe_text", RATING="rating";
    private String INGREDIENTS="ingredients", IMAGE_URL="image_url", FOOD_TYPE="food_type", COOK_TIME="cook_time";
    private String CATEGORY="recipe_category", AUTHOR="author", SERVINGS="servings";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private DocumentReference docref;
    ProgressDialog pd;

    CheckBox checkBox;
    TableRow row;
    Boolean clicked = false;
    ImageView image;
    TextView desc;
    boolean f=true;
    FloatingActionButton fab1, fab2, fab3, btnFav;

    //flag==true means the recipe is favorite
    //flag==false means the recipe is not favorite
    boolean flag;
    Button openSteps;
    TableLayout tableLayout;

    Chip chip, chip_rating, cook_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_display);

        init();
        Intent intent = getIntent();

        if(!isConnected(this))
        {
            showCustomDialog();
        }
        if(checkForDynamicLinks())
        {
            showProgress("Downloading data");
            extractParameters();
            checkIfFavorite();
        }
        else {
            dish_name = intent.getStringExtra(DISH_NAME);
            description = intent.getStringExtra(DESCRIPTION);
            ingredients = intent.getStringArrayListExtra(INGREDIENTS);
            image_url = intent.getStringExtra(IMAGE_URL);
            f = intent.getBooleanExtra(FOOD_TYPE, true);
            cooktime = intent.getIntExtra(COOK_TIME, 0);
            recipe_text = intent.getStringArrayListExtra(RECIPE_TEXT);
            recipe_category = intent.getStringExtra(CATEGORY);
            rating = intent.getFloatExtra(RATING, 0.0f);
            author = intent.getStringExtra(AUTHOR);
            servings = intent.getIntExtra(SERVINGS, 2);
            checkIfFavorite();
            setParameters(dish_name, description, ingredients, image_url, f, cooktime, recipe_text, recipe_category, rating);
        }


        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);
        openSteps.setOnClickListener(this);
        btnFav.setOnClickListener(this);


    }

    private void setParameters(String dish_name, String description, List<String> ingredients, String image_url,
                               boolean f, int cooktime, List<String> recipe_text, String recipe_category, float rating) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).  setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(dish_name);
        if(f)
        {
            chip.setChipIcon(getResources().getDrawable(R.drawable.ic_veg));
            chip.setText("Veg");
        }else{
            chip.setChipIcon(getResources().getDrawable(R.drawable.ic_non_veg));
            chip.setText("Non-Veg");
        }
        DecimalFormat numberFormat = new DecimalFormat("#.0");
        String tt = "Rating : "+numberFormat.format(rating);
        chip_rating.setText(tt);
        desc.setText(description);
        Glide.with(this).load(image_url).into(image);
        String r = "Time : "+ cooktime + "mins";
        cook_time.setText(r);

        //Setting up the ingredients in a tablelayout with help of CheckBox
        int flag=1;
        for(int i = 0; i< this.ingredients.size(); i++)
        {
            String text="";
            checkBox = new CheckBox(this);
            checkBox.setId(i);
            text = ingredients.get(i);
            checkBox.setText(text);
            checkBox.setPadding(0,0,0,1 );
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    Toast.makeText(RecipeDisplay.this, "Checkbox :"+isChecked, Toast.LENGTH_LONG).show();
                }
            });

                row = new TableRow(this);
                row.addView(checkBox);
                tableLayout.addView(row, new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
            }


        closeProgress();
        }






    private void init() {
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab3 = findViewById(R.id.floating_action_button);
        openSteps = findViewById(R.id.openSteps);
        chip = findViewById(R.id.chipFoodType);
        cook_time = findViewById(R.id.cook_time);
        chip_rating = findViewById(R.id.rating);
        image = findViewById(R.id.image);
        desc = findViewById(R.id.description);
        tableLayout = findViewById(R.id.tableLayout);
        tableLayout.setColumnStretchable(0, true);
        tableLayout.setColumnStretchable(1, true);
        pd = new ProgressDialog(RecipeDisplay.this);
        btnFav = findViewById(R.id.btnFav);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            docref = db.collection("Users").document(user.getUid());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.share) {
            shareLongDynamicLink();
            showProgress("Generating link for recipe");
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.fab1:
                openDialog();
                break;

            case R.id.fab2:
                if(ContextCompat.checkSelfPermission(RecipeDisplay.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    try {
                        showProgress("Generating PDF");
                        createPDF();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    requestStoragePermission();
                }

                break;

            case R.id.floating_action_button:
                onAddButtonClicked();
                break;

            case R.id.openSteps:
                Intent i = new Intent(RecipeDisplay.this, StepsActivity.class);
                i.putStringArrayListExtra(RECIPE_TEXT, (ArrayList<String>) recipe_text);
                startActivity(i);
                break;

            case R.id.btnFav:

                if(flag == true) {
                    addToFavorites();
//                    Toast.makeText(this, "Add to Favorites clicked", Toast.LENGTH_SHORT).show();
                }else {
                    removeFromFavorites();
//                    Toast.makeText(this, "Remove from Favorites clicked", Toast.LENGTH_SHORT).show();
                }

        }

    }

    private void requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is required to save PDF on your phone")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(RecipeDisplay.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();


        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission GRANTED!", Toast.LENGTH_SHORT).show();
                try {
                    showProgress("Generating PDF");
                    createPDF();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(this, "Permission DENIED!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openDialog() {
        RatingDialog ratingDialog = new RatingDialog(dish_name);
        ratingDialog.show(getSupportFragmentManager(), "rating dialog");
    }

    public void applyRating(final float rating)
    {
        showProgress("Updating rating");
        final DocumentReference docref = db.collection(recipe_category).document(dish_name);
        db.runTransaction(new Transaction.Function<Float>() {

            @Nullable
            @Override
            public Float apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Recipe recipe = transaction.get(docref).toObject(Recipe.class);
                int C = recipe.getCount()+1;
                transaction.update(docref, "count", C);
                float S = recipe.getSum()+rating;
                transaction.update(docref, "sum",S);
                float result = S/C;
                transaction.update(docref, "rating",result);
                return result;
            }
        })
        .addOnSuccessListener(new OnSuccessListener<Float>() {
            @Override
            public void onSuccess(Float result) {
                Toast.makeText(RecipeDisplay.this, "Updated", Toast.LENGTH_SHORT).show();
                DecimalFormat numberFormat = new DecimalFormat("#.0");
//                rating.setText(numberFormat.format(rate));
                String text = "Rating : "+numberFormat.format(result);
                chip_rating.setText(text);
                closeProgress();

            }
        });
    }

    private void onAddButtonClicked() {

        setVisibility(clicked);
        setAnimation(clicked);
        clicked = !clicked;
    }

    private void setAnimation(Boolean choice) {
        if(!choice)
        {

            fab1.setVisibility(View.VISIBLE);
            fab2.setVisibility(View.VISIBLE);
        }
        else{
            fab1.setVisibility(View.GONE);
            fab2.setVisibility(View.GONE);
        }
    }

    private void setVisibility(Boolean choice) {
        if(!choice)
        {
            fab1.startAnimation(AnimationUtils.loadAnimation(RecipeDisplay.this, R.anim.from_bottom_anim));
            fab2.startAnimation(AnimationUtils.loadAnimation(RecipeDisplay.this, R.anim.from_bottom_anim));
            fab3.startAnimation(AnimationUtils.loadAnimation(RecipeDisplay.this, R.anim.rotate_open_anim));
        }
        else{

            fab1.startAnimation(AnimationUtils.loadAnimation(RecipeDisplay.this, R.anim.to_bottom_anim));
            fab2.startAnimation(AnimationUtils.loadAnimation(RecipeDisplay.this, R.anim.to_bottom_anim));
            fab3.startAnimation(AnimationUtils.loadAnimation(RecipeDisplay.this, R.anim.rotate_close_anim));

        }
    }

    private void shareLongDynamicLink()
    {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://covidstatsnfighter.herokuapp.com/"))
                .setDomainUriPrefix("https://recipeifyfui.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();

        String shareLinkText = "https://recipeifyfui.page.link/?"
                +"link=https://covidstatsnfighter.herokuapp.com/?dishname="+dish_name+"-"+recipe_category
                +"&apn=com.example.fui"
                +"&st=Recipeify"
                +"&sd=The best platform for recipes"
                +"&si="+image_url;

        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(Uri.parse(shareLinkText))
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            closeProgress();

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(intent);
                        } else {
                            // Error
                            // ...
                            Toast.makeText(RecipeDisplay.this, "Error occurred in shortLink Task line 265", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private boolean checkForDynamicLinks()
    {
        boolean flag = false;
        String recipeLink = getIntent().getExtras().getString("recipeLink");
        if(recipeLink != null)
        { flag = true; }
        return flag;
    }

    private void extractParameters()
    {
        String recipeLink = getIntent().getExtras().getString("recipeLink");
        recipeLink = recipeLink.substring(recipeLink.lastIndexOf("="));
        String dishname = recipeLink.substring(0, recipeLink.lastIndexOf("-"));
        dishname = dishname.replace("=","");
        dishname = dishname.replace("+"," ");
        String RC = recipeLink.substring(recipeLink.indexOf("-")+1);
        String DNAME = dishname;

        DocumentReference docref = db.collection(RC).document(DNAME);
        docref.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        dish_name = recipe.getDish_name();
                        description = recipe.getDescription();
                        image_url = recipe.getImage_url();
                        ingredients = recipe.getIngredients();
                        recipe_text = recipe.getRecipe_text();
                        cooktime = recipe.getCook_time();
                        rating = recipe.getRating();
                        f = recipe.isFood_type();
                        author = recipe.getAuthor();
                        servings = recipe.getServings();
                        setParameters(dish_name, description, ingredients, image_url, f, cooktime, recipe_text, RC, rating);
//                        closeProgress();


                    }
                });
    }


    private void createPDF() throws FileNotFoundException {
        //The below statment will save the generated PDF into the Downloads folder of mobile
        //Environment.DIRECTORY_DOCUMENTS will save a pdf in documents location
        String pdfPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recipeify/";

        //File class is used to specify the name that should be given to the PDF with .pdf extension
//        File file = new File(pdfPath, filename.getText().toString());
        //OutputStream outputStream = new FileOutputStream(file);

        File file = new File(pdfPath);
        if(!file.exists())
        {
            Toast.makeText(this, "Directory does not exist", Toast.LENGTH_SHORT).show();
        }


        if(!file.mkdirs())
        {
            file.mkdirs();
        }

        file = new File(pdfPath, dish_name+".pdf");


        Drawable d1 = getDrawable(R.drawable.recipeify);
        Bitmap bitmap1 = ((BitmapDrawable)d1).getBitmap();
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream1);
        byte[] bitmapData1 = stream1.toByteArray();

        ImageData imageData1 = ImageDataFactory.create(bitmapData1);
        Image image1 = new Image(imageData1);
        image1.setWidth(140f);




        DeviceRgb orange = new DeviceRgb(255,165,0);
        DeviceRgb red = new DeviceRgb(255,0,0);
        DeviceRgb green = new DeviceRgb(0,128,0);
        DeviceRgb blue = new DeviceRgb(30,178,242);





        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

//        pdfWriter.setCompressionLevel(0);




        float[] columnWidth ={140,140,140,140};
        Table table1 = new Table(columnWidth);

        //        table1 01
        table1.addCell(new Cell(1,1).add(new Paragraph("RECIPEIFY").setFontSize(26f).setFontColor(orange).setTextAlignment(TextAlignment.CENTER)).setVerticalAlignment(VerticalAlignment.BOTTOM).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell(1,1).add(image1).setBorder(Border.NO_BORDER));

        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));



        //        table1 02
        table1.addCell(new Cell().add(new Paragraph(dish_name).setFontSize(20f)).setVerticalAlignment(VerticalAlignment.BOTTOM).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        if(f){
            table1.addCell(new Cell().add(new Paragraph("Veg").setFontColor(green).setBold().setUnderline()).setVerticalAlignment(VerticalAlignment.BOTTOM).setBorder(Border.NO_BORDER));
        }else{
            table1.addCell(new Cell().add(new Paragraph("Non-Veg").setFontColor(red).setBold().setUnderline()).setVerticalAlignment(VerticalAlignment.BOTTOM).setBorder(Border.NO_BORDER));
        }


        //        table1 03
        table1.addCell(new Cell().add(new Paragraph("No. of servings :")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph(String.valueOf(servings))).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("Rating :").setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph(String.valueOf(rating))).setBorder(Border.NO_BORDER));

        //        table1 04
        table1.addCell(new Cell().add(new Paragraph("Cook time :")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph(cooktime +"mins")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("Author :").setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph(author)).setBorder(Border.NO_BORDER));

//        Drawable d2 = getDrawable(R.drawable.dal_makhani);
        Drawable d2 = image.getDrawable();
        Bitmap bitmap2 = ((BitmapDrawable)d2).getBitmap();
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
        byte[] bitmapData2 = stream2.toByteArray();



        ImageData imageData2 = ImageDataFactory.create(bitmapData2);
        Image image2 = new Image(imageData2);
        image2.setFixedPosition(110, 150);
        image2.setHeight(390f);
        image2.setWidth(390f);







        float[] columnWidth2 = {140,420};
        Table table2 = new Table(columnWidth2);

        //Table2 01
        table2.addCell(new Cell().add(new Paragraph("Description")));
        table2.addCell(new Cell().add(new Paragraph(description)));

        //Table2 02
        table2.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));

        //Table2 03
        table2.addCell(new Cell().add(new Paragraph("Ingredients")));
        String temp="";
        int count;
        for(int i=0;i<ingredients.size();i++)
        {
            count = i+1;
            temp = temp+count+" "+ingredients.get(i)+"\n";
        }
        table2.addCell(new Cell().add(new Paragraph(temp)));

        //Table2 04
        table2.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));


        float[] columnWidth3 = {100, 460};
        Table table3 = new Table(columnWidth3);
        table3.addCell(new Cell(1,2).add(new Paragraph("Procedure").setTextAlignment(TextAlignment.CENTER)));


        for(int i=0;i<recipe_text.size();i++)
        {
                int tmp = i+1;
                table3.addCell(new Cell().add(new Paragraph("Step "+tmp).setTextAlignment(TextAlignment.CENTER)));
                table3.addCell(new Cell().add(new Paragraph(recipe_text.get(i))));
        }


        SolidLine line = new SolidLine(2f);
        line.setColor(orange);
        LineSeparator ls = new LineSeparator(line);

        Rectangle rect = new Rectangle(0,0);
        PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
        // Setting action of the annotation
        PdfAction action = PdfAction.createURI("https://www.google.com/");
        annotation.setAction(action);
        // Creating a link
        Link link = new Link("Click here", annotation);
        link.setFontColor(blue);
        Paragraph para = new Paragraph("Download our app from this website :");
        para.add(link.setUnderline()).setVerticalAlignment(VerticalAlignment.BOTTOM)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);


        document.add(table1);
        document.add(ls);
        ls.setVerticalAlignment(VerticalAlignment.BOTTOM);
        document.add(image2);
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        document.add(table2);
        document.add(ls);
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        document.add(table3);
        document.add(para);
        document.setBackgroundColor(new DeviceRgb(255,255,232));


        document.close();
        closeProgress();
        Toast.makeText(this, "Document created\nPlease check "+Environment.getExternalStorageDirectory().getAbsolutePath()+"/Recipeify"+" folder", Toast.LENGTH_LONG).show();
        closeProgress();



    }


    private void showProgress(String message)
    {
        pd.setTitle("Please Wait...");
        pd.setMessage(message+"...");
        pd.setCancelable(false);
        pd.show();
    }
    private void closeProgress()
    {
        pd.dismiss();
    }

    private void checkIfFavorite()
    {
        showProgress("Checking");
        docref.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> map = (Map<String, Object>)documentSnapshot.get("Favorites");
                        if(!map.isEmpty())
                        {
                            for(Map.Entry<String, Object> entry : map.entrySet())
                            {
                                if(entry.getKey().equalsIgnoreCase(recipe_category))
                                {
                                    List<String> list = (List<String>) entry.getValue();
                                    if(list.size() == 0)
                                    {
                                        Toast.makeText(RecipeDisplay.this, "No favorite present", Toast.LENGTH_SHORT).show();
                                    }else{
                                        for(int i=0;i<list.size();i++)
                                        {
                                            if(list.get(i).equalsIgnoreCase(dish_name))
                                            {
                                                flag = false;
                                                btnFav.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite));
                                            }
                                            else{
                                                flag = true;
                                                btnFav.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_border));
                                            }
                                            closeProgress();
                                        }
                                    }

                                }
                            }
                        }


                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecipeDisplay.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFavorite()
    {
        btnFav.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite));
    }

    private void removeFavorite()
    {
        btnFav.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_border));
    }


    List<String> tempAdd;
    private void addToFavorites()
    {
        showProgress("Adding");
        DocumentReference documentReference = db.collection("Users").document(user.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> tempMap = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) transaction.get(documentReference).get("Favorites");
                for(Map.Entry<String, Object> entry : map.entrySet())
                {
                    if(!entry.getKey().equalsIgnoreCase(recipe_category))
                    {
                        tempMap.put(entry.getKey(), entry.getValue());
                    }else{
                        List<String> list = (List<String>) entry.getValue();
                        list.add(dish_name);
                        tempMap.put(entry.getKey(), list);
                    }
                }
                transaction.update(documentReference, "Favorites", tempMap);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RecipeDisplay.this, "Values updated", Toast.LENGTH_SHORT).show();
                btnFav.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite));
                flag = false;
                closeProgress();
            }
        });
    }

    List<String> tempList;
    private void removeFromFavorites()
    {
        showProgress("Removing");
        DocumentReference documentReference = db.collection("Users").document(user.getUid());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> tempMap = new HashMap<>();
                Map<String, Object> map = (Map<String, Object>) transaction.get(documentReference).get("Favorites");
                for(Map.Entry<String, Object> entry : map.entrySet())
                {
                    if(!entry.getKey().equalsIgnoreCase(recipe_category))
                    {
                        tempMap.put(entry.getKey(), entry.getValue());
                    }
                    else{
                        tempList = new ArrayList<>();
                        List<String> list = (List<String>) entry.getValue();
                        for(int i=0;i<list.size();i++)
                        {
                            if(!list.get(i).equalsIgnoreCase(dish_name))
                            {
                                tempList.add(list.get(i));
                            }
                        }
                        tempMap.put(entry.getKey(), tempList);
                    }
                }
                transaction.update(documentReference, "Favorites", tempMap);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RecipeDisplay.this, "values updated", Toast.LENGTH_SHORT).show();
                btnFav.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_border));
                flag = true;
                closeProgress();
            }
        });
    }

    private boolean isConnected(RecipeDisplay mainActivity) {
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

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecipeDisplay.this);
        builder.setMessage("Please connect to internet to proceed further")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        AlertDialog.Builder bld = new AlertDialog.Builder(RecipeDisplay.this);
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
                        Toast.makeText(RecipeDisplay.this, "Sorry but you cannot access content without any network", Toast.LENGTH_LONG).show();
                    }
                })
                .setIcon(R.drawable.ic_signal_wifi_off);

        builder.show();
    }

}
