
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class OnlineBankingSystem {
    // Database connection details
    private static final String DB_URL = "jdbc:h2:./bankingDB";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    
    // UI Components
    private JFrame mainFrame;
    private JPanel loginPanel, registerPanel, dashboardPanel, transferPanel, historyPanel;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Current logged in user
    private int currentUserId = -1;
    private String currentUsername = "";
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start application
        SwingUtilities.invokeLater(() -> {
            OnlineBankingSystem app = new OnlineBankingSystem();
            app.initializeDatabase();
            app.createAndShowGUI();
        });
    }
    
    // Initialize database tables if they don't exist
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Create users table
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS users (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "username VARCHAR(50) UNIQUE NOT NULL, " +
                             "password VARCHAR(50) NOT NULL, " +
                             "full_name VARCHAR(100) NOT NULL, " +
                             "email VARCHAR(100) UNIQUE NOT NULL, " +
                             "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.executeUpdate(sql);
            }
            
            // Create accounts table
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "user_id INT NOT NULL, " +
                             "account_number VARCHAR(20) UNIQUE NOT NULL, " +
                             "account_type VARCHAR(20) NOT NULL, " +
                             "balance DECIMAL(15,2) DEFAULT 0.00, " +
                             "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                             "FOREIGN KEY (user_id) REFERENCES users(id))";
                stmt.executeUpdate(sql);
            }
            
            // Create transactions table
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "from_account_id INT, " +
                             "to_account_id INT, " +
                             "amount DECIMAL(15,2) NOT NULL, " +
                             "transaction_type VARCHAR(20) NOT NULL, " +
                             "description VARCHAR(200), " +
                             "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                             "FOREIGN KEY (from_account_id) REFERENCES accounts(id), " +
                             "FOREIGN KEY (to_account_id) REFERENCES accounts(id))";
                stmt.executeUpdate(sql);
            }
            
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to initialize database: " + e.getMessage(), 
                                         "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Create and display the main GUI
    private void createAndShowGUI() {
        // Create main frame
        mainFrame = new JFrame("Online Banking System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);
        
        // Create card layout for switching between panels
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Initialize all panels
        createLoginPanel();
        createRegisterPanel();
        createDashboardPanel();
        createTransferPanel();
        createHistoryPanel();
        
        // Add panels to content panel
        contentPanel.add(loginPanel, "LOGIN");
        contentPanel.add(registerPanel, "REGISTER");
        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(transferPanel, "TRANSFER");
        contentPanel.add(historyPanel, "HISTORY");
        
        // Show login panel first
        cardLayout.show(contentPanel, "LOGIN");
        
        // Add content panel to frame
        mainFrame.add(contentPanel);
        mainFrame.setVisible(true);
    }
    
    // Create login panel
    private void createLoginPanel() {
        loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // North panel for title
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Online Banking System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Center panel for login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        
        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add action listeners
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter both username and password", 
                                             "Login Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Authenticate user
            if (authenticateUser(username, password)) {
                // Load user dashboard
                loadUserDashboard();
                cardLayout.show(contentPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Invalid username or password", 
                                             "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        registerButton.addActionListener(e -> cardLayout.show(contentPanel, "REGISTER"));
        
        // Add panels to login panel
        loginPanel.add(titlePanel, BorderLayout.NORTH);
        loginPanel.add(formPanel, BorderLayout.CENTER);
    }
    
    // Create registration panel
    private void createRegisterPanel() {
        registerPanel = new JPanel(new BorderLayout());
        registerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // North panel for title
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Register New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Center panel for registration form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel fullNameLabel = new JLabel("Full Name:");
        JTextField fullNameField = new JTextField(20);
        
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);
        
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField(20);
        
        JLabel accountTypeLabel = new JLabel("Account Type:");
        String[] accountTypes = {"Savings", "Checking"};
        JComboBox<String> accountTypeComboBox = new JComboBox<>(accountTypes);
        
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");
        
        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(fullNameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(fullNameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(confirmPasswordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        formPanel.add(confirmPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(accountTypeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        formPanel.add(accountTypeComboBox, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add action listeners
        registerButton.addActionListener(e -> {
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String accountType = (String) accountTypeComboBox.getSelectedItem();
            
            // Validate input
            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please fill in all fields", 
                                             "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(mainFrame, "Passwords do not match", 
                                             "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Register user
            if (registerUser(fullName, email, username, password, accountType)) {
                JOptionPane.showMessageDialog(mainFrame, "Registration successful! Please login.", 
                                             "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(contentPanel, "LOGIN");
                
                // Clear fields
                fullNameField.setText("");
                emailField.setText("");
                usernameField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
            }
        });
        
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "LOGIN"));
        
        // Add panels to register panel
        registerPanel.add(titlePanel, BorderLayout.NORTH);
        registerPanel.add(formPanel, BorderLayout.CENTER);
    }
    
    // Create dashboard panel
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // North panel for title and logout
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            currentUserId = -1;
            currentUsername = "";
            cardLayout.show(contentPanel, "LOGIN");
        });
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(logoutPanel, BorderLayout.EAST);
        
        // Center panel for account information
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, User!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Account summary panel
        JPanel accountSummaryPanel = new JPanel();
        accountSummaryPanel.setLayout(new BoxLayout(accountSummaryPanel, BoxLayout.Y_AXIS));
        accountSummaryPanel.setBorder(BorderFactory.createTitledBorder("Account Summary"));
        
        // This will be populated when user logs in
        JPanel accountsPanel = new JPanel();
        accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));
        
        JScrollPane accountsScrollPane = new JScrollPane(accountsPanel);
        accountsScrollPane.setPreferredSize(new Dimension(400, 200));
        
        accountSummaryPanel.add(accountsScrollPane);
        
        // Button panel for actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton transferButton = new JButton("Transfer Funds");
        JButton historyButton = new JButton("Transaction History");
        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        
        actionPanel.add(transferButton);
        actionPanel.add(historyButton);
        actionPanel.add(depositButton);
        actionPanel.add(withdrawButton);
        
        // Add action listeners
        transferButton.addActionListener(e -> {
            updateTransferPanel();
            cardLayout.show(contentPanel, "TRANSFER");
        });
        
        historyButton.addActionListener(e -> {
            updateTransactionHistory();
            cardLayout.show(contentPanel, "HISTORY");
        });
        
        depositButton.addActionListener(e -> {
            // Show deposit dialog
            showDepositDialog();
        });
        
        withdrawButton.addActionListener(e -> {
            // Show withdraw dialog
            showWithdrawDialog();
        });
        
        // Add components to center panel
        centerPanel.add(welcomeLabel, BorderLayout.NORTH);
        centerPanel.add(accountSummaryPanel, BorderLayout.CENTER);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Add panels to dashboard panel
        dashboardPanel.add(titlePanel, BorderLayout.NORTH);
        dashboardPanel.add(centerPanel, BorderLayout.CENTER);
    }
    
    // Create transfer panel
    private void createTransferPanel() {
        transferPanel = new JPanel(new BorderLayout());
        transferPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // North panel for title
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Transfer Funds");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "DASHBOARD"));
        
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backButton);
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(backPanel, BorderLayout.WEST);
        
        // Center panel for transfer form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel fromAccountLabel = new JLabel("From Account:");
        JComboBox<String> fromAccountComboBox = new JComboBox<>();
        
        JLabel toAccountLabel = new JLabel("To Account:");
        JTextField toAccountField = new JTextField(20);
        
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(20);
        
        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField(20);
        
        JButton transferButton = new JButton("Transfer");
        
        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(fromAccountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(fromAccountComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(toAccountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(toAccountField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(amountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(amountField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(descriptionLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        formPanel.add(descriptionField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(transferButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Add action listener for transfer button
        transferButton.addActionListener(e -> {
            if (fromAccountComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(mainFrame, "Please select a source account", 
                                             "Transfer Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String fromAccountStr = (String) fromAccountComboBox.getSelectedItem();
            String toAccountStr = toAccountField.getText();
            String amountStr = amountField.getText();
            String description = descriptionField.getText();
            
            if (toAccountStr.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Please fill in all required fields", 
                                             "Transfer Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(mainFrame, "Amount must be greater than zero", 
                                                 "Transfer Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Extract account number from combo box selection
                String fromAccount = fromAccountStr.split(" - ")[0];
                
                // Perform transfer
                if (transferFunds(fromAccount, toAccountStr, amount, description)) {
                    JOptionPane.showMessageDialog(mainFrame, "Transfer successful!", 
                                                 "Transfer Success", JOptionPane.INFORMATION_MESSAGE);
                    // Clear fields
                    toAccountField.setText("");
                    amountField.setText("");
                    descriptionField.setText("");
                    
                    // Refresh dashboard
                    loadUserDashboard();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter a valid amount", 
                                             "Transfer Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add panels to transfer panel
        transferPanel.add(titlePanel, BorderLayout.NORTH);
        transferPanel.add(formPanel, BorderLayout.CENTER);
    }
    
    // Create transaction history panel
    private void createHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // North panel for title
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> cardLayout.show(contentPanel, "DASHBOARD"));
        
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backPanel.add(backButton);
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(backPanel, BorderLayout.WEST);
        
        // Center panel for transaction table
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Create table model
        String[] columnNames = {"Date", "Type", "Description", "Amount", "Balance"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add panels to history panel
        historyPanel.add(titlePanel, BorderLayout.NORTH);
        historyPanel.add(tablePanel, BorderLayout.CENTER);
    }
    
    // Authenticate user
    private boolean authenticateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentUserId = rs.getInt("id");
                        currentUsername = username;
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), 
                                         "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    // Register new user
    private boolean registerUser(String fullName, String email, String username, String password, String accountType) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Insert user
                String userSql = "INSERT INTO users (username, password, full_name, email) VALUES (?, ?, ?, ?)";
                int userId;
                
                try (PreparedStatement pstmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    pstmt.setString(3, fullName);
                    pstmt.setString(4, email);
                    
                    pstmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            userId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating user failed, no ID obtained.");
                        }
                    }
                }
                
                // Generate account number
                String accountNumber = generateAccountNumber();
                
                // Insert account
                String accountSql = "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, accountNumber);
                    pstmt.setString(3, accountType);
                    pstmt.setDouble(4, 1000.00); // Initial balance of $1000
                    
                    pstmt.executeUpdate();
                }
                
                // Commit transaction
                conn.commit();
                return true;
            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                e.printStackTrace();
                
                if (e.getMessage().contains("Unique index or primary key violation")) {
                    JOptionPane.showMessageDialog(mainFrame, "Username or email already exists", 
                                                 "Registration Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Registration error: " + e.getMessage(), 
                                                 "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), 
                                         "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    // Generate random account number
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        // Generate 10-digit account number
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
    
    // Load user dashboard
    private void loadUserDashboard() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Get user's full name
            String userSql = "SELECT full_name FROM users WHERE id = ?";
            String fullName = "";
            
            try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        fullName = rs.getString("full_name");
                    }
                }
            }
            
            // Update welcome message
            JLabel welcomeLabel = (JLabel) ((JPanel) ((BorderLayout) dashboardPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponent(0);
            welcomeLabel.setText("Welcome, " + fullName + "!");
            
            // Get user's accounts
            String accountSql = "SELECT id, account_number, account_type, balance FROM accounts WHERE user_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    // Get accounts panel
                    JPanel centerPanel = (JPanel) ((BorderLayout) dashboardPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    JPanel accountSummaryPanel = (JPanel) centerPanel.getComponent(1);
                    JScrollPane accountsScrollPane = (JScrollPane) accountSummaryPanel.getComponent(0);
                    JPanel accountsPanel = (JPanel) accountsScrollPane.getViewport().getView();
                    
                    // Clear accounts panel
                    accountsPanel.removeAll();
                    
                    // Add accounts to panel
                    while (rs.next()) {
                        int accountId = rs.getInt("id");
                        String accountNumber = rs.getString("account_number");
                        String accountType = rs.getString("account_type");
                        double balance = rs.getDouble("balance");
                        
                        // Create account panel
                        JPanel accountPanel = new JPanel(new BorderLayout());
                        accountPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(5, 5, 5, 5),
                            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
                        ));
                        
                        // Account info
                        JPanel accountInfoPanel = new JPanel(new GridLayout(3, 1));
                        accountInfoPanel.add(new JLabel("Account: " + accountNumber));
                        accountInfoPanel.add(new JLabel("Type: " + accountType));
                        
                        // Format balance with 2 decimal places
                        String balanceStr = String.format("$%.2f", balance);
                        JLabel balanceLabel = new JLabel("Balance: " + balanceStr);
                        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
                        accountInfoPanel.add(balanceLabel);
                        
                        accountPanel.add(accountInfoPanel, BorderLayout.CENTER);
                        
                        // Add account panel to accounts panel
                        accountsPanel.add(accountPanel);
                        accountsPanel.add(Box.createVerticalStrut(10));
                    }
                    
                    // Refresh panel
                    accountsPanel.revalidate();
                    accountsPanel.repaint();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error loading dashboard: " + e.getMessage(), 
                                         "Dashboard Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Update transfer panel with user's accounts
    private void updateTransferPanel() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Get user's accounts
            String sql = "SELECT account_number, account_type, balance FROM accounts WHERE user_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    // Get from account combo box
                    JPanel formPanel = (JPanel) ((BorderLayout) transferPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
JComboBox<String> fromAccountComboBox = (JComboBox<String>) formPanel.getComponent(1);

                    
                    // Clear combo box
                    fromAccountComboBox.removeAllItems();
                    
                    // Add accounts to combo box
                    while (rs.next()) {
                        String accountNumber = rs.getString("account_number");
                        String accountType = rs.getString("account_type");
                        double balance = rs.getDouble("balance");
                        
                        String item = accountNumber + " - " + accountType + " - $" + String.format("%.2f", balance);
                        fromAccountComboBox.addItem(item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error loading accounts: " + e.getMessage(), 
                                         "Transfer Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Update transaction history
    private void updateTransactionHistory() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Get user's accounts
            String accountSql = "SELECT id, account_number FROM accounts WHERE user_id = ?";
            Map<Integer, String> accountMap = new HashMap<>();
            
            try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        accountMap.put(rs.getInt("id"), rs.getString("account_number"));
                    }
                }
            }
            
            if (accountMap.isEmpty()) {
                return;
            }
            
            // Get transaction history
            StringBuilder transactionSql = new StringBuilder();
            transactionSql.append("SELECT t.transaction_date, t.transaction_type, t.description, t.amount, ");
            transactionSql.append("a_from.account_number as from_account, a_to.account_number as to_account ");
            transactionSql.append("FROM transactions t ");
            transactionSql.append("LEFT JOIN accounts a_from ON t.from_account_id = a_from.id ");
            transactionSql.append("LEFT JOIN accounts a_to ON t.to_account_id = a_to.id ");
            transactionSql.append("WHERE a_from.user_id = ? OR a_to.user_id = ? ");
            transactionSql.append("ORDER BY t.transaction_date DESC");
            
            try (PreparedStatement pstmt = conn.prepareStatement(transactionSql.toString())) {
                pstmt.setInt(1, currentUserId);
                pstmt.setInt(2, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    // Get transaction table
                    JPanel tablePanel = (JPanel) ((BorderLayout) historyPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    JScrollPane scrollPane = (JScrollPane) tablePanel.getComponent(0);
                    JTable transactionTable = (JTable) scrollPane.getViewport().getView();
                    DefaultTableModel tableModel = (DefaultTableModel) transactionTable.getModel();
                    
                    // Clear table
                    tableModel.setRowCount(0);
                    
                    // Add transactions to table
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    
                    while (rs.next()) {
                        String date = dateFormat.format(rs.getTimestamp("transaction_date"));
                        String type = rs.getString("transaction_type");
                        String description = rs.getString("description");
                        double amount = rs.getDouble("amount");
                        String fromAccount = rs.getString("from_account");
                        String toAccount = rs.getString("to_account");
                        
                        // Format description
                        if (description == null || description.isEmpty()) {
                            if (type.equals("TRANSFER")) {
                                description = "Transfer from " + fromAccount + " to " + toAccount;
                            } else if (type.equals("DEPOSIT")) {
                                description = "Deposit to " + toAccount;
                            } else if (type.equals("WITHDRAWAL")) {
                                description = "Withdrawal from " + fromAccount;
                            }
                        }
                        
                        // Format amount
                        String amountStr;
                        if (type.equals("WITHDRAWAL") || 
                            (type.equals("TRANSFER") && accountMap.containsValue(fromAccount))) {
                            amountStr = "-$" + String.format("%.2f", amount);
                        } else {
                            amountStr = "+$" + String.format("%.2f", amount);
                        }
                        
                        // Add row to table
                        tableModel.addRow(new Object[]{date, type, description, amountStr, ""});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Error loading transaction history: " + e.getMessage(), 
                                         "History Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Transfer funds between accounts
    private boolean transferFunds(String fromAccount, String toAccount, double amount, String description) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Get from account ID and balance
                String fromSql = "SELECT id, balance FROM accounts WHERE account_number = ? AND user_id = ?";
                int fromAccountId = -1;
                double fromBalance = 0;
                
                try (PreparedStatement pstmt = conn.prepareStatement(fromSql)) {
                    pstmt.setString(1, fromAccount);
                    pstmt.setInt(2, currentUserId);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            fromAccountId = rs.getInt("id");
                            fromBalance = rs.getDouble("balance");
                        } else {
                            throw new SQLException("Source account not found");
                        }
                    }
                }
                
                // Check if balance is sufficient
                if (fromBalance < amount) {
                    throw new SQLException("Insufficient funds");
                }
                
                // Get to account ID
                String toSql = "SELECT id FROM accounts WHERE account_number = ?";
                int toAccountId = -1;
                
                try (PreparedStatement pstmt = conn.prepareStatement(toSql)) {
                    pstmt.setString(1, toAccount);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            toAccountId = rs.getInt("id");
                        } else {
                            throw new SQLException("Destination account not found");
                        }
                    }
                }
                
                // Update from account balance
                String updateFromSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateFromSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, fromAccountId);
                    pstmt.executeUpdate();
                }
                
                // Update to account balance
                String updateToSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateToSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, toAccountId);
                    pstmt.executeUpdate();
                }
                
                // Record transaction
                String transactionSql = "INSERT INTO transactions (from_account_id, to_account_id, amount, transaction_type, description) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(transactionSql)) {
                    pstmt.setInt(1, fromAccountId);
                    pstmt.setInt(2, toAccountId);
                    pstmt.setDouble(3, amount);
                    pstmt.setString(4, "TRANSFER");
                    pstmt.setString(5, description);
                    pstmt.executeUpdate();
                }
                
                // Commit transaction
                conn.commit();
                return true;
            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Transfer error: " + e.getMessage(), 
                                             "Transfer Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), 
                                         "Transfer Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    // Show deposit dialog
    private void showDepositDialog() {
        // Create dialog
        JDialog depositDialog = new JDialog(mainFrame, "Deposit", true);
        depositDialog.setSize(400, 250);
        depositDialog.setLocationRelativeTo(mainFrame);
        depositDialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel accountLabel = new JLabel("Account:");
        JComboBox<String> accountComboBox = new JComboBox<>();
        
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(20);
        
        JButton depositButton = new JButton("Deposit");
        JButton cancelButton = new JButton("Cancel");
        
        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(accountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(accountComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(amountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(amountField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(depositButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Load user accounts
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT account_number, account_type FROM accounts WHERE user_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String accountNumber = rs.getString("account_number");
                        String accountType = rs.getString("account_type");
                        
                        accountComboBox.addItem(accountNumber + " - " + accountType);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Add action listeners
        depositButton.addActionListener(e -> {
            if (accountComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(depositDialog, "Please select an account", 
                                             "Deposit Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String accountStr = (String) accountComboBox.getSelectedItem();
            String amountStr = amountField.getText();
            
            if (amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(depositDialog, "Please enter an amount", 
                                             "Deposit Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(depositDialog, "Amount must be greater than zero", 
                                                 "Deposit Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Extract account number from combo box selection
                String accountNumber = accountStr.split(" - ")[0];
                
                // Perform deposit
                if (deposit(accountNumber, amount)) {
                    JOptionPane.showMessageDialog(depositDialog, "Deposit successful!", 
                                                 "Deposit Success", JOptionPane.INFORMATION_MESSAGE);
                    depositDialog.dispose();
                    
                    // Refresh dashboard
                    loadUserDashboard();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(depositDialog, "Please enter a valid amount", 
                                             "Deposit Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> depositDialog.dispose());
        
        // Add form panel to dialog
        depositDialog.add(formPanel, BorderLayout.CENTER);
        
        // Show dialog
        depositDialog.setVisible(true);
    }
    
    // Show withdraw dialog
    private void showWithdrawDialog() {
        // Create dialog
        JDialog withdrawDialog = new JDialog(mainFrame, "Withdraw", true);
        withdrawDialog.setSize(400, 250);
        withdrawDialog.setLocationRelativeTo(mainFrame);
        withdrawDialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JLabel accountLabel = new JLabel("Account:");
        JComboBox<String> accountComboBox = new JComboBox<>();
        
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField(20);
        
        JButton withdrawButton = new JButton("Withdraw");
        JButton cancelButton = new JButton("Cancel");
        
        // Add components to form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(accountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(accountComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(amountLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(amountField, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(withdrawButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);
        
        // Load user accounts with balances
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT account_number, account_type, balance FROM accounts WHERE user_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUserId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String accountNumber = rs.getString("account_number");
                        String accountType = rs.getString("account_type");
                        double balance = rs.getDouble("balance");
                        
                        accountComboBox.addItem(accountNumber + " - " + accountType + " - $" + String.format("%.2f", balance));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Add action listeners
        withdrawButton.addActionListener(e -> {
            if (accountComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(withdrawDialog, "Please select an account", 
                                             "Withdraw Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String accountStr = (String) accountComboBox.getSelectedItem();
            String amountStr = amountField.getText();
            
            if (amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(withdrawDialog, "Please enter an amount", 
                                             "Withdraw Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(withdrawDialog, "Amount must be greater than zero", 
                                                 "Withdraw Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Extract account number from combo box selection
                String accountNumber = accountStr.split(" - ")[0];
                
                // Perform withdrawal
                if (withdraw(accountNumber, amount)) {
                    JOptionPane.showMessageDialog(withdrawDialog, "Withdrawal successful!", 
                                                 "Withdraw Success", JOptionPane.INFORMATION_MESSAGE);
                    withdrawDialog.dispose();
                    
                    // Refresh dashboard
                    loadUserDashboard();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(withdrawDialog, "Please enter a valid amount", 
                                             "Withdraw Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> withdrawDialog.dispose());
        
        // Add form panel to dialog
        withdrawDialog.add(formPanel, BorderLayout.CENTER);
        
        // Show dialog
        withdrawDialog.setVisible(true);
    }
    
    // Deposit funds
    private boolean deposit(String accountNumber, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Get account ID
                String accountSql = "SELECT id FROM accounts WHERE account_number = ? AND user_id = ?";
                int accountId = -1;
                
                try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                    pstmt.setString(1, accountNumber);
                    pstmt.setInt(2, currentUserId);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            accountId = rs.getInt("id");
                        } else {
                            throw new SQLException("Account not found");
                        }
                    }
                }
                
                // Update account balance
                String updateSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, accountId);
                    pstmt.executeUpdate();
                }
                
                // Record transaction
                String transactionSql = "INSERT INTO transactions (to_account_id, amount, transaction_type, description) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(transactionSql)) {
                    pstmt.setInt(1, accountId);
                    pstmt.setDouble(2, amount);
                    pstmt.setString(3, "DEPOSIT");
                    pstmt.setString(4, "Deposit to account");
                    pstmt.executeUpdate();
                }
                
                // Commit transaction
                conn.commit();
                return true;
            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Deposit error: " + e.getMessage(), 
                                             "Deposit Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), 
                                         "Deposit Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    // Withdraw funds
    private boolean withdraw(String accountNumber, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Get account ID and balance
                String accountSql = "SELECT id, balance FROM accounts WHERE account_number = ? AND user_id = ?";
                int accountId = -1;
                double balance = 0;
                
                try (PreparedStatement pstmt = conn.prepareStatement(accountSql)) {
                    pstmt.setString(1, accountNumber);
                    pstmt.setInt(2, currentUserId);
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            accountId = rs.getInt("id");
                            balance = rs.getDouble("balance");
                        } else {
                            throw new SQLException("Account not found");
                        }
                    }
                }
                
                // Check if balance is sufficient
                if (balance < amount) {
                    throw new SQLException("Insufficient funds");
                }
                
                // Update account balance
                String updateSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setInt(2, accountId);
                    pstmt.executeUpdate();
                }
                
                // Record transaction
                String transactionSql = "INSERT INTO transactions (from_account_id, amount, transaction_type, description) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(transactionSql)) {
                    pstmt.setInt(1, accountId);
                    pstmt.setDouble(2, amount);
                    pstmt.setString(3, "WITHDRAWAL");
                    pstmt.setString(4, "Withdrawal from account");
                    pstmt.executeUpdate();
                }
                
                // Commit transaction
                conn.commit();
                return true;
            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Withdrawal error: " + e.getMessage(), 
                                             "Withdrawal Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + e.getMessage(), 
                                         "Withdrawal Error", JOptionPane.ERROR_MESSAGE);
             return false;
        }
        
        


// To run this application:
// 1. Save this file as OnlineBankingSystem.java
// 2. Compile with: javac -cp ".;h2-2.1.214.jar" OnlineBankingSystem.java (Windows)
//    or: javac -cp ".:h2-2.1.214.jar" OnlineBankingSystem.java (Linux/Mac)
// 3. Run with: java -cp ".;h2-2.1.214.jar" OnlineBankingSystem (Windows)
//    or: java -cp ".:h2-2.1.214.jar" OnlineBankingSystem (Linux/Mac)
// Note: You need to download the H2 database JAR file from https://h2database.com/

// For demonstration purposes:
 System.out.println("This code creates a complete Java Online Banking System with:");
 System.out.println("1. User registration and login");
 System.out.println("2. Account management");
 System.out.println("3. Fund transfers between accounts");
 System.out.println("4. Deposits and withdrawals");
 System.out.println("5. Transaction history");
 System.out.println("6. Embedded H2 database for data storage");
 System.out.println("");
 System.out.println("To run this application, save it as OnlineBankingSystem.java and follow the instructions in the comments.");
    }

}
