import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

// ============================================================
// DEIHS LIBRARY SYSTEM
// Complete Java Swing Library Management System
// ============================================================

abstract class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String title;
    private final String author;
    private final String category;
    private boolean borrowed;

    public Book(int id, String title, String author,
                String category, boolean borrowed) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.borrowed = borrowed;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isBorrowed() { return borrowed; }

    public void setBorrowed(boolean borrowed) {
        this.borrowed = borrowed;
    }

    public abstract String getType();
    public abstract String getExtraInfo();
}

class PrintedBook extends Book {
    private static final long serialVersionUID = 1L;
    private final int pageCount;

    public PrintedBook(int id, String title, String author,
                       String category, boolean borrowed, int pageCount) {
        super(id, title, author, category, borrowed);
        this.pageCount = pageCount;
    }

    public int getPageCount() { return pageCount; }

    @Override
    public String getType() { return "Printed"; }

    @Override
    public String getExtraInfo() {
        return pageCount + " pages";
    }
}

class Ebook extends Book {
    private static final long serialVersionUID = 1L;
    private final double fileSizeMB;

    public Ebook(int id, String title, String author,
                 String category, boolean borrowed, double fileSizeMB) {
        super(id, title, author, category, borrowed);
        this.fileSizeMB = fileSizeMB;
    }

    public double getFileSizeMB() { return fileSizeMB; }

    @Override
    public String getType() { return "E-Book"; }

    @Override
    public String getExtraInfo() {
        return String.format(Locale.US, "%.2f MB", fileSizeMB);
    }
}

public class Book1 extends JFrame {

    private static final String DATA_FILE = "library_data.ser";

    private final List<Book> catalog = new ArrayList<>();

    private final Color NAVY = new Color(13, 38, 76);
    private final Color BLUE = new Color(25, 105, 200);
    private final Color BG = new Color(243, 247, 253);
    private final Color BORDER = new Color(220, 229, 240);
    private final Color TEXT = Color.BLACK;
    private final Color MUTED = new Color(85, 96, 110);
    private final Color GREEN = new Color(218, 247, 230);
    private final Color PURPLE = new Color(237, 229, 255);
    private final Color ORANGE = new Color(255, 238, 215);

    private JPanel mainContent;
    private JPanel sidebar;
    private JLabel pageTitle;
    private JLabel statusLabel;

    private JLabel totalValue;
    private JLabel availableValue;
    private JLabel borrowedValue;
    private JLabel printedValue;
    private JLabel ebookValue;

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField searchField;
    private JComboBox<String> categoryFilter;

    public Book1() {
        setTitle("DEIHS Library System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1380, 820);
        setLocationRelativeTo(null);

        loadCatalog();
        buildInterface();
        showDashboard();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    // =========================================================
    // MAIN INTERFACE
    // =========================================================

    private void buildInterface() {
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG);

        rightPanel.add(createTopBar(), BorderLayout.NORTH);

        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG);
        mainContent.setBorder(new EmptyBorder(12, 18, 8, 18));

        rightPanel.add(mainContent, BorderLayout.CENTER);
        rightPanel.add(createFooter(), BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.CENTER);
    }

    // =========================================================
    // LEFT SIDEBAR
    // Every sidebar button opens its own page
    // =========================================================

