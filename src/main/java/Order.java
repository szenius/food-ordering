/**
 * @author zhengyu
 * @since 2017-05-13
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

    public Integer getUserId() {
        return userId;
    }

    public String getUserName() {
        return username;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    // For view command
    public String getViewString() {
        return getCollateString() + " by " + getUserName();
    }

    // For collate command
    public String getCollateString() {
        return getName() + ", $" + String.valueOf(getPrice());
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