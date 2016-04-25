package boeken.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Show a dialogue for ithe nput of password for the MySQL boeken account,
 * which gives access to schema boeken.
 *
 * See: https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
 * and: https://docs.oracle.com/javase/7/docs/api/javax/swing/JOptionPane.html
 * and: http://stackoverflow.com/questions/8881213/joptionpane-to-get-password
 *
 * Created by cvengelen on 25-04-16.
 */
public class PasswordPanel extends JPanel {
    private final JPasswordField passwordField = new JPasswordField(12);
    private JOptionPane passwordOptionPane;
    private JDialog passwordDialog;
    private boolean focusRequested;

    public PasswordPanel() {
        super(new FlowLayout());

        add(new JLabel("Password: "));
        add(passwordField);

        passwordOptionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        passwordDialog = passwordOptionPane.createDialog("Boeken database");

        // Use a WindowAdapter object to create a listener for when the dialogue has gained focus
        passwordDialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                // Let the password panel know that the dialogue has gained focus
                gainedFocus();
            }
        });

        // Start the password dialogue
        passwordDialog.setVisible( true );
    }

    // Hook method called when the password dialog gained focus
    private void gainedFocus() {
        if (!focusRequested) {
            focusRequested = true;

            // Set the focus on the password field
            passwordField.requestFocusInWindow();
        }
    }

    public String getPassword() {
        // Dispose of all resources used by the password dialogue
        passwordDialog.dispose();

        // Check the Cancel option
        if (passwordOptionPane.getValue() == null || passwordOptionPane.getValue().equals(JOptionPane.CANCEL_OPTION)) {
            return null;
        }

        return new String(passwordField.getPassword());
    }
}
