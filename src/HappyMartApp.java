import database.ProductDAO;
import model.Product;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;

public class HappyMartApp extends JFrame {

    // ─── DAO ──────────────────────────────────────────────────────────────────
    private final ProductDAO dao = new ProductDAO();

    // ─── State ────────────────────────────────────────────────────────────────
    private byte[] currentImageBytes = null;
    private int    selectedProductId = -1;

    // ─── Form components ─────────────────────────────────────────────────────
    private JLabel     imagePreviewLabel;
    private JButton    uploadImageBtn;
    private JTextField nameField;
    private JTextField amountField;
    private JTextField priceField;
    private JButton    addBtn;
    private JButton    deleteBtn;
    private JButton    updateBtn;
    private JButton    clearBtn;
    private JButton    signOutBtn;

    // ─── Table ────────────────────────────────────────────────────────────────
    private JTable         productTable;
    private DefaultTableModel tableModel;

    // ─── Fonts ───────────────────────────────────────────────────────────────
    private static final Font TITLE_FONT  = new Font("SansSerif", Font.BOLD, 22);
    private static final Font LABEL_FONT  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font TABLE_FONT  = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 13);

    // ─── Decimal format ──────────────────────────────────────────────────────
    private static final DecimalFormat PRICE_FMT = new DecimalFormat("#,##0.00");

    public HappyMartApp() {
        setTitle("Happy Mart");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 640);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);

        loadProducts();
        setVisible(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // Circle logo placeholder
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

    // ═════════════════════════════════════════════════════════════════════════
    //  MAIN PANEL  (left form + right table)
    // ═════════════════════════════════════════════════════════════════════════
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

    // ═════════════════════════════════════════════════════════════════════════
    //  FORM PANEL (left side)
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
            new LineBorder(Color.BLACK, 1),
            new EmptyBorder(14, 14, 14, 14)
        ));

        // ── Image preview ──
        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(160, 130));
        imagePreviewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setBorder(new LineBorder(Color.LIGHT_GRAY));
        imagePreviewLabel.setBackground(new Color(245, 245, 245));
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setText("No Image");
        imagePreviewLabel.setFont(LABEL_FONT);
        imagePreviewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Upload button ──
        uploadImageBtn = createButton("Upload Product Image", false);
        uploadImageBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadImageBtn.addActionListener(e -> chooseImage());

        // ── Fields ──
        nameField   = createField("Enter product name");
        amountField = createField("Enter amount in stock");
        priceField  = createField("Enter the price");

        // ── Add button ──
        addBtn = createButton("Add the product", true);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(e -> addProduct());

        // ── Bottom action row ──
        deleteBtn  = createSmallButton("Delete");
        updateBtn  = createSmallButton("Update");
        clearBtn   = createSmallButton("Clear");
        signOutBtn = createSmallButton("Sign Out");
        deleteBtn.addActionListener(e -> deleteProduct());
        updateBtn.addActionListener(e -> updateProduct());
        clearBtn.addActionListener(e -> clearForm());
        signOutBtn.addActionListener(e -> signOut());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionRow.setBackground(Color.WHITE);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        actionRow.add(deleteBtn);
        actionRow.add(updateBtn);
        actionRow.add(clearBtn);
        actionRow.add(signOutBtn);

        // ── Assemble ──
        panel.add(imagePreviewLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(uploadImageBtn);
        panel.add(Box.createVerticalStrut(14));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(amountField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(priceField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(addBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(actionRow);

        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TABLE PANEL (right side)
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("All products are displayed here");
        heading.setFont(LABEL_FONT);
        heading.setForeground(Color.DARK_GRAY);

        // ── Table model – thumbnail column stores ImageIcon ──
        String[] cols = {"Code", "Thumbnail", "Name", "Amount", "Price"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(TABLE_FONT);
        productTable.setRowHeight(100);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setGridColor(Color.LIGHT_GRAY);
        productTable.setShowGrid(true);
        productTable.setBackground(Color.WHITE);
        productTable.setSelectionBackground(new Color(220, 220, 220));
        productTable.setSelectionForeground(Color.BLACK);

        JTableHeader header = productTable.getTableHeader();
        header.setFont(HEADER_FONT);
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));

        // Column widths
        int[] widths = {80, 220, 180, 80, 100};
        for (int i = 0; i < widths.length; i++) {
            productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Centre-align cells
        DefaultTableCellRenderer centreRenderer = new DefaultTableCellRenderer();
        centreRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 3, 4}) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centreRenderer);
        }

        // Row selection → populate form
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(new LineBorder(Color.BLACK, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS – widget factories
    // ═════════════════════════════════════════════════════════════════════════
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

        // Placeholder behaviour
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
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setPreferredSize(new Dimension(200, 36));
        return btn;
    }

    private JButton createSmallButton(String text) {
        JButton btn = createButton(text, false);
        btn.setMaximumSize(new Dimension(90, 32));
        btn.setPreferredSize(new Dimension(80, 32));
        return btn;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  IMAGE
    // ═════════════════════════════════════════════════════════════════════════
    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try {
                currentImageBytes = Files.readAllBytes(f.toPath());
                showThumbnail(currentImageBytes);
            } catch (IOException ex) {
                showError("Cannot read image: " + ex.getMessage());
            }
        }
    }

    private void showThumbnail(byte[] data) {
        if (data == null) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");
            return;
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            if (img != null) {
                Image scaled = img.getScaledInstance(
                    imagePreviewLabel.getWidth() > 0  ? imagePreviewLabel.getWidth()  - 4 : 156,
                    imagePreviewLabel.getHeight() > 0 ? imagePreviewLabel.getHeight() - 4 : 126,
                    Image.SCALE_SMOOTH
                );
                imagePreviewLabel.setIcon(new ImageIcon(scaled));
                imagePreviewLabel.setText("");
            }
        } catch (IOException ex) {
            imagePreviewLabel.setIcon(null);
            imagePreviewLabel.setText("No Image");
        }
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

    // ═════════════════════════════════════════════════════════════════════════
    //  CRUD
    // ═════════════════════════════════════════════════════════════════════════
    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = dao.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getCode(),
                makeThumbnailIcon(p.getThumbnail()),
                p.getName(),
                p.getAmount(),
                "$" + PRICE_FMT.format(p.getPrice())
            });
        }
    }

    private void addProduct() {
        String name   = getFieldValue(nameField,   "Enter product name");
        String amtStr = getFieldValue(amountField, "Enter amount in stock");
        String prcStr = getFieldValue(priceField,  "Enter the price");

        if (name.isEmpty() || amtStr.isEmpty() || prcStr.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        int    amount;
        double price;
        try { amount = Integer.parseInt(amtStr); }
        catch (NumberFormatException e) { showError("Amount must be a whole number."); return; }
        try { price = Double.parseDouble(prcStr); }
        catch (NumberFormatException e) { showError("Price must be a number."); return; }

        String code = dao.generateNextCode();
        System.out.println(code);
        Product p   = new Product(code, name, amount, price, currentImageBytes);

        if (dao.insertProduct(p)) {
            loadProducts();
            clearForm();
            showInfo("Product added successfully! Code: " + code);
        } else {
            showError("Failed to add product.");
        }
    }

    private void updateProduct() {
        if (selectedProductId == -1) { showError("Select a product to update."); return; }

        String name   = getFieldValue(nameField,   "Enter product name");
        String amtStr = getFieldValue(amountField, "Enter amount in stock");
        String prcStr = getFieldValue(priceField,  "Enter the price");

        if (name.isEmpty() || amtStr.isEmpty() || prcStr.isEmpty()) {
            showError("Please fill in all fields."); return;
        }

        int    amount;
        double price;
        try { amount = Integer.parseInt(amtStr); }
        catch (NumberFormatException e) { showError("Amount must be a whole number."); return; }
        try { price = Double.parseDouble(prcStr); }
        catch (NumberFormatException e) { showError("Price must be a number."); return; }

        Product existing = dao.getProductById(selectedProductId);
        if (existing == null) { showError("Product not found."); return; }

        byte[] imgBytes = (currentImageBytes != null) ? currentImageBytes : existing.getThumbnail();
        Product updated = new Product(selectedProductId, existing.getCode(), name, amount, price, imgBytes);

        if (dao.updateProduct(updated)) {
            loadProducts();
            clearForm();
            showInfo("Product updated successfully!");
        } else {
            showError("Failed to update product.");
        }
    }

    private void deleteProduct() {
        if (selectedProductId == -1) { showError("Select a product to delete."); return; }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Delete this product?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteProduct(selectedProductId)) {
                loadProducts();
                clearForm();
                showInfo("Product deleted.");
            } else {
                showError("Failed to delete product.");
            }
        }
    }

    private void clearForm() {
        selectedProductId = -1;
        currentImageBytes = null;
        imagePreviewLabel.setIcon(null);
        imagePreviewLabel.setText("No Image");
        setFieldValue(nameField,   "Enter product name",       "");
        setFieldValue(amountField, "Enter amount in stock",    "");
        setFieldValue(priceField,  "Enter the price",          "");
        productTable.clearSelection();
    }

    // ── Row selected → fill form ──────────────────────────────────────────────
    private void onRowSelected() {
        int row = productTable.getSelectedRow();
        if (row < 0) return;

        String code = (String) tableModel.getValueAt(row, 0);
        List<Product> all = dao.getAllProducts();
        Product found = all.stream()
            .filter(p -> p.getCode().equals(code))
            .findFirst().orElse(null);

        if (found == null) return;

        selectedProductId = found.getId();
        currentImageBytes = found.getThumbnail();
        showThumbnail(currentImageBytes);
        setFieldValue(nameField,   "Enter product name",    found.getName());
        setFieldValue(amountField, "Enter amount in stock", String.valueOf(found.getAmount()));
        setFieldValue(priceField,  "Enter the price",       String.valueOf(found.getPrice()));
    }

    private void signOut() {
        dispose();
        new SignInScreen();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DIALOGS
    // ═════════════════════════════════════════════════════════════════════════
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(HappyMartApp::new);
    }
}
