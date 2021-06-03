package com.example.fui.Fragments;

public class Model {

    int image;
    String recipeCategory;
    int recipeCount;

    public Model(int image, String recipeCategory, int recipeCount) {
        this.image = image;
        this.recipeCategory = recipeCategory;
        this.recipeCount = recipeCount;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getRecipeCategory() {
        return recipeCategory;
    }

    public void setRecipeCategory(String recipeCategory) {
        this.recipeCategory = recipeCategory;
    }

    public int getRecipeCount() {
        return recipeCount;
    }

    public void setRecipeCount(int recipeCount) {
        this.recipeCount = recipeCount;
    }
}
