import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author SzeYing
 * @since 2017-05-13
 */
public class Bot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    public Bot(){}

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                LOGGER.info("Got message from {}", update.getMessage().getChatId());

                SendMessage result = new SendMessage();
                result.setText("Hello!");
                result.setChatId(update.getMessage().getChatId());
                sendMessage(result);
            }
        } catch (TelegramApiException e) {
            LOGGER.error("Could not send message");
        }
    }

    public String getBotUsername() {
        return "FoodOrderingBot";
    }

    public String getBotToken() {
        return "274950550:AAGxFzmpkZykZs-wkkYGQocwk05qgOve1VM";
    }
}
