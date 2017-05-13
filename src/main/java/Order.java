/**
 * Created by zhengyu on 5/13/17.
 */

public class Order {
    private int userId;
    private String username, name;
    private double price;

    public Order(Integer userId, String userName, String name, double price) {
        this.userId = userId;
        this.username = userName;
        this.name = name;
        this.price = price;
    }

    public Integer getUserid() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getViewString() {
        return getName() + ", $" + String.valueOf(getPrice()) + " by " + getUsername();
    }

    public void setUserid(Integer newUserid){
        this.userId = newUserid;
    }

    public void setUsername(String newUsername){
        this.username = newUsername;
    }

    public void setName(String newName){
        this.name = newName;
    }

    public void setPrice(double newPrice){
        this.price = newPrice;
    }
}