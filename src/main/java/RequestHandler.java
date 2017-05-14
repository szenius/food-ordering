import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.User;

import java.util.*;

/**
 * @author SzeYing
 * @since 2017-05-13
 */
public class RequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private static final String MESSAGE_COMMAND_ERROR = "Sorry, I didn't understand that! Could you try again?";

    private static List<Order> orders; // TODO: change to List<Order>

    private static Menu menuList = new Menu();

    public RequestHandler() {
        orders = new ArrayList<>();
    }

    public String execute(Message message) {
        List<MessageEntity> entities = message.getEntities();
        Command command = parseCommand(entities.get(0));

        if (command == null) {
            return getCommandErrorMessage();
        }

        String text = removeFirstWord(message.getText());
        User user = message.getFrom();
        String url = removeFirstWord(message.getText());

        switch (command) {
            case ADD:
                return addOrder(text, user);
            case CLEAR:
                return clearOrders();
            case VIEW:
                return viewOrders();
            case COLLATE:
                return collateOrders();
            case MENU:
                return loadMenu(url);
            case SPLIT:
                return splitOrdersByUser();
            default:
                return getCommandErrorMessage();
        }
    }

    // Add an order
    public String addOrder(String text, User user) {
        LOGGER.info("We are trying to add this order: {}", text);
        String[] tokens = text.trim().split(", ");
        Integer userId = user.getId();
        String userName = user.getFirstName();
        String foodName = tokens[0];
        double price = Double.parseDouble(tokens[1]);

        orders.add(new Order(userId, userName, foodName, price));
        return "We added your order " + text + " by " + user.getFirstName();
    }

    // Clear all orders
    public String clearOrders() {
        LOGGER.info("Clearing orders.");
        orders.clear();
        return "Cleared your orders!";
    }

    // View all orders, no grouping
    public String viewOrders() {
        if (orders.isEmpty()) {
            return "You have no orders! Try adding one with '/add'!";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("These are your orders so far:\n\n");
        for (Order order : orders) {
            builder.append(order.getViewString());
            builder.append('\n');
        }
        return builder.toString();
    }

    // Collate orders by item
    public String collateOrders() {
        Map<String, Integer> result = new HashMap<>();

        // Load orders into map (collated)
        for (Order order : orders) {

            if (result.containsKey(order.getCollateString())) {
                // Already exists in map

                Integer count = result.get(order.getCollateString()) + 1;
                result.replace(order.getCollateString(), count);
            } else {
                // Seeing this order for the first time

                result.put(order.getCollateString(), 1);
            }

        }

        // Load collated orders into String
        StringBuilder builder = new StringBuilder();
        builder.append("Collated your orders!\n\n");

        for (String key : result.keySet()) {
            builder.append(key + " x" + result.get(key));
            builder.append('\n');
        }

        return builder.toString();
    }

    // Split orders by user (bill splitting)
    public String splitOrdersByUser() {
        Map<String, List<Order>> result = new HashMap<>();

        // Load orders into Map
        for (Order order : orders) {
            if (result.containsKey(order.getUserName())) {
                // Already exists

                result.get(order.getUserName()).add(order);
            } else {
                // Found first order by this user

                result.put(order.getUserName(), new ArrayList<>(Arrays.asList(order)));
            }
        }

        // Load result into String
        StringBuilder builder = new StringBuilder();
        builder.append("Split your bill by user:\n\n");

        for (String userName : result.keySet()) {
            builder.append(userName + " ordered:\n");
            List<Order> ordersByUser = result.get(userName);
            double totalPayable = 0;

            for (Order order : ordersByUser) {
                builder.append(order.getCollateString());
                builder.append('\n');

                totalPayable += order.getPrice();
            }

            builder.append("Total = " + String.valueOf(totalPayable));
            builder.append('\n');
            builder.append('\n');
        }

        return builder.toString();
    }

    // View Menu
    public String loadMenu(String url) {
        LOGGER.info("Loading Menu: {}", url);
        menuList.loadMenu();
        String[] tokens = url.trim().split("\\.");
        String restaurant = tokens[1];
        return restaurant + " menu has been loaded.";
    }

    public String removeFirstWord(String str) {
        return str.substring(str.indexOf(" ") + 1);
    }

    public Command parseCommand(MessageEntity command) {
        String cmdString = command.getText().substring(1);
        LOGGER.info("Parsing command: {}", cmdString);

        switch (cmdString) {
            case "add":
                return Command.ADD;
            case "clear":
                return Command.CLEAR;
            case "view":
                return Command.VIEW;
            case "collate":
                return Command.COLLATE;
            case "split":
                return Command.SPLIT;
            case "order":
                return Command.ORDER;
            case "menu":
                return Command.MENU;
            default:
                return null;
        }
    }

    public String getCommandErrorMessage() {
        return MESSAGE_COMMAND_ERROR;
    }

    public enum Command {
        ADD,
        CLEAR,
        VIEW,
        COLLATE,
        SPLIT,
        ORDER,
        MENU
    }
}
