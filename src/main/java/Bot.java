import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;

/**
 * @author SzeYing
 * @since 2017-05-13
 */
public class Bot extends TelegramLongPollingBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);

    private static RequestHandler handler;

    public Bot() {
        handler = new RequestHandler();
    }

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasEntities() && update.getMessage().hasText()) {
                respond(update);
            }

//            else if (update.hasChannelPost() && update.getChannelPost().hasText()) {
//                respond(update.getChannelPost().getText(), update.getChannelPost().getChatId());
//            } else if (update.hasMessage() && update.getMessage().hasText()) {
//                respond(update.getMessage().getText(), update.getMessage().getChatId());
//            }
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

    // Deciphers commands
    public void respond(Update update) throws TelegramApiException {
        SendMessage result = new SendMessage();
        Message message = update.getMessage();

        String response = handler.execute(message);
        result.setText(response);
        result.setChatId(message.getChatId());
        sendMessage(result);
    }
}
