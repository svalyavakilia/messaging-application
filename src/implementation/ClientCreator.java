import static javax.swing.SwingUtilities.invokeLater;

class ClientCreator {
    public static void main(final String... clientCreatorArguments) {
        invokeLater(
            () -> {
                new Client("theFirstChatUser", "groupChat");
                new Client("theSecondChatUser", "groupChat");
                //new Client("theThirdChatUser", "groupChat");
            }
        );
    }
}