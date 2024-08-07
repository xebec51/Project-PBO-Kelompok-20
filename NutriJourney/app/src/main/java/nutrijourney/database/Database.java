package nutrijourney.database;

import nutrijourney.models.Food;
import nutrijourney.models.Fruit;
import nutrijourney.models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static Connection conn;

    public static synchronized void initialize() {
        try {
            if (conn == null || conn.isClosed()) {
                // Load SQLite JDBC Driver
                Class.forName("org.sqlite.JDBC");

                // Create connection to SQLite database
                conn = DriverManager.getConnection("jdbc:sqlite:nutrijourney.db");
                System.out.println("Database connection established.");

                // Create tables if not exists
                try (Statement stmt = conn.createStatement()) {
                    String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                            "username TEXT PRIMARY KEY, " +
                            "password TEXT NOT NULL, " +
                            "full_name TEXT, " +
                            "age INTEGER, " +
                            "weight REAL, " +
                            "height REAL, " +
                            "gender TEXT, " +
                            "activity_level REAL)";
                    stmt.execute(userTable);

                    String foodLogTable = "CREATE TABLE IF NOT EXISTS food_log (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "username TEXT, " +
                            "food_name TEXT, " +
                            "calories REAL, " +
                            "protein REAL, " +
                            "fat REAL, " +
                            "carbs REAL, " +
                            "water REAL, " + // Tambahkan kolom untuk jumlah air
                            "consumption_date TEXT, " +
                            "consumption_day TEXT, " +
                            "FOREIGN KEY (username) REFERENCES users(username))";
                    stmt.execute(foodLogTable);

                    // Check if 'water' column exists in 'foods' table, add if not exists
                    ResultSet rs = stmt.executeQuery("PRAGMA table_info(foods);");
                    boolean waterColumnExists = false;
                    while (rs.next()) {
                        if ("water".equalsIgnoreCase(rs.getString("name"))) {
                            waterColumnExists = true;
                            break;
                        }
                    }
                    if (!waterColumnExists) {
                        stmt.execute("ALTER TABLE foods ADD COLUMN water REAL;");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("SQLite JDBC Driver not found.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Connection to database failed.");
        }
    }

    public static synchronized Connection getConnection() {
        if (conn == null) {
            initialize();
        }
        try {
            if (conn == null || conn.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static synchronized void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized User getUser(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement("SELECT * FROM users WHERE username = ?")) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getInt("age"),
                    rs.getDouble("weight"),
                    rs.getDouble("height"),
                    rs.getString("gender"),
                    rs.getDouble("activity_level")
                );
                user.setFoodLog(getFoodLog(username));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement("INSERT INTO users (username, password, full_name, age, weight, height, gender, activity_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setInt(4, user.getAge());
            pstmt.setDouble(5, user.getWeight());
            pstmt.setDouble(6, user.getHeight());
            pstmt.setString(7, user.getGender());
            pstmt.setDouble(8, user.getActivityLevel());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void logFood(String username, Food food, String date, String day) {
        if (username == null || username.isEmpty() || food == null) {
            throw new IllegalArgumentException("Username and food cannot be null or empty");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement("INSERT INTO food_log (username, food_name, calories, protein, fat, carbs, water, consumption_date, consumption_day) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, username);
            pstmt.setString(2, food.getName());
            pstmt.setDouble(3, food.getCalories());
            pstmt.setDouble(4, food.getProtein());
            pstmt.setDouble(5, food.getFat());
            pstmt.setDouble(6, food.getCarbs());
            pstmt.setDouble(7, food.getWater()); // Menyimpan jumlah air
            pstmt.setString(8, date);
            pstmt.setString(9, day);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void updateUserWeight(String username, double newWeight) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement("UPDATE users SET weight = ? WHERE username = ?")) {
            pstmt.setDouble(1, newWeight);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized List<Food> getFoodLog(String username) {
        List<Food> foodLog = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement("SELECT * FROM food_log WHERE username = ?")) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Food food = new Fruit(
                    rs.getString("food_name"),
                    rs.getDouble("calories"),
                    rs.getDouble("protein"),
                    rs.getDouble("fat"),
                    rs.getDouble("carbs"),
                    rs.getDouble("water")
                );
                food.setDate(rs.getString("consumption_date"));
                food.setDay(rs.getString("consumption_day"));
                foodLog.add(food);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foodLog;
    }

    public static synchronized List<Food> getAllFoods() {
        List<Food> foods = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM foods")) {
            
            while (rs.next()) {
                Food food = new Fruit(
                    rs.getString("name"),
                    rs.getDouble("calories"),
                    rs.getDouble("protein"),
                    rs.getDouble("fat"),
                    rs.getDouble("carbs"),
                    rs.getDouble("water")
                );
                foods.add(food);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foods;
    }

    // Tambahkan metode getAllLoggedFoods
    public static synchronized List<Food> getAllLoggedFoods(String username) {
        List<Food> foodLog = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        String query = "SELECT food_name, calories, protein, fat, carbs, water, consumption_date, consumption_day FROM food_log WHERE username = ? ORDER BY consumption_date, consumption_day";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Gunakan kelas konkret untuk menginstansiasi objek Food
                Food food = new Fruit(
                    rs.getString("food_name"),
                    rs.getDouble("calories"),
                    rs.getDouble("protein"),
                    rs.getDouble("fat"),
                    rs.getDouble("carbs"),
                    rs.getDouble("water")
                );
                food.setDate(rs.getString("consumption_date"));
                food.setDay(rs.getString("consumption_day"));
                foodLog.add(food);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foodLog;
    }

    public static synchronized void updateFoodLog(String username, Food food, String date, String day) {
        if (username == null || username.isEmpty() || food == null) {
            throw new IllegalArgumentException("Username and food cannot be null or empty");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "UPDATE food_log SET food_name = ?, calories = ?, protein = ?, fat = ?, carbs = ?, water = ? WHERE username = ? AND consumption_date = ? AND consumption_day = ?")) {
            pstmt.setString(1, food.getName());
            pstmt.setDouble(2, food.getCalories());
            pstmt.setDouble(3, food.getProtein());
            pstmt.setDouble(4, food.getFat());
            pstmt.setDouble(5, food.getCarbs());
            pstmt.setDouble(6, food.getWater());
            pstmt.setString(7, username);
            pstmt.setString(8, date);
            pstmt.setString(9, day);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void deleteFoodLog(String username, String foodName, String date, String day) {
        if (username == null || username.isEmpty() || foodName == null || foodName.isEmpty() || date == null || date.isEmpty() || day == null || day.isEmpty()) {
            throw new IllegalArgumentException("Parameters cannot be null or empty");
        }
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "DELETE FROM food_log WHERE username = ? AND food_name = ? AND consumption_date = ? AND consumption_day = ?")) {
            pstmt.setString(1, username);
            pstmt.setString(2, foodName);
            pstmt.setString(3, date);
            pstmt.setString(4, day);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
