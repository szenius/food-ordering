import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengyu
 * @since 5/13/17
 */
public class Menu {
    private static List<MenuItem> menuList = new ArrayList<>();

    public List<MenuItem> getMenuList(List<MenuItem> menuList) {
        return menuList;
    }

    public void setMenuList(List<MenuItem> menuList) {
        this.menuList = menuList;
    }

    public void loadMenu() {
        MenuItem food1 = new MenuItem("Spring Chicken", 12);
        MenuItem food2 = new MenuItem("Salmon Rosti", 15);
        MenuItem food3 = new MenuItem("Curry Cauliflower", 11);


        menuList.add(food1);
        menuList.add(food2);
        menuList.add(food3);
    }
}
