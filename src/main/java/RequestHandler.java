import com.sun.xml.internal.ws.util.StringUtils;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.User;

import java.io.*;
import java.util.*;

/**
 * @author SzeYing
 * @since 2017-05-13
 */
public class RequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private static final String MESSAGE_COMMAND_ERROR = "Sorry, I didn't understand that! Could you try again?";
    private static final String MESSAGE_TTS_ERROR = "We could not generate your orders in an audio file...";
    private static final String MESSAGE_NO_ORDERS = "You have no orders! Try adding one with '/add'!";

    private static List<Order> orders; // TODO: change to List<Order>

    private static Menu menuList = new Menu();
    private String response;
    private SendVoice audioOrder;

    private static final Map<String, Menu> menuMap = new HashMap<>();

    public RequestHandler() {
        orders = new ArrayList<>();
        response = getCommandErrorMessage();
        audioOrder = new SendVoice();

    }

    public void execute(Message message) {
        List<MessageEntity> entities = message.getEntities();
        Command command = parseCommand(entities.get(0));



        if (command == null) {
            setResponse(getCommandErrorMessage());
        }

        String text = removeFirstWord(message.getText());
        User user = message.getFrom();
        String url = removeFirstWord(message.getText());

        String result = null;

        switch (command) {
            case ADD:
                result = addOrder(text, user);
                break;
            case CLEAR:
                result = clearOrders();
                break;
            case VIEW:
                result = viewOrders();
                break;
            case COLLATE:
                result = collateOrders();
                break;
            case MENU:
                result = loadMenu(url);
            case SPLIT:
                result = splitOrdersByUser();
                break;
            case ORDER:
                SendVoice audioMessage = ttsOrder(message.getChatId());
                setAudio(audioMessage);
                break;
            default:
                result = getCommandErrorMessage();
        }

        setResponse(result);
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setAudio(SendVoice audio) {
        this.audioOrder = audio;
    }

    public SendVoice getAudioOrder() {
        return audioOrder;
    }

    // Add an order
    private String addOrder(String text, User user) {
        LOGGER.info("We are trying to add this order: {}", text);
        Integer userId = user.getId();
        String userName = user.getFirstName();
        String foodName = text;

        orders.add(new Order(userId, userName, StringUtils.capitalize(foodName)));

        /** Build response **/
        StringBuilder builder = new StringBuilder();

        // Notification that order was added
        builder.append("`" + user.getFirstName() + "` added 1 " + text + "!\n");
        builder.append("\n");

        // Load current orders
        Map<String, List<Order>> ordersByUser = loadItemsByUser();
        // For each user
        for (String username : ordersByUser.keySet()) {
            builder.append(username + " ordered:\n");

            // For each unique item ordered by user
            Map<String, Integer> ordersByItem = loadOrdersByItem(ordersByUser.get(username));
            for (String orderName : ordersByItem.keySet()) {
                int numOrders = ordersByItem.get(orderName);
                builder.append(buildItemString(orderName, numOrders));
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    // Clear all orders
    private String clearOrders() {
        LOGGER.info("Clearing orders.");
        orders.clear();
        return "Cleared your orders!";
    }

    // View all orders, no grouping
    private String viewOrders() {
        if (orders.isEmpty()) {
            return getNoOrdersMessage();
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
    private String collateOrders() {
        if (orders.isEmpty()) {
            return getNoOrdersMessage();
        }
        Map<String, Integer> result = loadOrdersByItem(orders);

        // Load collated orders into String
        StringBuilder builder = new StringBuilder();
        builder.append("Collated your orders!\n\n");

        for (String key : result.keySet()) {
            builder.append(buildItemString(key, result.get(key)));
        }

        return builder.toString();
    }

    // Load into map for collate and TTS
    private Map<String, Integer> loadOrdersByItem(List<Order> orders) {
        Map<String, Integer> result = new HashMap<>();
        // Load orders into map (collated)
        for (Order order : orders) {
            if (result.containsKey(order.getName())) {
                // Already exists in map

                Integer count = result.get(order.getName()) + 1;
                result.replace(order.getName(), count);
            } else {
                // Seeing this order for the first time

                result.put(order.getName(), 1);
            }
        }
        return result;
    }

    // Split orders by user (bill splitting)
    private String splitOrdersByUser() {
        Map<String, List<Order>> result = loadItemsByUser();

        // Load result into String
        StringBuilder builder = new StringBuilder();
        builder.append("Split your bill by user:\n\n");

        for (String userName : result.keySet()) {
            builder.append(userName + " ordered:\n");
            List<Order> ordersByUser = result.get(userName);
            double totalPayable = 0;

            for (Order order : ordersByUser) {
                builder.append(order.getName());
                builder.append('\n');

                // TODO: totalPayable requires menu price
//                totalPayable += order.getPrice();
            }

            builder.append("Total = " + String.valueOf(totalPayable));
            builder.append('\n');
            builder.append('\n');
        }

        return builder.toString();
    }

    private Map<String, List<Order>> loadItemsByUser() {
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

        return result;
    }

    // View Menu
    public String loadMenu(String url) {
        LOGGER.info("Loading Menu: {}", url);

        String key = "http://www.madenicenyc.com/menu/";
//        List<Menu> menu = new List<Menu>() {
//        }
//
//        if(menuMap.get(key)) {
//
//        }

        menuList.loadMenu();
        String[] tokens = url.trim().split("\\.");
        String restaurant = tokens[1];
        return restaurant + " menu has been loaded.";
    }

    // Text to speech ordering!!!!!!
    private SendVoice ttsOrder(Long chatId) {
        CloseableHttpResponse response = makeTtsRequest();

        SendVoice audioOrder = new SendVoice();
        try {
            audioOrder.setNewVoice("Orders", response.getEntity().getContent());
        } catch (IOException ioe) {
            LOGGER.error("Error attaching audio file", ioe);
        }
        audioOrder.setChatId(chatId);
        return audioOrder;
    }

    private String convertOrdersToJson() {
        LOGGER.info("Converting {} to audio", collateOrders());
        Map<String, Integer> collated = loadOrdersByItem(orders);
        StringBuilder builder = new StringBuilder();
        builder.append("Hi, I would like to place a delivery order.\n");
        builder.append("Can I have ");
        for (String key : collated.keySet()) {
            builder.append(collated.get(key));
            builder.append(" " + key);
            builder.append("\n");
        }
        builder.append("Thank you!\n");

        return "{\"text\":\"" + builder.toString().replaceAll("\n", "<break/>") + "\"}";
    }

    private CloseableHttpResponse makeTtsRequest() {
        String url = "https://stream.watsonplatform.net/text-to-speech/api/v1/synthesize";

        // Create an instance of HttpClient.
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        try {
            UsernamePasswordCredentials credentials =
                    new UsernamePasswordCredentials("de758945-6764-4ead-847e-ab0a4c5b1625", "nsCpFNOj2AsC");
            httpPost.addHeader(new BasicScheme().authenticate(credentials, httpPost, null));
        } catch (AuthenticationException ae) {
            LOGGER.error("Could not authenticate", ae);
        }

        try {
            httpPost.setEntity(new StringEntity(convertOrdersToJson()));
        } catch (UnsupportedEncodingException uee) {
            LOGGER.error("Could not set entity for HTTP Post", uee);
        }
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "audio/wav");

        try {
            // Execute the method.
            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() != 200) {
                LOGGER.error("Method failed: {}", response.getStatusLine().getStatusCode());
            }

            // Read the response body.
            return response;
        } catch (IOException ioe) {
            LOGGER.error("Could not write output file", ioe);
        }

        return null;
    }

    private static String buildItemString(String orderName, int numOrders) {
        if (numOrders > 1 && !orderName.endsWith("s")) {
            orderName += 's';
        }

        return numOrders + " x " + orderName + "\n";
    }

    private static String removeFirstWord(String str) {
        return str.substring(str.indexOf(" ") + 1);
    }

    private static Command parseCommand(MessageEntity command) {
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

    private static String getCommandErrorMessage() {
        return MESSAGE_COMMAND_ERROR;
    }

    private static String getTtsErrorMessage() {
        return MESSAGE_TTS_ERROR;
    }

    private static String getNoOrdersMessage() {
        return MESSAGE_NO_ORDERS;
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
