// StockTradingApp.java - Corrected Version
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class StockTradingApp extends JFrame {
    // Inner classes
    static class Stock {
        private String symbol;
        private String name;
        private double currentPrice;
        private double dailyChange;
        private int volume;
        private String sector;
        private double marketCap;
        private java.util.List<Double> priceHistory;
        
        public Stock(String symbol, String name, double price, String sector, 
                     double marketCap) {
            this.symbol = symbol;
            this.name = name;
            this.currentPrice = price;
            this.sector = sector;
            this.marketCap = marketCap;
            this.dailyChange = 0.0;
            this.volume = 1000000;
            this.priceHistory = new ArrayList<>();
            this.priceHistory.add(price);
        }
        
        public void updatePrice() {
            double volatility = 0.015;
            double changePercent = (Math.random() * 2 * volatility) - volatility;
            this.currentPrice *= (1 + changePercent);
            this.currentPrice = Math.round(this.currentPrice * 100.0) / 100.0;
            this.dailyChange = Math.round(changePercent * 10000.0) / 100.0;
            this.volume = (int)(Math.random() * 1000000) + 10000;
            priceHistory.add(this.currentPrice);
        }
        
        public String getSymbol() { return symbol; }
        public String getName() { return name; }
        public double getCurrentPrice() { return currentPrice; }
        public double getDailyChange() { return dailyChange; }
        public int getVolume() { return volume; }
        public String getSector() { return sector; }
        public double getMarketCap() { return marketCap; }
        public java.util.List<Double> getPriceHistory() { return priceHistory; }
        
        public String getFormattedPrice() {
            return String.format("$%.2f", currentPrice);
        }
        
        public String getFormattedChange() {
            return String.format("%+.2f%%", dailyChange);
        }
        
        public String getFormattedVolume() {
            return String.format("%,d", volume);
        }
    }

    static class Transaction {
        private int id;
        private String symbol;
        private String type;
        private int quantity;
        private double price;
        private Date timestamp;
        private double totalAmount;
        
        public Transaction(int id, String symbol, String type, int quantity, 
                          double price, double totalAmount) {
            this.id = id;
            this.symbol = symbol;
            this.type = type;
            this.quantity = quantity;
            this.price = price;
            this.timestamp = new Date();
            this.totalAmount = totalAmount;
        }
        
        public String getFormattedString() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return String.format("%s - %s %d %s @ $%.2f ($%.2f)",
                               sdf.format(timestamp), type, quantity, 
                               symbol, price, totalAmount);
        }
        
        public String getSymbol() { return symbol; }
        public String getType() { return type; }
        public int getQuantity() { return quantity; }
        public double getTotalAmount() { return totalAmount; }
    }

    static class Portfolio {
        private String userId;
        private double balance;
        private Map<String, Integer> holdings;
        private java.util.List<Transaction> transactions;
        private int nextTransactionId;
        
        public Portfolio(String userId, double initialBalance) {
            this.userId = userId;
            this.balance = initialBalance;
            this.holdings = new HashMap<>();
            this.transactions = new ArrayList<>();
            this.nextTransactionId = 1;
        }
        
        public boolean buyStock(String symbol, int quantity, double price) {
            double totalCost = quantity * price;
            
            if (totalCost > balance) {
                return false;
            }
            
            balance -= totalCost;
            holdings.put(symbol, holdings.getOrDefault(symbol, 0) + quantity);
            
            Transaction transaction = new Transaction(nextTransactionId++, 
                symbol, "BUY", quantity, price, totalCost);
            transactions.add(transaction);
            
            saveToFile();
            return true;
        }
        
        public boolean sellStock(String symbol, int quantity, double price) {
            if (!holdings.containsKey(symbol) || holdings.get(symbol) < quantity) {
                return false;
            }
            
            double totalValue = quantity * price;
            balance += totalValue;
            
            int newQuantity = holdings.get(symbol) - quantity;
            if (newQuantity == 0) {
                holdings.remove(symbol);
            } else {
                holdings.put(symbol, newQuantity);
            }
            
            Transaction transaction = new Transaction(nextTransactionId++, 
                symbol, "SELL", quantity, price, totalValue);
            transactions.add(transaction);
            
            saveToFile();
            return true;
        }
        
        public double getPortfolioValue(Map<String, Stock> marketData) {
            double holdingsValue = 0;
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String symbol = entry.getKey();
                int quantity = entry.getValue();
                if (marketData.containsKey(symbol)) {
                    holdingsValue += quantity * marketData.get(symbol).getCurrentPrice();
                }
            }
            return balance + holdingsValue;
        }
        
        public Map<String, Double> getHoldingsValue(Map<String, Stock> marketData) {
            Map<String, Double> holdingsValue = new HashMap<>();
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String symbol = entry.getKey();
                int quantity = entry.getValue();
                if (marketData.containsKey(symbol)) {
                    holdingsValue.put(symbol, quantity * marketData.get(symbol).getCurrentPrice());
                }
            }
            return holdingsValue;
        }
        
        private void saveToFile() {
            try {
                Properties props = new Properties();
                props.setProperty("balance", String.valueOf(balance));
                props.setProperty("nextTransactionId", String.valueOf(nextTransactionId));
                
                // Save holdings
                StringBuilder holdingsStr = new StringBuilder();
                for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                    holdingsStr.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
                }
                props.setProperty("holdings", holdingsStr.toString());
                
                // Save transactions
                StringBuilder transStr = new StringBuilder();
                for (Transaction t : transactions) {
                    transStr.append(t.getSymbol()).append(",")
                            .append(t.getType()).append(",")
                            .append(t.getQuantity()).append(",")
                            .append(t.getTotalAmount()).append(";");
                }
                props.setProperty("transactions", transStr.toString());
                
                props.store(new FileOutputStream("portfolio.properties"), "Portfolio Data");
            } catch (IOException e) {
                System.err.println("Error saving portfolio: " + e.getMessage());
            }
        }
        
        public static Portfolio loadFromFile(String userId) {
            File file = new File("portfolio.properties");
            if (!file.exists()) {
                return new Portfolio(userId, 10000.00);
            }
            
            try {
                Properties props = new Properties();
                props.load(new FileInputStream("portfolio.properties"));
                
                double balance = Double.parseDouble(props.getProperty("balance", "10000.0"));
                Portfolio portfolio = new Portfolio(userId, balance);
                portfolio.nextTransactionId = Integer.parseInt(props.getProperty("nextTransactionId", "1"));
                
                // Load holdings
                String holdingsStr = props.getProperty("holdings", "");
                if (!holdingsStr.isEmpty()) {
                    String[] holdingsArr = holdingsStr.split(";");
                    for (String holding : holdingsArr) {
                        if (!holding.isEmpty()) {
                            String[] parts = holding.split(":");
                            if (parts.length == 2) {
                                portfolio.holdings.put(parts[0], Integer.parseInt(parts[1]));
                            }
                        }
                    }
                }
                
                return portfolio;
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading portfolio: " + e.getMessage());
                return new Portfolio(userId, 10000.00);
            }
        }
        
        public double getBalance() { return balance; }
        public Map<String, Integer> getHoldings() { return holdings; }
        public java.util.List<Transaction> getTransactions() { return transactions; }
    }

    static class MarketData {
        private Map<String, Stock> stocks;
        private boolean marketOpen;
        private java.util.Timer timer;
        
        public MarketData() {
            stocks = new LinkedHashMap<>();
            marketOpen = true;
            initializeStocks();
        }
        
        private void initializeStocks() {
            stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 175.25, "Technology", 2.7e12));
            stocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 138.75, "Technology", 1.7e12));
            stocks.put("MSFT", new Stock("MSFT", "Microsoft Corp.", 330.45, "Technology", 2.5e12));
            stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 210.30, "Automotive", 650e9));
            stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 145.80, "E-commerce", 1.5e12));
            stocks.put("JPM", new Stock("JPM", "JPMorgan Chase", 155.60, "Finance", 450e9));
            stocks.put("NVDA", new Stock("NVDA", "NVIDIA Corp.", 485.25, "Technology", 1.2e12));
            stocks.put("META", new Stock("META", "Meta Platforms", 320.10, "Technology", 820e9));
            stocks.put("V", new Stock("V", "Visa Inc.", 240.75, "Finance", 500e9));
            stocks.put("JNJ", new Stock("JNJ", "Johnson & Johnson", 155.90, "Healthcare", 380e9));
        }
        
        public void startMarketUpdates(Runnable updateCallback) {
            timer = new java.util.Timer();
            timer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    if (marketOpen) {
                        updateMarket();
                        SwingUtilities.invokeLater(updateCallback);
                    }
                }
            }, 0, 2000);
        }
        
        private void updateMarket() {
            for (Stock stock : stocks.values()) {
                stock.updatePrice();
            }
        }
        
        public boolean toggleMarket() {
            marketOpen = !marketOpen;
            return marketOpen;
        }
        
        public void saveMarketData() {
            try (PrintWriter writer = new PrintWriter("market_data.csv")) {
                writer.println("Symbol,Name,Price,Change%,Volume,Sector,MarketCap");
                for (Stock stock : stocks.values()) {
                    writer.printf("%s,%s,%.2f,%.2f,%d,%s,%.0f%n",
                        stock.getSymbol(), stock.getName(), stock.getCurrentPrice(),
                        stock.getDailyChange(), stock.getVolume(), 
                        stock.getSector(), stock.getMarketCap());
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving market data: " + e.getMessage());
            }
        }
        
        public Map<String, Stock> getStocks() { return stocks; }
        public boolean isMarketOpen() { return marketOpen; }
        public java.util.List<Stock> getAllStocks() { return new ArrayList<>(stocks.values()); }
    }

    // GUI Components
    private MarketData marketData;
    private Portfolio portfolio;
    
    private JTable marketTable;
    private DefaultTableModel marketTableModel;
    private JTable holdingsTable;
    private DefaultTableModel holdingsTableModel;
    private JTextArea transactionsArea;
    private JLabel balanceLabel;
    private JLabel portfolioValueLabel;
    private JLabel marketStatusLabel;
    private JTextField symbolField;
    private JTextField quantityField;
    private JLabel stockInfoLabel;
    
    private String[] marketColumns = {"Symbol", "Name", "Price", "Change%", "Volume", "Sector"};
    private String[] holdingsColumns = {"Symbol", "Quantity", "Price", "Value"};
    
    public StockTradingApp() {
        marketData = new MarketData();
        portfolio = Portfolio.loadFromFile("default_user");
        
        initializeUI();
        marketData.startMarketUpdates(this::updateDisplay);
        updateDisplay();
    }
    
    private void initializeUI() {
        setTitle("Stock Trading Simulator - Swing Version");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null); // Center window
        
        // Create main layout
        setLayout(new BorderLayout());
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Market Data");
        exportItem.addActionListener(e -> exportMarketData());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        JMenu marketMenu = new JMenu("Market");
        JMenuItem toggleMarketItem = new JMenuItem("Toggle Market");
        toggleMarketItem.addActionListener(e -> toggleMarket());
        marketMenu.add(toggleMarketItem);
        
        menuBar.add(fileMenu);
        menuBar.add(marketMenu);
        setJMenuBar(menuBar);
        
        // Create main panels
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(800);
        
        // Left panel - Market Data
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(createMarketPanel(), BorderLayout.CENTER);
        leftPanel.add(createTradingPanel(), BorderLayout.SOUTH);
        
        // Right panel - Portfolio
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createPortfolioPanel(), BorderLayout.CENTER);
        
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightPanel);
        
        add(mainSplitPane, BorderLayout.CENTER);
        
        // Setup table selection listener
        marketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = marketTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String symbol = (String) marketTable.getValueAt(selectedRow, 0);
                    updateStockInfo(symbol);
                }
            }
        });
    }
    
    private JPanel createMarketPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Market Data"));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> updateMarketTable());
        
        JButton toggleButton = new JButton("Toggle Market");
        toggleButton.addActionListener(e -> toggleMarket());
        
        marketStatusLabel = new JLabel("Market: OPEN");
        marketStatusLabel.setForeground(Color.GREEN);
        marketStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        controlPanel.add(refreshButton);
        controlPanel.add(toggleButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(marketStatusLabel);
        
        // Market table
        marketTableModel = new DefaultTableModel(marketColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        marketTable = new JTable(marketTableModel);
        marketTable.setRowHeight(25);
        marketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add scrollbar
        JScrollPane scrollPane = new JScrollPane(marketTable);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createTradingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Trading"));
        panel.setPreferredSize(new Dimension(0, 100));
        
        // Trading controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        controlPanel.add(new JLabel("Symbol:"));
        symbolField = new JTextField(8);
        symbolField.setPreferredSize(new Dimension(80, 25));
        controlPanel.add(symbolField);
        
        controlPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField("1", 8);
        quantityField.setPreferredSize(new Dimension(80, 25));
        controlPanel.add(quantityField);
        
        JButton buyButton = new JButton("Buy");
        buyButton.setBackground(new Color(76, 175, 80));
        buyButton.setForeground(Color.WHITE);
        buyButton.addActionListener(e -> buyStock());
        
        JButton sellButton = new JButton("Sell");
        sellButton.setBackground(new Color(244, 67, 54));
        sellButton.setForeground(Color.WHITE);
        sellButton.addActionListener(e -> sellStock());
        
        controlPanel.add(buyButton);
        controlPanel.add(sellButton);
        
        // Stock info
        stockInfoLabel = new JLabel("Select a stock from the market table");
        stockInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(stockInfoLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPortfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Portfolio"));
        
        // Portfolio summary
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        balanceLabel = new JLabel("Balance: $10,000.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        portfolioValueLabel = new JLabel("Portfolio Value: $10,000.00");
        portfolioValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        summaryPanel.add(balanceLabel);
        summaryPanel.add(portfolioValueLabel);
        
        // Holdings table
        JPanel holdingsPanel = new JPanel(new BorderLayout());
        holdingsPanel.setBorder(BorderFactory.createTitledBorder("Holdings"));
        
        holdingsTableModel = new DefaultTableModel(holdingsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        
        holdingsTable = new JTable(holdingsTableModel);
        holdingsTable.setRowHeight(25);
        JScrollPane holdingsScroll = new JScrollPane(holdingsTable);
        holdingsPanel.add(holdingsScroll, BorderLayout.CENTER);
        
        // Transactions area
        JPanel transactionsPanel = new JPanel(new BorderLayout());
        transactionsPanel.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        
        transactionsArea = new JTextArea(6, 25);
        transactionsArea.setEditable(false);
        transactionsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane transScroll = new JScrollPane(transactionsArea);
        transactionsPanel.add(transScroll, BorderLayout.CENTER);
        
        // Layout all portfolio components
        JPanel mainPortfolioPanel = new JPanel(new BorderLayout());
        mainPortfolioPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPortfolioPanel.add(holdingsPanel, BorderLayout.CENTER);
        mainPortfolioPanel.add(transactionsPanel, BorderLayout.SOUTH);
        
        panel.add(mainPortfolioPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateDisplay() {
        updateMarketTable();
        updatePortfolioDisplay();
    }
    
    private void updateMarketTable() {
        if (marketTableModel == null) return;
        
        SwingUtilities.invokeLater(() -> {
            marketTableModel.setRowCount(0);
            for (Stock stock : marketData.getAllStocks()) {
                Object[] row = {
                    stock.getSymbol(),
                    stock.getName(),
                    stock.getFormattedPrice(),
                    stock.getFormattedChange(),
                    stock.getFormattedVolume(),
                    stock.getSector()
                };
                marketTableModel.addRow(row);
            }
            
            // Apply color coding to change column
            for (int i = 0; i < marketTableModel.getRowCount(); i++) {
                String change = (String) marketTableModel.getValueAt(i, 3);
                if (change != null) {
                    if (change.startsWith("+")) {
                        marketTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable table, Object value,
                                    boolean isSelected, boolean hasFocus, int row, int column) {
                                Component c = super.getTableCellRendererComponent(table, value, 
                                    isSelected, hasFocus, row, column);
                                if (column == 3) {
                                    String val = (String) value;
                                    if (val != null && val.startsWith("+")) {
                                        c.setForeground(Color.GREEN);
                                    } else if (val != null && val.startsWith("-")) {
                                        c.setForeground(Color.RED);
                                    }
                                }
                                return c;
                            }
                        });
                    }
                }
            }
        });
    }
    
    private void updatePortfolioDisplay() {
        SwingUtilities.invokeLater(() -> {
            // Update summary
            double portfolioValue = portfolio.getPortfolioValue(marketData.getStocks());
            
            balanceLabel.setText(String.format("Balance: $%,.2f", portfolio.getBalance()));
            portfolioValueLabel.setText(String.format("Portfolio Value: $%,.2f", portfolioValue));
            
            // Update holdings table
            holdingsTableModel.setRowCount(0);
            Map<String, Double> holdingsValue = portfolio.getHoldingsValue(marketData.getStocks());
            
            for (Map.Entry<String, Integer> entry : portfolio.getHoldings().entrySet()) {
                String symbol = entry.getKey();
                int quantity = entry.getValue();
                
                if (marketData.getStocks().containsKey(symbol)) {
                    double price = marketData.getStocks().get(symbol).getCurrentPrice();
                    double value = holdingsValue.getOrDefault(symbol, 0.0);
                    
                    Object[] row = {
                        symbol,
                        String.valueOf(quantity),
                        String.format("$%.2f", price),
                        String.format("$%,.2f", value)
                    };
                    holdingsTableModel.addRow(row);
                }
            }
            
            // Update transactions
            transactionsArea.setText("");
            java.util.List<Transaction> transactions = portfolio.getTransactions();
            int start = Math.max(0, transactions.size() - 5);
            
            for (int i = Math.max(0, transactions.size() - 1); i >= start; i--) {
                transactionsArea.append(transactions.get(i).getFormattedString() + "\n");
            }
        });
    }
    
    private void updateStockInfo(String symbol) {
        if (marketData.getStocks().containsKey(symbol)) {
            Stock stock = marketData.getStocks().get(symbol);
            symbolField.setText(symbol);
            
            String info = String.format("<html>%s (%s) - %s (%s)<br>Sector: %s | Market Cap: $%.1fB</html>",
                stock.getName(), stock.getSymbol(), stock.getFormattedPrice(),
                stock.getFormattedChange(), stock.getSector(), stock.getMarketCap() / 1e9);
            stockInfoLabel.setText(info);
        }
    }
    
    private void buyStock() {
        String symbol = symbolField.getText().trim().toUpperCase();
        int quantity;
        
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!marketData.getStocks().containsKey(symbol)) {
            JOptionPane.showMessageDialog(this, "Invalid stock symbol", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be positive", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Stock stock = marketData.getStocks().get(symbol);
        double totalCost = quantity * stock.getCurrentPrice();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Buy %d shares of %s at %s?\nTotal: $%,.2f", 
                quantity, symbol, stock.getFormattedPrice(), totalCost),
            "Confirm Buy", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = portfolio.buyStock(symbol, quantity, stock.getCurrentPrice());
            if (success) {
                JOptionPane.showMessageDialog(this, "Purchase successful!");
                updatePortfolioDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient balance", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void sellStock() {
        String symbol = symbolField.getText().trim().toUpperCase();
        int quantity;
        
        try {
            quantity = Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!portfolio.getHoldings().containsKey(symbol)) {
            JOptionPane.showMessageDialog(this, "You don't own this stock", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be positive", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int ownedQuantity = portfolio.getHoldings().get(symbol);
        if (quantity > ownedQuantity) {
            JOptionPane.showMessageDialog(this, 
                String.format("Insufficient shares. You own %d shares", ownedQuantity),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Stock stock = marketData.getStocks().get(symbol);
        double totalValue = quantity * stock.getCurrentPrice();
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Sell %d shares of %s at %s?\nTotal: $%,.2f", 
                quantity, symbol, stock.getFormattedPrice(), totalValue),
            "Confirm Sell", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = portfolio.sellStock(symbol, quantity, stock.getCurrentPrice());
            if (success) {
                JOptionPane.showMessageDialog(this, "Sale successful!");
                updatePortfolioDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Sell failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void toggleMarket() {
        boolean isOpen = marketData.toggleMarket();
        String status = isOpen ? "OPEN" : "CLOSED";
        Color color = isOpen ? Color.GREEN : Color.RED;
        
        marketStatusLabel.setText("Market: " + status);
        marketStatusLabel.setForeground(color);
        
        JOptionPane.showMessageDialog(this, "Market is now " + status);
    }
    
    private void exportMarketData() {
        marketData.saveMarketData();
        JOptionPane.showMessageDialog(this, "Market data exported to market_data.csv");
    }
    
    public static void main(String[] args) {
        // Use try-catch for better error handling
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Set system look and feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    
                    // Create and show application
                    StockTradingApp app = new StockTradingApp();
                    app.setVisible(true);
                    
                    // Add window listener to save on close
                    app.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            // Any cleanup can go here
                            System.exit(0);
                        }
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, 
                        "Error starting application: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}