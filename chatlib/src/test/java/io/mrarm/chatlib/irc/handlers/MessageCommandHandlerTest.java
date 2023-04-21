package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.irc.InvalidMessageException;
import io.mrarm.chatlib.irc.MessagePrefix;
import io.mrarm.chatlib.irc.ServerConnectionData;
import io.mrarm.chatlib.irc.dcc.DCCClientManager;
import io.mrarm.chatlib.irc.dcc.DCCServerManager;
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
        handler.setDCCServerManager(new DCCServerManager());
        handler.setDCCClientManager(new DCCClientManager() {
            @Override
            public void onFileOffered(ServerConnectionData connection, MessagePrefix sender, String filename, String address, int port, long fileSize) {
            }

            @Override
            public void onFileOfferedUsingReverse(ServerConnectionData connection, MessagePrefix sender, String filename, long fileSize, int uploadId) {
            }
        });
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
        testMessageForCrash("test\20z");
        testMessageForCrash("\20");
    }

    @Test
    void handleInvalidDccResume() {
        testMessageForCrash("\01DCC RESUME \01");
        testMessageForCrash("\01DCC RESUME test\01");
        testMessageForCrash("\01DCC RESUME test \01");
        testMessageForCrash("\01DCC RESUME test test\01");
        testMessageForCrash("\01DCC RESUME test test \01");
        testMessageForCrash("\01DCC RESUME test test test\01");
        testMessageForCrash("\01DCC RESUME    \01");
        testMessageForCrash("\01DCC RESUME test -1 -1\01");
        testMessageForCrash("\01DCC RESUME test 10000000000 0\01");
        testMessageForCrash("\01DCC RESUME test 0 9223372036854775808\01");
    }

    @Test
    void handleInvalidDtcSend() {
        testMessageForCrash("\01DCC SEND \01");
        testMessageForCrash("\01DCC SEND test\01");
        testMessageForCrash("\01DCC SEND test \01");
        testMessageForCrash("\01DCC SEND test test\01");
        testMessageForCrash("\01DCC SEND test test \01");
        testMessageForCrash("\01DCC SEND test test test\01");
        testMessageForCrash("\01DCC SEND test test test \01");
        testMessageForCrash("\01DCC SEND test test test test\01");
        testMessageForCrash("\01DCC SEND     \01");
        testMessageForCrash("\01DCC SEND test -1 -1 -1\01");
        testMessageForCrash("\01DCC SEND test 10000000000 1 0\01");
        testMessageForCrash("\01DCC SEND test 1 10000000000 0\01");
        testMessageForCrash("\01DCC SEND test 1 1 9223372036854775808\01");
        testMessageForCrash("\01DCC SEND test 0 0 1\01");
        testMessageForCrash("\01DCC SEND test 0 0 1 9223372036854775808\01");
    }

}
