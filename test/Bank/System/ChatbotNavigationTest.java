package Bank.System;

import Bank.Bank;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ChatbotNavigationTest {
    @Test
    void menusExposeChatAsTheirLastOption() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("4\n7\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output));
            Screen screen = new Screen();
            assertEquals(4, screen.loginChoicesPage(new Bank("Test")));
            assertEquals(7, screen.customerDashBoardPage(new Bank("Test")));
            String menus = output.toString();
            assertTrue(menus.indexOf("4. Chat with assistant") > menus.indexOf("3. Open an account"));
            assertTrue(menus.indexOf("7. Chat with assistant") > menus.indexOf("6. Accounts and Cards"));
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    @Test
    void clearBlankAndBackReturnToEachOriginWithoutCallingOllama() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("\nclear\nback\nback\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output));
            AppSystem app = new AppSystem("Test");
            app.loadChatbotPage(Screen.Page.LOGINCHOICES);
            assertEquals(Screen.Page.LOGINCHOICES, app.getCurrentPage());
            app.loadChatbotPage(Screen.Page.CUSTOMERDASHBOARD);
            assertEquals(Screen.Page.CUSTOMERDASHBOARD, app.getCurrentPage());
            // End-of-input also exits instead of crashing or sending requests forever.
            app.loadChatbotPage(Screen.Page.LOGINCHOICES);
            assertEquals(Screen.Page.LOGINCHOICES, app.getCurrentPage());
            assertTrue(output.toString().contains("Conversation cleared"));
            assertFalse(output.toString().contains("Assistant is thinking"));
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}
