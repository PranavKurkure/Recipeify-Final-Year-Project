package com.example.fui.Model;

import java.util.List;

public class Recipe {
    private String createdOn, dish_name, image_url, recipe_category;
    private List<String>  ingredients, recipe_text;
    private int cook_time;
    private int servings;
    private boolean food_type;
    private String description;
    private int count;
    private float rating, sum;
    private String author;


    public Recipe(){
            //empty constructor required for building the layout
    }

    public Recipe(int cook_time, String createdOn, String dish_name, String image_url, List<String> recipe_text, List<String> ingredients, String recipe_category,
    int servings, boolean food_type, String description, String author) {
        this.cook_time = cook_time;
        this.createdOn = createdOn;
        this.dish_name = dish_name;
        this.image_url = image_url;
        this.recipe_text = recipe_text;
        this.ingredients = ingredients;
        this.recipe_category = recipe_category;
        this.servings = servings;
        this.food_type = food_type;
        this.description = description;
        this.author = author;
    }
    public int getCook_time() {
        return cook_time;
    }
    public String getCreatedOn() {
        return createdOn;
    }
    public String getDish_name() {
        return dish_name;
    }
    public String getImage_url() {
        return image_url;
    }
    public List<String> getRecipe_text() {
        return recipe_text;
    }
    public List<String> getIngredients(){ return ingredients; }
    public String getRecipe_category(){ return recipe_category; }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public boolean isFood_type() {
        return food_type;
    }

    public void setFood_type(boolean food_type) {
        this.food_type = food_type;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setDish_name(String dish_name) {
        this.dish_name = dish_name;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setRecipe_text(List<String> recipe_text) {
        this.recipe_text = recipe_text;
    }

    public void setRecipe_category(String recipe_category) {
        this.recipe_category = recipe_category;
    }


    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setCook_time(int cook_time) {
        this.cook_time = cook_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
        this.sum = sum;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
