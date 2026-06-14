import database.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class SignInScreen extends JFrame {

    private static final Font TITLE_FONT  = new Font("SansSerif", Font.BOLD, 22);
    private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font LABEL_FONT  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    private final UserDAO userDAO = new UserDAO();

    private JTextField userIdField;
    private JTextField nameField;
    private JLabel     errorLabel;

    public SignInScreen() {
        setTitle("Happy Mart – Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 520);
        setMinimumSize(new Dimension(400, 460));
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        add(buildCard());
        setVisible(true);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1),
                new EmptyBorder(28, 32, 32, 32)
        ));
        card.setPreferredSize(new Dimension(340, 390));
        card.setMaximumSize(new Dimension(340, 390));

        // ── Logo row ──
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        logoRow.setBackground(Color.WHITE);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(2, 2, 34, 34);
                super.paintComponent(g);
            }
        };
        logo.setPreferredSize(new Dimension(38, 38));

        JLabel appTitle = new JLabel("HAPPY MART");
        appTitle.setFont(TITLE_FONT);
        appTitle.setForeground(Color.BLACK);

        logoRow.add(logo);
        logoRow.add(appTitle);

        // ── Sign In heading ──
        JLabel heading = new JLabel("Sign In");
        heading.setFont(HEADING_FONT);
        heading.setForeground(Color.BLACK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Fields ──
        userIdField = createField("Enter your user ID");
        nameField   = createField("Enter your name");

        // ── Error label ──
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        errorLabel.setForeground(Color.RED);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Sign In button ──
        JButton signInBtn = createButton("Sign In");
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.addActionListener(e -> attemptSignIn());

        // Allow Enter key to trigger sign-in from any field
        ActionListener enterAction = e -> attemptSignIn();
        userIdField.addActionListener(enterAction);
        nameField.addActionListener(enterAction);

        // ── Assemble ──
        card.add(logoRow);
        card.add(Box.createVerticalStrut(18));
        card.add(sep());
        card.add(Box.createVerticalStrut(18));
        card.add(heading);
        card.add(Box.createVerticalStrut(20));
        card.add(userIdField);
        card.add(Box.createVerticalStrut(10));
        card.add(nameField);
        card.add(Box.createVerticalStrut(6));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(signInBtn);

        return card;
    }

    private JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(Color.LIGHT_GRAY);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }



    private JTextField createField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(LABEL_FONT);
        tf.setForeground(Color.GRAY);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(Color.BLACK, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);

        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(Color.GRAY);
                }
            }
        });
        return tf;
    }

    private String getFieldValue(JTextField tf, String placeholder) {
        String v = tf.getText().trim();
        return v.equals(placeholder) ? "" : v;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? Color.DARK_GRAY : Color.BLACK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                super.paintComponent(g);
            }
        };
        btn.setFont(BUTTON_FONT);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setPreferredSize(new Dimension(276, 38));
        return btn;
    }

    // TODO
    private void attemptSignIn() {

        errorLabel.setText(" ");
        String userId = getFieldValue(userIdField, "Enter your user id");
        String name = getFieldValue(nameField, "Enter your name");

        if (userId.isEmpty() || name.isEmpty()) {
            errorLabel.setText("Please input the user id and name");
            return;
        }

        User user = userDAO.findUser(userId, name);
        System.out.println(user);
        if (user == null) {
            errorLabel.setText("Invalid user id or name");
            return;
        }

        errorLabel.setText(" ");
        dispose();
        if ("cashier".equalsIgnoreCase(user.getRole())) {
//            errorLabel.setText("Access denied!! Admin accounts only!");
//            return;
            new OrderScreen(user);
        } else {
            new HappyMartApp();
        }



    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SignInScreen::new);
    }
}
