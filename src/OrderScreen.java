import database.OrderDAO;
import database.ProductDAO;
import model.OrderItem;
import model.Product;
import model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderScreen extends JFrame {

    // ─── DAO ──────
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO   orderDAO   = new OrderDAO();

    // ─── State ────
    private final User currentUser;
    private final List<OrderItem> cartItems = new ArrayList<>();
    private int selectedRow = -1;

    // ─── Form components ───
    private JTextField codeField;
    private JTextField amountField;
    private JLabel     statusLabel;
    private JButton    addBtn;
    private JButton    deleteBtn;
    private JButton    updateBtn;
    private JButton    clearBtn;
    private JButton    signOutBtn;
    private JButton    orderBtn;

    // ─── Table ─────
    private JTable           orderTable;
    private DefaultTableModel tableModel;
    private JLabel           totalLabel;

    // ─── Fonts ──────
    private static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD, 22);
    private static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 20);
    private static final Font LABEL_FONT   = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font BUTTON_FONT  = new Font("SansSerif", Font.BOLD, 13);
    private static final Font TABLE_FONT   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font HEADER_FONT  = new Font("SansSerif", Font.BOLD, 13);
    private static final Font TOTAL_FONT   = new Font("SansSerif", Font.BOLD, 15);

    // ─── Decimal format ────
    private static final DecimalFormat PRICE_FMT = new DecimalFormat("#,##0.00");

    public OrderScreen(User user) {
        this.currentUser = user;

        setTitle("Happy Mart – Order");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setMinimumSize(new Dimension(880, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);

        setVisible(true);
    }

    //  HEADER
    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

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

        JLabel title = new JLabel("HAPPY MART");
        title.setFont(TITLE_FONT);
        title.setForeground(Color.BLACK);

        header.add(logo);
        header.add(title);
        return header;
    }

    //  MAIN PANEL  (left form + right table)
    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.insets  = new Insets(0, 0, 0, 12);
        gbc.gridy   = 0;
        gbc.weighty = 1.0;

        // Left form
        gbc.gridx   = 0;
        gbc.weightx = 0.32;
        main.add(buildFormPanel(), gbc);

        // Right table
        gbc.gridx   = 1;
        gbc.weightx = 0.68;
        gbc.insets  = new Insets(0, 0, 0, 0);
        main.add(buildTablePanel(), gbc);

        return main;
    }

    //  FORM PANEL (left side)
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // ── Heading ──
        JLabel heading = new JLabel("Product");
        heading.setFont(HEADING_FONT);
        heading.setForeground(Color.BLACK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Fields ──
        codeField   = createField("Enter product code");
        amountField = createField("Enter the amount");

        // ── Status label ──
        statusLabel = new JLabel(" ");
        statusLabel.setFont(LABEL_FONT);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ── Add button ──
        addBtn = createButton("Add", true);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(e -> addToOrder());

        // ── Bottom action row ──
        deleteBtn  = createSmallButton("Delete");
        updateBtn  = createSmallButton("Update");
        clearBtn   = createSmallButton("Clear");
        signOutBtn = createSmallButton("Signout");
        deleteBtn.addActionListener(e -> deleteSelectedItem());
        updateBtn.addActionListener(e -> updateSelectedItem());
        clearBtn.addActionListener(e -> clearSelectionAndFields());
        signOutBtn.addActionListener(e -> signOut());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionRow.setBackground(Color.WHITE);
        actionRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        actionRow.add(deleteBtn);
        actionRow.add(updateBtn);
        actionRow.add(clearBtn);
        actionRow.add(signOutBtn);

        // ── Order button ──
        orderBtn = createButton("Order", true);
        orderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        orderBtn.addActionListener(e -> placeOrder());

        // ── Assemble ──
        panel.add(heading);
        panel.add(Box.createVerticalStrut(16));
        panel.add(codeField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(amountField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(addBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(actionRow);
        panel.add(Box.createVerticalStrut(16));
        panel.add(orderBtn);

        return panel;
    }

    //  TABLE PANEL (right side)
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("All products for this order");
        heading.setFont(LABEL_FONT);
        heading.setForeground(Color.DARK_GRAY);

        String[] cols = {"Code", "Thumbnail", "Name", "Amount", "Unit Price", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setFont(TABLE_FONT);
        orderTable.setRowHeight(70);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setGridColor(Color.LIGHT_GRAY);
        orderTable.setShowGrid(true);
        orderTable.setBackground(Color.WHITE);
        orderTable.setSelectionBackground(new Color(220, 220, 220));
        orderTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = orderTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));

        int[] widths = {70, 90, 180, 70, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            orderTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer centreRenderer = new DefaultTableCellRenderer();
        centreRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 3, 4, 5}) {
            orderTable.getColumnModel().getColumn(i).setCellRenderer(centreRenderer);
        }

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        JScrollPane scroll = new JScrollPane(orderTable);
        scroll.setBorder(new LineBorder(Color.BLACK, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        // ── Total row ──
        totalLabel = new JLabel("Total: " + PRICE_FMT.format(0.0) + "$");
        totalLabel.setFont(TOTAL_FONT);
        totalLabel.setForeground(Color.BLACK);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setBorder(new EmptyBorder(8, 0, 0, 4));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        return panel;
    }

    //  HELPERS – widget factories
    private JTextField createField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(LABEL_FONT);
        tf.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY),
                new EmptyBorder(4, 8, 4, 8)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setForeground(Color.GRAY);
        tf.setText(placeholder);

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

    private void setFieldValue(JTextField tf, String placeholder, String value) {
        if (value == null || value.isEmpty()) {
            tf.setText(placeholder);
            tf.setForeground(Color.GRAY);
        } else {
            tf.setText(value);
            tf.setForeground(Color.BLACK);
        }
    }

    private JButton createButton(String text, boolean filled) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (filled) {
                    g2.setColor(getModel().isPressed() ? Color.DARK_GRAY : Color.BLACK);
                } else {
                    g2.setColor(getModel().isPressed() ? new Color(220,220,220) : Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                super.paintComponent(g);
            }
        };
        btn.setFont(BUTTON_FONT);
        btn.setForeground(filled ? Color.WHITE : Color.BLACK);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        return btn;
    }

    private JButton createSmallButton(String text) {
        JButton btn = createButton(text, false);
        btn.setMaximumSize(new Dimension(90, 32));
        btn.setPreferredSize(new Dimension(80, 32));
        return btn;
    }

    private ImageIcon makeThumbnailIcon(byte[] data) {
        if (data == null) return null;
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            if (img == null) return null;
            Image scaled = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException e) {
            return null;
        }
    }

    //  CART / ORDER LOGIC
    // TODO
    private void addToOrder() {

    }

    private OrderItem findCartItem(String code) {



        return null;
    }

    private void updateSelectedItem() {

    }

    private void deleteSelectedItem() {

    }

    private void placeOrder() {

    }

    private double computeTotal() {
        double total = 0;



        return total;
    }

    private void onRowSelected() {

    }

    private void clearSelectionAndFields() {
        selectedRow = -1;
        orderTable.clearSelection();
        setFieldValue(codeField, "Enter product code", "");
        setFieldValue(amountField, "Enter the amount", "");
        setStatus(" ", Color.GRAY);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (OrderItem item : cartItems) {
            tableModel.addRow(new Object[]{
                    item.getProductCode(),
                    makeThumbnailIcon(item.getThumbnail()),
                    item.getProductName(),
                    item.getAmount(),
                    "$" + PRICE_FMT.format(item.getUnitPrice()),
                    "$" + PRICE_FMT.format(item.getSubtotal())
            });
        }
        totalLabel.setText("Total: " + PRICE_FMT.format(computeTotal()) + "$");
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private void signOut() {
        dispose();
        new SignInScreen();
    }

    //  DIALOGS
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}