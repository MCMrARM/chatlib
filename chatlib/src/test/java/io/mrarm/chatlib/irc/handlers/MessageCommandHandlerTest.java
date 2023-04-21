package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.test.TestApiImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MessageCommandHandlerTest {

    private void testMessageForCrash(String msg) {
        TestApiImpl testApi = new TestApiImpl("tester");
        MessageCommandHandler handler = new MessageCommandHandler();
        MessagePrefix sender = new MessagePrefix("sender!user@localhost");
        try {
            handler.handle(testApi.getServerConnectionData(), sender, "PRIVMSG", Arrays.asList(
                    "#test",
                    msg
            ), new HashMap<>());
        } catch (InvalidMessageException ignored) {
            // ignored
        }
    }

    @Test
    void handleInvalidDequote() {
        testMessageForCrash("test\20");
    }

}
