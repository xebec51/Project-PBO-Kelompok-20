package nutrijourney.models;

public abstract class Food { // Abstraksi
    private String name; // Enkapsulasi
    private double calories;
    private double protein;
    private double fat;
    private double carbs;
    private double water; // Jumlah air dalam ml
    private String date;
    private String day;

    public Food(String name, double calories, double protein, double fat, double carbs) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.water = 0;
    }

    public Food(String name, double calories, double protein, double fat, double carbs, double water) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.water = water;
    }

    public String getName() {
        return name;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getWater() {
        return water;
    }

    public void setWater(double water) {
        this.water = water;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public abstract void displayInfo(); // Polimorfisme
}
