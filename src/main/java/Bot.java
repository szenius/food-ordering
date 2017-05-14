import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

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
        } catch (TelegramApiException e) {
            LOGGER.error("Could not send message");
        }
    }

    public String getBotUsername() {
        return "DinoBot";
    }

    public String getBotToken() {
        return "274950550:AAGxFzmpkZykZs-wkkYGQocwk05qgOve1VM";
    }

    // Deciphers commands
    public void respond(Update update) throws TelegramApiException {
        Message message = update.getMessage();

        handler.execute(message);
        String textResponse = handler.getResponse();

        if (textResponse != null) {
            SendMessage result = new SendMessage();
            result.setText(textResponse);
            result.setChatId(message.getChatId());
            result.setParseMode("Markdown");
            sendMessage(result);
        } else {
            sendVoice(handler.getAudioOrder());
        }
    }
}