    private JPanel createSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(NAVY);
        panel.setPreferredSize(new Dimension(220, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(20, 16, 18, 10));

        JLabel logo = new JLabel("@ DEIHS");
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel subtitle = new JLabel("      LIBRARY SYSTEM");
        subtitle.setForeground(new Color(100, 220, 235));
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 10));

        brand.add(logo);
        brand.add(Box.createVerticalStrut(5));
        brand.add(subtitle);

        panel.add(brand, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(8, 12, 8, 12));

        addSidebarButton(menu, "⌂", "Dashboard",
                this::showDashboard);

        addSidebarButton(menu, "+", "Add a Book",
                this::showAddBookPage);

        addSidebarButton(menu, "↗", "Borrow Book",
                this::showBorrowPage);

        addSidebarButton(menu, "↶", "Return Book",
                this::showReturnPage);

        addSidebarButton(menu, "✕", "Remove Book",
                this::showRemoveBookPage);

        addSidebarButton(menu, "⇧", "Upload Book File",
                this::uploadBookFile);

        addSidebarButton(menu, "▤", "View Full Catalog",
                this::showCatalogPage);

        addSidebarButton(menu, "▣", "Save Catalog",
                this::saveCatalog);

        panel.add(menu, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(10, 16, 20, 8));

        JLabel quote = new JLabel("\"Read. Learn. Grow.\"");
        quote.setForeground(new Color(100, 220, 235));
        quote.setFont(new Font("Serif", Font.ITALIC, 15));

        JLabel school = new JLabel("DEIHS SCHOOL LIBRARY");
        school.setForeground(Color.WHITE);
        school.setFont(new Font("SansSerif", Font.BOLD, 9));

        bottom.add(quote);
        bottom.add(Box.createVerticalStrut(9));
        bottom.add(school);

        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private void addSidebarButton(
            JPanel parent, String icon, String text, Runnable action) {

        JButton button = new JButton(icon + "   " + text);

        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(248, 250, 255));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Thinner borders and smaller padding
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(190, 205, 225), 1
                ),
                new EmptyBorder(7, 10, 7, 5)
        ));

        button.setPreferredSize(new Dimension(196, 40));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setMinimumSize(new Dimension(175, 40));

        button.addActionListener(e -> action.run());

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(215, 231, 250));
                button.setForeground(Color.BLACK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(248, 250, 255));
                button.setForeground(Color.BLACK);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(new Color(195, 218, 246));
                button.setForeground(Color.BLACK);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(
                        button.contains(e.getPoint())
                                ? new Color(215, 231, 250)
                                : new Color(248, 250, 255)
                );
                button.setForeground(Color.BLACK);
            }
        });

        parent.add(button);
        parent.add(Box.createVerticalStrut(7));
    }

    // =========================================================
    // TOP BAR
    // =========================================================

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        pageTitle = new JLabel("Library Dashboard");
        pageTitle.setForeground(TEXT);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 19));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel date = new JLabel(
                new SimpleDateFormat("EEE, dd MMM yyyy").format(new Date())
        );
        date.setForeground(TEXT);
        date.setFont(new Font("SansSerif", Font.BOLD, 11));

        JLabel avatar = new JLabel(" M ");
        avatar.setOpaque(true);
        avatar.setBackground(new Color(220, 235, 255));
        avatar.setForeground(TEXT);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 14));
        avatar.setBorder(new EmptyBorder(5, 5, 5, 5));

        right.add(date);
        right.add(avatar);

        top.add(pageTitle, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    // =========================================================
    // DASHBOARD
    //Note: The dashboard displays statistics and the book catalog
    // =========================================================

    private void showDashboard() {
        pageTitle.setText("Library Dashboard");
        mainContent.removeAll();

        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);

        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setOpaque(false);

        topSection.add(createWelcomePanel(), BorderLayout.NORTH);
        topSection.add(createStatisticsPanel(), BorderLayout.CENTER);

        page.add(topSection, BorderLayout.NORTH);
        page.add(createCatalogPanel(), BorderLayout.CENTER);

        mainContent.add(page, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();

        refreshCatalogTable();
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(new Color(220, 235, 255));

        // Reduced border thickness
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(150, 190, 240), 1
                ),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Welcome to DEIHS Library!");
        heading.setForeground(Color.BLACK);
        heading.setFont(new Font("SansSerif", Font.BOLD, 21));

        JLabel description = new JLabel(
                "Your learning journey begins here. Explore and manage your collection."
        );
        description.setForeground(new Color(45, 55, 70));
        description.setFont(new Font("SansSerif", Font.PLAIN, 12));

        labels.add(heading);
        labels.add(Box.createVerticalStrut(5));
        labels.add(description);

        JLabel bookIcon = new JLabel("▤");
        bookIcon.setForeground(new Color(25, 85, 160));
        bookIcon.setFont(new Font("SansSerif", Font.BOLD, 34));

        panel.add(labels, BorderLayout.CENTER);
        panel.add(bookIcon, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 0));
        panel.setOpaque(false);

        panel.add(createStatCard("TOTAL BOOKS", "0",
                new Color(220, 235, 255), label -> totalValue = label));

        panel.add(createStatCard("AVAILABLE", "0",
                GREEN, label -> availableValue = label));

        panel.add(createStatCard("BORROWED", "0",
                new Color(255, 225, 229), label -> borrowedValue = label));

        panel.add(createStatCard("PRINTED-BOOKS", "0",
                ORANGE, label -> printedValue = label));

        panel.add(createStatCard("E-BOOKS", "0",
                PURPLE, label -> ebookValue = label));

        return panel;
    }

    private JPanel createStatCard(
            String title, String initialValue, Color background,
            java.util.function.Consumer<JLabel> labelConsumer) {

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel heading = new JLabel(title);
        heading.setForeground(MUTED);
        heading.setFont(new Font("SansSerif", Font.BOLD, 10));

        JLabel value = new JLabel(initialValue);
        value.setForeground(Color.BLACK);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);

        labelConsumer.accept(value);

        return panel;
    }

    // =========================================================
    // CATALOG PANEL
    // =========================================================

    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(12, 12, 10, 12)
        ));

        JPanel header = new JPanel(new BorderLayout(10, 8));
        header.setOpaque(false);

        JPanel headingPanel = new JPanel();
        headingPanel.setOpaque(false);
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("Book Catalog");
        heading.setForeground(Color.BLACK);
        heading.setFont(new Font("SansSerif", Font.BOLD, 17));

        JLabel subtitle = new JLabel("Browse and search your library collection.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));

        headingPanel.add(heading);
        headingPanel.add(Box.createVerticalStrut(3));
        headingPanel.add(subtitle);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        filters.setOpaque(false);

        searchField = new JTextField(13);
        searchField.setToolTipText("Search title, author, category, or ID");
        searchField.setForeground(Color.BLACK);
        searchField.setBackground(Color.WHITE);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(175, 32));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));

        categoryFilter = new JComboBox<>();
        categoryFilter.setForeground(Color.BLACK);
        categoryFilter.setBackground(Color.WHITE);
        categoryFilter.setFont(new Font("SansSerif", Font.PLAIN, 11));
        categoryFilter.setPreferredSize(new Dimension(130, 32));

        JButton refresh = createButton("Refresh",
                new Color(220, 235, 255));

        refresh.setPreferredSize(new Dimension(85, 32));
        refresh.addActionListener(e -> refreshCatalogTable());

        filters.add(searchField);
        filters.add(categoryFilter);
        filters.add(refresh);

        header.add(headingPanel, BorderLayout.WEST);
        header.add(filters, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        String[] columns = {
                "ID", "TYPE", "BOOK TITLE", "AUTHOR",
                "CATEGORY", "STATUS", "DETAILS"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bookTable.setForeground(Color.BLACK);
        bookTable.setBackground(Color.WHITE);
        bookTable.setSelectionBackground(new Color(210, 230, 252));
        bookTable.setSelectionForeground(Color.BLACK);
        bookTable.setRowHeight(32);
        bookTable.setShowVerticalLines(false);
        bookTable.setGridColor(new Color(230, 237, 247));
        bookTable.setFillsViewportHeight(true);
        bookTable.setIntercellSpacing(new Dimension(0, 1));
        bookTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader th = bookTable.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 10));
        th.setForeground(Color.BLACK);
        th.setBackground(new Color(235, 242, 251));
        th.setPreferredSize(new Dimension(0, 34));
        th.setReorderingAllowed(false);

        sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);

        int[] widths = {45, 75, 170, 130, 100, 90, 85};

        for (int i = 0; i < widths.length; i++) {
            bookTable.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
            bookTable.getColumnModel().getColumn(i)
                    .setCellRenderer(new BookCellRenderer());
        }

        JScrollPane scroll = new JScrollPane(bookTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);

        // Table expands to fill available dashboard space
        panel.add(scroll, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        applyFilters();
                    }
                });

        categoryFilter.addActionListener(e -> applyFilters());

        return panel;
    }

    private class BookCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, selected, focus, row, column);

            label.setBorder(new EmptyBorder(0, 7, 0, 6));
            label.setForeground(Color.BLACK);
            label.setBackground(row % 2 == 0
                    ? Color.WHITE : new Color(247, 250, 255));

            label.setFont(new Font("SansSerif",
                    column == 2 ? Font.BOLD : Font.PLAIN, 11));

            if (selected) {
                label.setBackground(new Color(210, 230, 252));
                label.setForeground(Color.BLACK);
            }

            if (column == 5 && !selected && value != null) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("SansSerif", Font.BOLD, 10));
                label.setBackground("Borrowed".equals(value.toString())
                        ? new Color(255, 225, 229) : GREEN);
            } else if (column == 0 || column == 1) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            return label;
        }
    }

    // =========================================================
    // ADD BOOK PAGE
    // Includes Printed / E-Book selection 
    // =========================================================

    private void showAddBookPage() {
        pageTitle.setText("Add a Book");
        mainContent.removeAll();

        JPanel page = createFormPage(
                "Add a New Book",
                "Enter the book details below to add it to the catalog."
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 20, 18, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"Printed Book", "E-Book"}
        );
        typeBox.setForeground(Color.BLACK);
        typeBox.setBackground(Color.WHITE);
        typeBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JTextField idField = createTextField();
        JTextField titleField = createTextField();
        JTextField authorField = createTextField();
        JTextField categoryField = createTextField();
        JTextField detailsField = createTextField();

        JLabel detailsLabel = new JLabel("Number of pages:");
        detailsLabel.setForeground(Color.BLACK);

        typeBox.addActionListener(e -> {
            if (typeBox.getSelectedIndex() == 0) {
                detailsLabel.setText("Number of pages:");
                detailsField.setToolTipText("Enter the total number of pages");
            } else {
                detailsLabel.setText("File size (MB):");
                detailsField.setToolTipText("Enter the ebook file size in MB");
            }
        });

        addFormRow(form, c, 0, "Book Type", typeBox);
        addFormRow(form, c, 1, "Book ID", idField);
        addFormRow(form, c, 2, "Book Title", titleField);
        addFormRow(form, c, 3, "Author", authorField);
        addFormRow(form, c, 4, "Category", categoryField);
        addFormRow(form, c, 5, detailsLabel, detailsField);

        JButton save = createButton("Save Book", GREEN);
        JButton clear = createButton("Clear Fields",
                new Color(235, 238, 243));
        JButton back = createButton("Back to Dashboard",
                new Color(220, 235, 255));

        save.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String category = categoryField.getText().trim();
                String details = detailsField.getText().trim();

                if (id <= 0 || title.isEmpty() || author.isEmpty()
                        || category.isEmpty() || details.isEmpty()) {
                    showWarning("Please complete all fields.");
                    return;
                }

                if (findBook(id) != null) {
                    showWarning("This Book ID already exists.");
                    return;
                }

                Book book;

                if (typeBox.getSelectedIndex() == 0) {
                    int pages = Integer.parseInt(details);

                    if (pages <= 0) {
                        showWarning("Page count must be greater than zero.");
                        return;
                    }

                    book = new PrintedBook(
                            id, title, author, category, false, pages);
                } else {
                    double size = Double.parseDouble(details);

                    if (!Double.isFinite(size) || size <= 0) {
                        showWarning("File size must be greater than zero.");
                        return;
                    }

                    book = new Ebook(
                            id, title, author, category, false, size);
                }

                catalog.add(book);
                persistCatalog();
                updateStatistics();
                setStatus("Added book: " + title);

                JOptionPane.showMessageDialog(this,
                        "Book added successfully!",
                        "DEIHS Library", JOptionPane.INFORMATION_MESSAGE);

                showDashboard();

            } catch (NumberFormatException ex) {
                showWarning("Enter a valid Book ID and valid numeric details.");
            }
        });

        clear.addActionListener(e -> {
            idField.setText("");
            titleField.setText("");
            authorField.setText("");
            categoryField.setText("");
            detailsField.setText("");
            typeBox.setSelectedIndex(0);
            idField.requestFocusInWindow();
        });

        back.addActionListener(e -> showDashboard());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setBackground(Color.WHITE);
        buttons.add(save);
        buttons.add(clear);
        buttons.add(back);

        addFormRow(form, c, 6, "", buttons);

        page.add(form, BorderLayout.NORTH);
        mainContent.add(page, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    // =========================================================
    // BORROW BOOK PAGE
    // =========================================================

    private void showBorrowPage() {
        showTransactionPage(true);
    }

    // =========================================================
    // RETURN BOOK PAGE
    // =========================================================

    private void showReturnPage() {
        showTransactionPage(false);
    }

    private void showTransactionPage(boolean borrowing) {
        pageTitle.setText(borrowing ? "Borrow a Book" : "Return a Book");
        mainContent.removeAll();

        JPanel page = createFormPage(
                borrowing ? "Borrow a Book" : "Return a Book",
                borrowing
                        ? "Enter a Book ID to record a book as borrowed."
                        : "Enter a Book ID to mark a borrowed book as available."
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 8, 9, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = createTextField();
        idField.setPreferredSize(new Dimension(330, 38));

        JLabel preview = new JLabel("Book information will appear here.");
        preview.setForeground(MUTED);
        preview.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton find = createButton("Find Book",
                new Color(220, 235, 255));

        find.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Book book = findBook(id);

                if (book == null) {
                    preview.setText("Book not found.");
                    showWarning("No book was found with that ID.");
                    return;
                }

                preview.setText("<html><b>" + escapeHtml(book.getTitle())
                        + "</b> — " + escapeHtml(book.getAuthor())
                        + "<br>Type: " + escapeHtml(book.getType())
                        + " | Status: "
                        + (book.isBorrowed() ? "Borrowed" : "Available")
                        + "</html>");
            } catch (NumberFormatException ex) {
                showWarning("Enter a valid numeric Book ID.");
            }
        });

        JButton submit = createButton(
                borrowing ? "Confirm Borrow" : "Confirm Return",
                borrowing ? new Color(220, 235, 255) : GREEN
        );

        submit.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Book book = findBook(id);

                if (book == null) {
                    showWarning("Book ID not found.");
                    return;
                }

                if (borrowing && book.isBorrowed()) {
                    showWarning("This book has already been borrowed.");
                    return;
                }

                if (!borrowing && !book.isBorrowed()) {
                    showWarning("This book is already available.");
                    return;
                }

                book.setBorrowed(borrowing);
                persistCatalog();
                updateStatistics();

                JOptionPane.showMessageDialog(this,
                        borrowing
                                ? "Book borrowed successfully:\n" + book.getTitle()
                                : "Book returned successfully:\n" + book.getTitle(),
                        "DEIHS Library",
                        JOptionPane.INFORMATION_MESSAGE);

                setStatus((borrowing ? "Borrowed: " : "Returned: ")
                        + book.getTitle());

                showDashboard();

            } catch (NumberFormatException ex) {
                showWarning("Enter a valid numeric Book ID.");
            }
        });

        JButton back = createButton("Back to Dashboard",
                new Color(235, 238, 243));
        back.addActionListener(e -> showDashboard());

        addFormRow(form, c, 0, "Book ID", idField);
        addFormRow(form, c, 1, "", find);
        addFormRow(form, c, 2, "Book Details", preview);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        buttons.setBackground(Color.WHITE);
        buttons.add(submit);
        buttons.add(back);

        addFormRow(form, c, 3, "", buttons);

        page.add(form, BorderLayout.NORTH);
        mainContent.add(page, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    // =========================================================
    // REMOVE BOOK PAGE
    // =========================================================

    private void showRemoveBookPage() {
        pageTitle.setText("Remove a Book");
        mainContent.removeAll();

        JPanel page = createFormPage(
                "Remove a Book",
                "Enter a Book ID to permanently remove a book, or remove all books from the catalog."
        );

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 8, 9, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = createTextField();
        idField.setPreferredSize(new Dimension(330, 38));

        JLabel preview = new JLabel("Book information will appear here.");
        preview.setForeground(MUTED);
        preview.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton find = createButton("Find Book",
                new Color(220, 235, 255));

        find.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Book book = findBook(id);

                if (book == null) {
                    preview.setText("Book not found.");
                    showWarning("No book was found with that ID.");
                    return;
                }

                preview.setText("<html><b>" + escapeHtml(book.getTitle())
                        + "</b> — " + escapeHtml(book.getAuthor())
                        + "<br>Type: " + escapeHtml(book.getType())
                        + " | Status: "
                        + (book.isBorrowed() ? "Borrowed" : "Available")
                        + "</html>");
            } catch (NumberFormatException ex) {
                showWarning("Enter a valid numeric Book ID.");
            }
        });

        JButton submit = createButton("Confirm Removal",
                new Color(255, 225, 229));

        submit.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Book book = findBook(id);

                if (book == null) {
                    showWarning("Book ID not found.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to remove the following book?\n\n"
                                + "ID: " + book.getId() + "\n"
                                + "Title: " + book.getTitle() + "\n"
                                + "Author: " + book.getAuthor(),
                        "Confirm Removal",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                catalog.remove(book);
                persistCatalog();
                updateStatistics();

                JOptionPane.showMessageDialog(this,
                        "Book removed successfully:\n" + book.getTitle(),
                        "DEIHS Library",
                        JOptionPane.INFORMATION_MESSAGE);

                setStatus("Removed book: " + book.getTitle());
                showDashboard();

            } catch (NumberFormatException ex) {
                showWarning("Enter a valid numeric Book ID.");
            }
        });

        JButton removeAllBtn = createButton("Remove All Books",
                new Color(255, 200, 200));

        removeAllBtn.addActionListener(e -> {
            if (catalog.isEmpty()) {
                showWarning("The catalog is already empty.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "WARNING: Are you sure you want to delete ALL books from the catalog?\nThis action cannot be undone!",
                    "Confirm Remove All Books",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            catalog.clear();
            persistCatalog();
            updateStatistics();

            JOptionPane.showMessageDialog(this,
                    "All books have been removed from the catalog.",
                    "DEIHS Library",
                    JOptionPane.INFORMATION_MESSAGE);

            setStatus("All books removed from catalog.");
            showDashboard();
        });

        JButton back = createButton("Back to Dashboard",
                new Color(235, 238, 243));
        back.addActionListener(e -> showDashboard());

        addFormRow(form, c, 0, "Book ID", idField);
        addFormRow(form, c, 1, "", find);
        addFormRow(form, c, 2, "Book Details", preview);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        buttons.setBackground(Color.WHITE);
        buttons.add(submit);
        buttons.add(removeAllBtn);
        buttons.add(back);

        addFormRow(form, c, 3, "", buttons);

        page.add(form, BorderLayout.NORTH);
        mainContent.add(page, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();
    }

    // =========================================================
    // FULL CATALOG PAGE
    // =========================================================

    private void showCatalogPage() {
        pageTitle.setText("Full Book Catalog");
        mainContent.removeAll();

        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);

        JLabel description = new JLabel(
                "Complete collection of printed books and electronic books."
        );
        description.setForeground(MUTED);
        description.setFont(new Font("SansSerif", Font.PLAIN, 12));

        page.add(description, BorderLayout.NORTH);
        page.add(createCatalogPanel(), BorderLayout.CENTER);

        mainContent.add(page, BorderLayout.CENTER);
        mainContent.revalidate();
        mainContent.repaint();

        refreshCatalogTable();
    }

    // =========================================================
    // FILE UPLOAD
    // CSV/TXT format:
    // Printed,101,Book Title,Author,Category,false,250
    // Ebook,102,Digital Book,Author,Education,false,12.5
    // =========================================================

    private void uploadBookFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Upload Book Data File");
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "Text or CSV Files", "txt", "csv"
                )
        );

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        int added = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(chooser.getSelectedFile()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                String[] p = line.split(",", -1);

                if (p.length < 7) {
                    skipped++;
                    continue;
                }

                try {
                    String type = p[0].trim();
                    int id = Integer.parseInt(p[1].trim());
                    String title = p[2].trim();
                    String author = p[3].trim();
                    String category = p[4].trim();
                    boolean borrowed = Boolean.parseBoolean(p[5].trim());

                    if (id <= 0 || title.isEmpty() || author.isEmpty()
                            || findBook(id) != null) {
                        skipped++;
                        continue;
                    }

                    Book book;

                    if (type.equalsIgnoreCase("Printed")) {
                        int pages = Integer.parseInt(p[6].trim());

                        if (pages <= 0) {
                            skipped++;
                            continue;
                        }

                        book = new PrintedBook(
                                id, title, author, category, borrowed, pages);
                    } else if (type.equalsIgnoreCase("Ebook")
                            || type.equalsIgnoreCase("E-Book")) {
                        double size = Double.parseDouble(p[6].trim());

                        if (!Double.isFinite(size) || size <= 0) {
                            skipped++;
                            continue;
                        }

                        book = new Ebook(
                                id, title, author, category, borrowed, size);
                    } else {
                        skipped++;
                        continue;
                    }

                    catalog.add(book);
                    added++;

                } catch (RuntimeException ex) {
                    skipped++;
                }
            }

            persistCatalog();
            updateStatistics();
            setStatus("Import complete. Added: " + added
                    + ", skipped: " + skipped);

            JOptionPane.showMessageDialog(this,
                    "Import completed!\n\nBooks added: " + added
                            + "\nRows skipped: " + skipped,
                    "Upload Results", JOptionPane.INFORMATION_MESSAGE);

            showDashboard();

        } catch (IOException ex) {
            showError("Unable to read file:\n" + ex.getMessage());
        }
    }

    // =========================================================
    // FILTERS AND TABLE REFRESH
    // =========================================================

    private void refreshCatalogTable() {
        if (tableModel == null || bookTable == null) {
            return;
        }

        tableModel.setRowCount(0);

        for (Book book : catalog) {
            tableModel.addRow(new Object[]{
                    book.getId(),
                    book.getType(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.isBorrowed() ? "Borrowed" : "Available",
                    book.getExtraInfo()
            });
        }

        updateStatistics();
        updateCategoryOptions();
        applyFilters();
    }

    private void applyFilters() {
        if (sorter == null || searchField == null
                || categoryFilter == null) {
            return;
        }

        List<RowFilter<DefaultTableModel, Object>> filters =
                new ArrayList<>();

        String query = searchField.getText().trim();

        if (!query.isEmpty()) {
            filters.add(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(query)));
        }

        Object selected = categoryFilter.getSelectedItem();

        if (selected != null
                && !"All Categories".equals(selected.toString())) {
            filters.add(RowFilter.regexFilter(
                    "^" + Pattern.quote(selected.toString()) + "$", 4));
        }

        sorter.setRowFilter(filters.isEmpty()
                ? null : RowFilter.andFilter(filters));

        setStatus("Showing " + bookTable.getRowCount()
                + " of " + catalog.size() + " books.");
    }

    private void updateCategoryOptions() {
        Object previous = categoryFilter.getSelectedItem();

        Set<String> categories = new TreeSet<>(
                String.CASE_INSENSITIVE_ORDER);

        for (Book book : catalog) {
            if (!book.getCategory().trim().isEmpty()) {
                categories.add(book.getCategory());
            }
        }

        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");

        for (String category : categories) {
            categoryFilter.addItem(category);
        }

        if (previous != null) {
            categoryFilter.setSelectedItem(previous.toString());
        }

        if (categoryFilter.getSelectedIndex() < 0) {
            categoryFilter.setSelectedIndex(0);
        }
    }

    private void updateStatistics() {
        if (totalValue == null) return;

        int borrowed = 0;
        int printed = 0;
        int ebooks = 0;

        for (Book book : catalog) {
            if (book.isBorrowed()) borrowed++;
            if (book instanceof PrintedBook) printed++;
            if (book instanceof Ebook) ebooks++;
        }

        totalValue.setText(String.valueOf(catalog.size()));
        availableValue.setText(String.valueOf(catalog.size() - borrowed));
        borrowedValue.setText(String.valueOf(borrowed));
        printedValue.setText(String.valueOf(printed));
        ebookValue.setText(String.valueOf(ebooks));
    }

    // =========================================================
    // SHARED FORM DESIGN
    // =========================================================

    private JPanel createFormPage(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Color.WHITE);
        page.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitleLabel);

        page.add(heading, BorderLayout.NORTH);

        return page;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(24);
        field.setForeground(Color.BLACK);
        field.setBackground(Color.WHITE);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(330, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints base,
            int row,
            String label,
            JComponent field) {

        JLabel name = new JLabel(label);
        name.setForeground(Color.BLACK);
        name.setFont(new Font("SansSerif", Font.BOLD, 12));

        GridBagConstraints left = (GridBagConstraints) base.clone();
        left.gridx = 0;
        left.gridy = row;
        left.weightx = 0.25;
        left.anchor = GridBagConstraints.WEST;
        panel.add(name, left);

        GridBagConstraints right = (GridBagConstraints) base.clone();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 0.75;
        right.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, right);
    }

    // Overload lets the Add Book page update the label dynamically.
    private void addFormRow(
            JPanel panel,
            GridBagConstraints base,
            int row,
            JLabel label,
            JComponent field) {

        label.setForeground(Color.BLACK);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));

        GridBagConstraints left = (GridBagConstraints) base.clone();
        left.gridx = 0;
        left.gridy = row;
        left.weightx = 0.25;
        left.anchor = GridBagConstraints.WEST;
        panel.add(label, left);

        GridBagConstraints right = (GridBagConstraints) base.clone();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 0.75;
        right.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, right);
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.BLACK);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1),
                new EmptyBorder(7, 12, 7, 12)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(background.darker());
                button.setForeground(Color.BLACK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
                button.setForeground(Color.BLACK);
            }
        });

        return button;
    }

    // =========================================================
    // SAVE / LOAD
    // =========================================================

    private void loadCatalog() {
        File file = new File(DATA_FILE);

        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(file))) {

                Object data = in.readObject();

                if (data instanceof List<?>) {
                    for (Object item : (List<?>) data) {
                        if (item instanceof Book) {
                            Book book = (Book) item;

                            if (findBook(book.getId()) == null) {
                                catalog.add(book);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Could not load catalog: "
                        + ex.getMessage());
            }
        }

        if (catalog.isEmpty()) {
            catalog.add(new PrintedBook(
                    101, "To Kill a Mockingbird", "Harper Lee",
                    "Fiction", false, 281));

            catalog.add(new Ebook(
                    102, "Java Programming", "John Doe",
                    "Education", false, 15.0));

            catalog.add(new PrintedBook(
                    103, "Introduction to Science", "Maria Santos",
                    "Science", false, 320));

            catalog.add(new Ebook(
                    104, "Digital Learning Guide", "Alex Reyes",
                    "Education", false, 8.5));

            persistCatalog();
        }
    }

    private void persistCatalog() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            out.writeObject(new ArrayList<>(catalog));
        } catch (IOException ex) {
            showError("Unable to save catalog:\n" + ex.getMessage());
        }
    }

    private void saveCatalog() {
        persistCatalog();
        setStatus("Catalog saved successfully.");

        JOptionPane.showMessageDialog(this,
                "Your catalog has been saved.",
                "Save Catalog", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    // UTILITIES
    // =========================================================

    private Book findBook(int id) {
        for (Book book : catalog) {
            if (book.getId() == id) return book;
        }
        return null;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("●  " + message);
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Please Check", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =========================================================
    // FOOTER
    // =========================================================

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(7, 18, 7, 18)
        ));

        statusLabel = new JLabel("●  System ready");
        statusLabel.setForeground(new Color(15, 125, 80));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 10));

        JLabel right = new JLabel("DEIHS Library Hub | Java Swing");
        right.setForeground(MUTED);
        right.setFont(new Font("SansSerif", Font.PLAIN, 10));

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);

        return footer;
    }

    // =========================================================
    // EXIT
    // =========================================================

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Would you like to save the catalog before exiting?",
                "Exit DEIHS Library Hub",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.CANCEL_OPTION
                || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        if (choice == JOptionPane.YES_OPTION) {
            persistCatalog();
        }

        dispose();
        System.exit(0);
    }

    // =========================================================
    // RUN APPLICATION
    // =========================================================

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            Book1 app = new Book1();
            app.setVisible(true);
        });
    }
}