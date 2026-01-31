import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class HotelBookingSystem extends JFrame {
    
    // ==================== DATA CLASSES ====================
    
    enum RoomType {
        STANDARD("Standard", 2500, "AC, Double Bed, TV, WiFi"),
        DELUXE("Deluxe", 4000, "AC, King Bed, City View"),
        SUITE("Suite", 6000, "AC, Living Area, Kitchenette");
        
        private String name;
        private int price;
        private String features;
        
        RoomType(String name, int price, String features) {
            this.name = name;
            this.price = price;
            this.features = features;
        }
        
        public String getName() { return name; }
        public int getPrice() { return price; }
        public String getFeatures() { return features; }
    }
    
    class Room {
        int number;
        RoomType type;
        boolean available;
        
        Room(int number, RoomType type) {
            this.number = number;
            this.type = type;
            this.available = true;
        }
    }
    
    class Customer {
        String name;
        String email;
        String phone;
        String id;
        
        Customer(String name, String email, String phone, String id) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.id = id;
        }
    }
    
    class Booking {
        String id;
        Customer customer;
        Room room;
        Date checkIn;
        Date checkOut;
        int persons;
        double amount;
        String status;
        
        Booking(Customer customer, Room room, Date checkIn, Date checkOut, int persons) {
            this.id = "BK" + new Random().nextInt(10000);
            this.customer = customer;
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.persons = persons;
            this.status = "Confirmed";
            calculateAmount();
        }
        
        void calculateAmount() {
            long days = (checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24);
            double base = room.type.getPrice() * days;
            this.amount = base + (base * 0.18);
        }
        
        void cancel() {
            this.status = "Cancelled";
            room.available = true;
        }
    }
    
    // ==================== DATABASE ====================
    
    class HotelDB {
        ArrayList<Room> rooms;
        ArrayList<Booking> bookings;
        String dataFile = "hotel_data.txt";
        
        HotelDB() {
            rooms = new ArrayList<>();
            bookings = new ArrayList<>();
            loadRooms();
            loadData();
        }
        
        void loadRooms() {
            rooms.add(new Room(101, RoomType.STANDARD));
            rooms.add(new Room(102, RoomType.STANDARD));
            rooms.add(new Room(103, RoomType.STANDARD));
            rooms.add(new Room(201, RoomType.DELUXE));
            rooms.add(new Room(202, RoomType.DELUXE));
            rooms.add(new Room(301, RoomType.SUITE));
        }
        
        ArrayList<Room> findAvailable(RoomType type, Date checkIn, Date checkOut) {
            ArrayList<Room> available = new ArrayList<>();
            for (Room room : rooms) {
                if (room.type == type && room.available) {
                    available.add(room);
                }
            }
            return available;
        }
        
        Booking makeBooking(Customer customer, Room room, Date checkIn, Date checkOut, int persons) {
            Booking booking = new Booking(customer, room, checkIn, checkOut, persons);
            room.available = false;
            bookings.add(booking);
            saveData();
            return booking;
        }
        
        boolean cancelBooking(String bookingId) {
            for (Booking booking : bookings) {
                if (booking.id.equals(bookingId) && booking.status.equals("Confirmed")) {
                    booking.cancel();
                    saveData();
                    return true;
                }
            }
            return false;
        }
        
        void saveData() {
            try (PrintWriter writer = new PrintWriter(dataFile)) {
                for (Booking booking : bookings) {
                    writer.println(booking.id + "," + booking.customer.name + "," + 
                                  booking.room.number + "," + booking.status);
                }
            } catch (IOException e) {
                System.out.println("Error saving data");
            }
        }
        
        void loadData() {
            File file = new File(dataFile);
            if (!file.exists()) return;
            
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(",");
                    if (parts.length == 4) {
                        String status = parts[3];
                        if (status.equals("Confirmed")) {
                            int roomNo = Integer.parseInt(parts[2]);
                            for (Room room : rooms) {
                                if (room.number == roomNo) {
                                    room.available = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading data");
            }
        }
    }
    
    // ==================== CALENDAR COMPONENT ====================
    
    class CalendarDialog extends JDialog {
        private JSpinner monthSpinner;
        private JSpinner yearSpinner;
        private JLabel monthLabel;
        private JPanel calendarPanel;
        private Date selectedDate;
        private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        
        CalendarDialog(JFrame parent, String title, Date initialDate) {
            super(parent, title, true);
            setSize(400, 400);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(10, 10));
            
            Calendar cal = Calendar.getInstance();
            if (initialDate != null) {
                cal.setTime(initialDate);
            }
            
            // Top panel for month/year selection
            JPanel topPanel = new JPanel(new FlowLayout());
            topPanel.setBackground(panelBg);
            
            String[] months = {"January", "February", "March", "April", "May", "June",
                              "July", "August", "September", "October", "November", "December"};
            
            monthSpinner = new JSpinner(new SpinnerListModel(months));
            monthSpinner.setValue(months[cal.get(Calendar.MONTH)]);
            monthSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
            topPanel.add(monthSpinner);
            
            yearSpinner = new JSpinner(new SpinnerNumberModel(
                cal.get(Calendar.YEAR), 2020, 2030, 1));
            yearSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
            topPanel.add(yearSpinner);
            
            JButton refreshBtn = new JButton("Refresh");
            refreshBtn.setBackground(buttonBg);
            refreshBtn.setForeground(Color.WHITE);
            refreshBtn.addActionListener(e -> updateCalendar());
            topPanel.add(refreshBtn);
            
            add(topPanel, BorderLayout.NORTH);
            
            // Calendar panel
            calendarPanel = new JPanel(new GridLayout(7, 7, 2, 2));
            calendarPanel.setBackground(panelBg);
            updateCalendar();
            
            add(new JScrollPane(calendarPanel), BorderLayout.CENTER);
            
            // Bottom panel with today button
            JPanel bottomPanel = new JPanel(new FlowLayout());
            bottomPanel.setBackground(panelBg);
            
            JButton todayBtn = new JButton("Today");
            todayBtn.setBackground(buttonBg);
            todayBtn.setForeground(Color.WHITE);
            todayBtn.addActionListener(e -> {
                Calendar today = Calendar.getInstance();
                monthSpinner.setValue(months[today.get(Calendar.MONTH)]);
                yearSpinner.setValue(today.get(Calendar.YEAR));
                updateCalendar();
            });
            bottomPanel.add(todayBtn);
            
            JButton okBtn = new JButton("OK");
            okBtn.setBackground(buttonBg);
            okBtn.setForeground(Color.WHITE);
            okBtn.addActionListener(e -> {
                if (selectedDate != null) {
                    dispose();
                }
            });
            bottomPanel.add(okBtn);
            
            add(bottomPanel, BorderLayout.SOUTH);
        }
        
        private void updateCalendar() {
            calendarPanel.removeAll();
            
            String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String day : days) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setForeground(Color.DARK_GRAY);
                calendarPanel.add(label);
            }
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, (Integer) yearSpinner.getValue());
            cal.set(Calendar.MONTH, getMonthIndex((String) monthSpinner.getValue()));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            
            int firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            // Fill empty cells before first day
            for (int i = 0; i < firstDay; i++) {
                calendarPanel.add(new JLabel(""));
            }
            
            // Add day buttons
            Calendar today = Calendar.getInstance();
            
            for (int day = 1; day <= daysInMonth; day++) {
                final int currentDay = day;
                JButton dayBtn = new JButton(String.valueOf(day));
                dayBtn.setFont(new Font("Arial", Font.PLAIN, 12));
                dayBtn.setBackground(panelBg);
                dayBtn.setForeground(Color.BLACK);
                
                // Check if this is today
                if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH)) {
                    dayBtn.setBackground(new Color(200, 220, 255));
                    dayBtn.setFont(new Font("Arial", Font.BOLD, 12));
                }
                
                dayBtn.addActionListener(e -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(Calendar.YEAR, (Integer) yearSpinner.getValue());
                    selectedCal.set(Calendar.MONTH, getMonthIndex((String) monthSpinner.getValue()));
                    selectedCal.set(Calendar.DAY_OF_MONTH, currentDay);
                    selectedDate = selectedCal.getTime();
                    
                    // Highlight selected date
                    for (Component comp : calendarPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            comp.setBackground(panelBg);
                            comp.setForeground(Color.BLACK);
                        }
                    }
                    dayBtn.setBackground(buttonBg);
                    dayBtn.setForeground(Color.WHITE);
                });
                
                calendarPanel.add(dayBtn);
            }
            
            calendarPanel.revalidate();
            calendarPanel.repaint();
        }
        
        private int getMonthIndex(String monthName) {
            String[] months = {"January", "February", "March", "April", "May", "June",
                              "July", "August", "September", "October", "November", "December"};
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(monthName)) {
                    return i;
                }
            }
            return 0;
        }
        
        public Date getSelectedDate() {
            return selectedDate;
        }
        
        public String getFormattedDate() {
            return selectedDate != null ? sdf.format(selectedDate) : "";
        }
    }
    
    // ==================== GUI COMPONENTS ====================
    
    private HotelDB db;
    private JTabbedPane tabPane;
    
    private JComboBox<RoomType> typeBox;
    private JTextField inDate, outDate;
    private JSpinner personsSpin;
    private JTable roomsTable;
    private DefaultTableModel roomsModel;
    
    private JTextField custName, custEmail, custPhone, custID;
    private JTextArea summary;
    
    private JTable bookingsTable;
    private DefaultTableModel bookingsModel;
    
    // Colors
    private Color mainBg = new Color(240, 248, 255);  // Light Blue
    private Color panelBg = new Color(255, 255, 255); // White
    private Color tableHeaderBg = new Color(220, 220, 220); // Light Gray
    private Color tableGrid = new Color(200, 200, 200);     // Gray
    private Color buttonBg = new Color(70, 130, 180); // Steel Blue
    
    public HotelBookingSystem() {
        db = new HotelDB();
        setupGUI();
    }
    
    private void setupGUI() {
        setTitle("Hotel Booking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Set main background
        getContentPane().setBackground(mainBg);
        
        tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Arial", Font.BOLD, 12));
        tabPane.setBackground(mainBg);
        
        tabPane.add("Search Rooms", createSearchPanel());
        tabPane.add("Book Room", createBookPanel());
        tabPane.add("Manage Bookings", createManagePanel());
        
        add(tabPane);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(mainBg);
        
        // Search criteria panel
        JPanel topPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
        topPanel.setBackground(panelBg);
        
        // All text components
        JLabel typeLabel = new JLabel("Room Type:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        typeLabel.setForeground(Color.BLACK);
        topPanel.add(typeLabel);
        
        typeBox = new JComboBox<>(RoomType.values());
        typeBox.setBackground(panelBg);
        typeBox.setForeground(Color.BLACK);
        topPanel.add(typeBox);
        
        JLabel checkInLabel = new JLabel("Check-in (dd-mm-yyyy):");
        checkInLabel.setFont(new Font("Arial", Font.BOLD, 12));
        checkInLabel.setForeground(Color.BLACK);
        topPanel.add(checkInLabel);
        
        JPanel inDatePanel = new JPanel(new BorderLayout());
        inDatePanel.setBackground(panelBg);
        inDate = new JTextField();
        inDate.setBackground(panelBg);
        inDate.setForeground(Color.BLACK);
        inDatePanel.add(inDate, BorderLayout.CENTER);
        
        JButton inCalendarBtn = new JButton("📅");
        inCalendarBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        inCalendarBtn.setBackground(buttonBg);
        inCalendarBtn.setForeground(Color.WHITE);
        inCalendarBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        inCalendarBtn.addActionListener(e -> showCalendarForCheckIn());
        inDatePanel.add(inCalendarBtn, BorderLayout.EAST);
        topPanel.add(inDatePanel);
        
        JLabel checkOutLabel = new JLabel("Check-out (dd-mm-yyyy):");
        checkOutLabel.setFont(new Font("Arial", Font.BOLD, 12));
        checkOutLabel.setForeground(Color.BLACK);
        topPanel.add(checkOutLabel);
        
        JPanel outDatePanel = new JPanel(new BorderLayout());
        outDatePanel.setBackground(panelBg);
        outDate = new JTextField();
        outDate.setBackground(panelBg);
        outDate.setForeground(Color.BLACK);
        outDatePanel.add(outDate, BorderLayout.CENTER);
        
        JButton outCalendarBtn = new JButton("📅");
        outCalendarBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        outCalendarBtn.setBackground(buttonBg);
        outCalendarBtn.setForeground(Color.WHITE);
        outCalendarBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        outCalendarBtn.addActionListener(e -> showCalendarForCheckOut());
        outDatePanel.add(outCalendarBtn, BorderLayout.EAST);
        topPanel.add(outDatePanel);
        
        JLabel personsLabel = new JLabel("Persons:");
        personsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        personsLabel.setForeground(Color.BLACK);
        topPanel.add(personsLabel);
        
        personsSpin = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        personsSpin.setBackground(panelBg);
        personsSpin.setForeground(Color.BLACK);
        topPanel.add(personsSpin);
        
        topPanel.add(new JLabel());
        
        JButton searchBtn = new JButton("Search Rooms");
        searchBtn.setBackground(buttonBg);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Arial", Font.BOLD, 12));
        searchBtn.addActionListener(e -> searchRooms());
        topPanel.add(searchBtn);
        
        // Rooms table
        String[] columns = {"Room No", "Type", "Price/Night", "Features"};
        roomsModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        roomsTable = new JTable(roomsModel);
        roomsTable.setRowHeight(25);
        roomsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        roomsTable.setForeground(Color.BLACK);
        roomsTable.setBackground(panelBg);
        roomsTable.setGridColor(tableGrid);
        
        roomsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        roomsTable.getTableHeader().setBackground(tableHeaderBg);
        roomsTable.getTableHeader().setForeground(Color.BLACK);
        
        JScrollPane scroll = new JScrollPane(roomsTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Available Rooms"));
        scroll.getViewport().setBackground(panelBg);
        
        // Book button
        JButton bookBtn = new JButton("Book Selected Room");
        bookBtn.setBackground(buttonBg);
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Arial", Font.BOLD, 12));
        bookBtn.addActionListener(e -> bookRoom());
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bookBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(mainBg);
        
        // Customer details panel
        JPanel custPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        custPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        custPanel.setBackground(panelBg);
        
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        nameLabel.setForeground(Color.BLACK);
        custPanel.add(nameLabel);
        
        custName = new JTextField();
        custName.setBackground(panelBg);
        custName.setForeground(Color.BLACK);
        custPanel.add(custName);
        
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 12));
        emailLabel.setForeground(Color.BLACK);
        custPanel.add(emailLabel);
        
        custEmail = new JTextField();
        custEmail.setBackground(panelBg);
        custEmail.setForeground(Color.BLACK);
        custPanel.add(custEmail);
        
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 12));
        phoneLabel.setForeground(Color.BLACK);
        custPanel.add(phoneLabel);
        
        custPhone = new JTextField();
        custPhone.setBackground(panelBg);
        custPhone.setForeground(Color.BLACK);
        custPanel.add(custPhone);
        
        JLabel idLabel = new JLabel("ID Proof:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 12));
        idLabel.setForeground(Color.BLACK);
        custPanel.add(idLabel);
        
        custID = new JTextField();
        custID.setBackground(panelBg);
        custID.setForeground(Color.BLACK);
        custPanel.add(custID);
        
        // Summary panel
        summary = new JTextArea(10, 40);
        summary.setEditable(false);
        summary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summary.setBackground(panelBg);
        summary.setForeground(Color.BLACK);
        JScrollPane summaryScroll = new JScrollPane(summary);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Booking Summary"));
        summaryScroll.getViewport().setBackground(panelBg);
        
        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(mainBg);
        
        JButton calcBtn = new JButton("Calculate Total");
        calcBtn.setBackground(buttonBg);
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setFont(new Font("Arial", Font.BOLD, 12));
        calcBtn.addActionListener(e -> calculateTotal());
        
        JButton confirmBtn = new JButton("Confirm Booking");
        confirmBtn.setBackground(buttonBg);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 12));
        confirmBtn.addActionListener(e -> confirmBooking());
        
        btnPanel.add(calcBtn);
        btnPanel.add(confirmBtn);
        
        panel.add(custPanel, BorderLayout.NORTH);
        panel.add(summaryScroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createManagePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(mainBg);
        
        // Bookings table
        String[] columns = {"Booking ID", "Customer", "Room", "Check-in", "Check-out", "Amount", "Status"};
        bookingsModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        bookingsTable = new JTable(bookingsModel);
        bookingsTable.setRowHeight(25);
        bookingsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookingsTable.setForeground(Color.BLACK);
        bookingsTable.setBackground(panelBg);
        bookingsTable.setGridColor(tableGrid);
        
        bookingsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        bookingsTable.getTableHeader().setBackground(tableHeaderBg);
        bookingsTable.getTableHeader().setForeground(Color.BLACK);
        
        bookingsTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
        
        JScrollPane scroll = new JScrollPane(bookingsTable);
        scroll.setBorder(BorderFactory.createTitledBorder("All Bookings"));
        scroll.getViewport().setBackground(panelBg);
        
        // Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(mainBg);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(buttonBg);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> loadBookings());
        
        JButton cancelBtn = new JButton("Cancel Booking");
        cancelBtn.setBackground(buttonBg);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 12));
        cancelBtn.addActionListener(e -> cancelBooking());
        
        btnPanel.add(refreshBtn);
        btnPanel.add(cancelBtn);
        
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        loadBookings();
        
        return panel;
    }
    
    // Custom cell renderer for status
    class StatusCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            c.setBackground(panelBg);
            c.setForeground(Color.BLACK);
            
            if (column == 6) { // Status column
                String status = (String) value;
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            }
            
            if (column == 5) { // Amount column
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            }
            
            return c;
        }
    }
    
    // ==================== BUSINESS LOGIC ====================
    
    private void showCalendarForCheckIn() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date initialDate = null;
            if (!inDate.getText().isEmpty()) {
                initialDate = sdf.parse(inDate.getText());
            }
            CalendarDialog dialog = new CalendarDialog(this, "Select Check-in Date", initialDate);
            dialog.setVisible(true);
            if (dialog.getSelectedDate() != null) {
                inDate.setText(dialog.getFormattedDate());
            }
        } catch (ParseException e) {
            CalendarDialog dialog = new CalendarDialog(this, "Select Check-in Date", null);
            dialog.setVisible(true);
            if (dialog.getSelectedDate() != null) {
                inDate.setText(dialog.getFormattedDate());
            }
        }
    }
    
    private void showCalendarForCheckOut() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date initialDate = null;
            if (!outDate.getText().isEmpty()) {
                initialDate = sdf.parse(outDate.getText());
            }
            CalendarDialog dialog = new CalendarDialog(this, "Select Check-out Date", initialDate);
            dialog.setVisible(true);
            if (dialog.getSelectedDate() != null) {
                outDate.setText(dialog.getFormattedDate());
            }
        } catch (ParseException e) {
            CalendarDialog dialog = new CalendarDialog(this, "Select Check-out Date", null);
            dialog.setVisible(true);
            if (dialog.getSelectedDate() != null) {
                outDate.setText(dialog.getFormattedDate());
            }
        }
    }
    
    private void searchRooms() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date checkIn = sdf.parse(inDate.getText());
            Date checkOut = sdf.parse(outDate.getText());
            
            if (!checkOut.after(checkIn)) {
                showError("Check-out must be after check-in");
                return;
            }
            
            RoomType type = (RoomType) typeBox.getSelectedItem();
            ArrayList<Room> rooms = db.findAvailable(type, checkIn, checkOut);
            
            roomsModel.setRowCount(0);
            
            for (Room room : rooms) {
                roomsModel.addRow(new Object[]{
                    room.number,
                    room.type.getName(),
                    "₹" + room.type.getPrice(),
                    room.type.getFeatures()
                });
            }
            
            if (roomsModel.getRowCount() == 0) {
                showMessage("No rooms available", "Info");
            }
            
        } catch (ParseException e) {
            showError("Enter dates as dd-mm-yyyy or use calendar");
        }
    }
    
    private void bookRoom() {
        int row = roomsTable.getSelectedRow();
        if (row == -1) {
            showError("Select a room first");
            return;
        }
        
        tabPane.setSelectedIndex(1);
        
        custName.setText("");
        custEmail.setText("");
        custPhone.setText("");
        custID.setText("");
        summary.setText("Selected room: " + roomsTable.getValueAt(row, 0));
    }
    
    private void calculateTotal() {
        try {
            if (inDate.getText().isEmpty() || outDate.getText().isEmpty()) {
                showError("Enter dates in Search tab first");
                return;
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date checkIn = sdf.parse(inDate.getText());
            Date checkOut = sdf.parse(outDate.getText());
            
            long days = (checkOut.getTime() - checkIn.getTime()) / (1000 * 60 * 60 * 24);
            int price = RoomType.STANDARD.getPrice();
            double total = (price * days) + ((price * days) * 0.18);
            
            summary.setText(String.format(
                "=== BOOKING SUMMARY ===\n\n" +
                "Check-in: %s\n" +
                "Check-out: %s\n" +
                "Nights: %d\n" +
                "Price per night: ₹%d\n" +
                "GST (18%%): ₹%.2f\n" +
                "Total Amount: ₹%.2f\n\n" +
                "Customer: %s\n" +
                "Email: %s\n" +
                "Phone: %s",
                sdf.format(checkIn), sdf.format(checkOut), days,
                price, (price * days) * 0.18, total,
                custName.getText(), custEmail.getText(), custPhone.getText()
            ));
            
        } catch (ParseException e) {
            showError("Invalid dates");
        }
    }
    
    private void confirmBooking() {
        try {
            if (custName.getText().isEmpty() || custEmail.getText().isEmpty()) {
                showError("Enter customer details");
                return;
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date checkIn = sdf.parse(inDate.getText());
            Date checkOut = sdf.parse(outDate.getText());
            int persons = (Integer) personsSpin.getValue();
            
            Customer customer = new Customer(
                custName.getText(),
                custEmail.getText(),
                custPhone.getText(),
                custID.getText()
            );
            
            RoomType type = (RoomType) typeBox.getSelectedItem();
            ArrayList<Room> rooms = db.findAvailable(type, checkIn, checkOut);
            
            if (rooms.isEmpty()) {
                showError("Room no longer available");
                return;
            }
            
            Room room = rooms.get(0);
            Booking booking = db.makeBooking(customer, room, checkIn, checkOut, persons);
            
            String message = String.format(
                "Booking Confirmed!\n\n" +
                "ID: %s\n" +
                "Customer: %s\n" +
                "Room: %d\n" +
                "Check-in: %s\n" +
                "Check-out: %s\n" +
                "Total: ₹%.2f\n\n" +
                "Email sent to: %s",
                booking.id, customer.name, room.number,
                sdf.format(checkIn), sdf.format(checkOut),
                booking.amount, customer.email
            );
            
            showMessage(message, "Success");
            
            custName.setText("");
            custEmail.setText("");
            custPhone.setText("");
            custID.setText("");
            summary.setText("");
            
            loadBookings();
            tabPane.setSelectedIndex(2);
            
        } catch (ParseException e) {
            showError("Invalid dates");
        }
    }
    
    private void loadBookings() {
        bookingsModel.setRowCount(0);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        
        for (Booking booking : db.bookings) {
            bookingsModel.addRow(new Object[]{
                booking.id,
                booking.customer.name,
                booking.room.number,
                sdf.format(booking.checkIn),
                sdf.format(booking.checkOut),
                String.format("₹%.2f", booking.amount),
                booking.status
            });
        }
    }
    
    private void cancelBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row == -1) {
            showError("Select a booking");
            return;
        }
        
        String bookingId = (String) bookingsTable.getValueAt(row, 0);
        String status = (String) bookingsTable.getValueAt(row, 6);
        
        if (!status.equals("Confirmed")) {
            showError("Only confirmed bookings can be cancelled");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel booking " + bookingId + "?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (db.cancelBooking(bookingId)) {
                showMessage("Booking cancelled", "Success");
                loadBookings();
            } else {
                showError("Cancellation failed");
            }
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                HotelBookingSystem app = new HotelBookingSystem();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}