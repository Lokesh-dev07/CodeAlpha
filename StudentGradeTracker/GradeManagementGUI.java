import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

class Student {
    private String name;
    private int id;
    private ArrayList<Double> grades;
    
    public Student(String name, int id) {
        this.name = name;
        this.id = id;
        this.grades = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
    
    public void addGrade(double grade) {
        grades.add(grade);
    }
    
    public double calculateAverage() {
        if (grades.isEmpty()) return 0.0;
        
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        return sum / grades.size();
    }
    
    public double getHighestGrade() {
        if (grades.isEmpty()) return 0.0;
        
        double highest = grades.get(0);
        for (double grade : grades) {
            if (grade > highest) {
                highest = grade;
            }
        }
        return highest;
    }
    
    public double getLowestGrade() {
        if (grades.isEmpty()) return 0.0;
        
        double lowest = grades.get(0);
        for (double grade : grades) {
            if (grade < lowest) {
                lowest = grade;
            }
        }
        return lowest;
    }
    
    public String getGradeLetter() {
        double avg = calculateAverage();
        if (avg >= 90) return "A";
        else if (avg >= 80) return "B";
        else if (avg >= 70) return "C";
        else if (avg >= 60) return "D";
        else return "F";
    }
    
    public ArrayList<Double> getGrades() {
        return new ArrayList<>(grades);
    }
    
    public String getGradesAsString() {
        return grades.toString();
    }
    
    public int getGradeCount() {
        return grades.size();
    }
}

public class GradeManagementGUI extends JFrame {
    private ArrayList<Student> students;
    private DefaultTableModel tableModel;
    private JTable studentTable;
    private JTextArea detailsArea;
    private JLabel statsLabel;
    
    public GradeManagementGUI() {
        students = new ArrayList<>();
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Student Grade Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Set default font for better visibility
        setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 12));
        
        // Create menu bar
        createMenuBar();
        
        // Create main panels with better colors
        createStudentTable();
        createControlPanel();
        createDetailsPanel();
        createStatisticsPanel();
        
        // Set background color
        getContentPane().setBackground(new Color(240, 240, 240));
        
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Method to set font for all components
    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(70, 130, 180));
        menuBar.setForeground(Color.WHITE);
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(Color.WHITE);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Student Grade Management System\nVersion 1.0\n\nFeatures:\n• Add/Remove Students\n• Manage Grades\n• View Statistics\n• Generate Reports",
            "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }
    
    private void createStudentTable() {
        String[] columns = {"ID", "Name", "Average", "Highest", "Lowest", "# Grades", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setRowHeight(30);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentTable.setGridColor(new Color(200, 200, 200));
        studentTable.setShowGrid(true);
        
        // Table header styling
        studentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        studentTable.getTableHeader().setBackground(new Color(70, 130, 180));
        studentTable.getTableHeader().setForeground(Color.WHITE);
        studentTable.getTableHeader().setReorderingAllowed(false);
        
        // Table cell styling
        studentTable.setBackground(Color.WHITE);
        studentTable.setForeground(Color.BLACK);
        studentTable.setSelectionBackground(new Color(173, 216, 230));
        studentTable.setSelectionForeground(Color.BLACK);
        
        // Alternate row colors for better readability
        studentTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(245, 245, 245));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                // Color code the grade column
                if (column == 6 && value != null) {
                    String grade = value.toString();
                    switch (grade) {
                        case "A": c.setForeground(new Color(0, 128, 0)); break; // Green
                        case "B": c.setForeground(new Color(0, 0, 255)); break; // Blue
                        case "C": c.setForeground(new Color(255, 165, 0)); break; // Orange
                        case "D": c.setForeground(new Color(255, 140, 0)); break; // Dark Orange
                        case "F": c.setForeground(Color.RED); break;
                        default: c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }
                
                // Center align numeric columns
                if (column == 0 || column == 2 || column == 3 || column == 4 || column == 5) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                return c;
            }
        });
        
        // Add row sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);
        
        // Add selection listener
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedStudent();
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Students List",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(70, 130, 180)
        ));
        tableScrollPane.setBackground(Color.WHITE);
        
        add(tableScrollPane, BorderLayout.CENTER);
    }
    
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        controlPanel.setBackground(new Color(240, 240, 240));
        
        // Define button colors with better contrast
        Color[] buttonColors = {
            new Color(46, 139, 87),   // Green
            new Color(30, 144, 255),  // Blue
            new Color(220, 20, 60),   // Red
            new Color(255, 165, 0),   // Orange
            new Color(147, 112, 219), // Purple
            new Color(128, 128, 128), // Gray
            new Color(0, 191, 255),   // Cyan
            new Color(255, 105, 180)  // Pink
        };
        
        String[] buttonTexts = {
            "Add Student", "Add Grades", "Remove Student", "Refresh",
            "Class Statistics", "Clear All", "Export Report", "Add Sample"
        };
        
        for (int i = 0; i < buttonTexts.length; i++) {
            JButton button = createStyledButton(buttonTexts[i], buttonColors[i]);
            controlPanel.add(button);
        }
        
        // Add action listeners
        Component[] components = controlPanel.getComponents();
        ((JButton) components[0]).addActionListener(e -> addStudentDialog());
        ((JButton) components[1]).addActionListener(e -> addGradesDialog());
        ((JButton) components[2]).addActionListener(e -> removeStudent());
        ((JButton) components[3]).addActionListener(e -> refreshTable());
        ((JButton) components[4]).addActionListener(e -> showStatistics());
        ((JButton) components[5]).addActionListener(e -> clearAllData());
        ((JButton) components[6]).addActionListener(e -> exportReport());
        ((JButton) components[7]).addActionListener(e -> addSampleStudent());
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void createDetailsPanel() {
        detailsArea = new JTextArea(10, 35);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        detailsArea.setBackground(Color.WHITE);
        detailsArea.setForeground(Color.BLACK);
        detailsArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        detailsArea.setTabSize(4);
        
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Student Details",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(70, 130, 180)
        ));
        
        add(detailsScrollPane, BorderLayout.EAST);
    }
    
    private void createStatisticsPanel() {
        statsLabel = new JLabel("Total Students: 0 | Class Average: 0.00");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(70, 130, 180)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setOpaque(true);
        statsLabel.setBackground(new Color(220, 230, 240));
        statsLabel.setForeground(new Color(0, 0, 139));
        
        add(statsLabel, BorderLayout.SOUTH);
    }
    
    // ... [Keep all the dialog and action methods from previous version]
    // These methods remain the same:
    // addStudentDialog(), addGradesDialog(), removeStudent(), refreshTable(),
    // displaySelectedStudent(), updateStatistics(), showStatistics(),
    // clearAllData(), exportReport(), addSampleStudent(), loadSampleData()
    
    // I'll include them but shortened for brevity - you should keep the full implementations
    
    private void addStudentDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(240, 240, 240));
        
        JTextField nameField = new JTextField(15);
        JTextField idField = new JTextField(10);
        
        panel.add(createLabel("Student Name:"));
        panel.add(nameField);
        panel.add(createLabel("Student ID:"));
        panel.add(idField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Add New Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int id = Integer.parseInt(idField.getText().trim());
                
                if (name.isEmpty()) {
                    showError("Name cannot be empty!");
                    return;
                }
                
                for (Student s : students) {
                    if (s.getId() == id) {
                        showError("Student ID already exists!");
                        return;
                    }
                }
                
                Student student = new Student(name, id);
                students.add(student);
                refreshTable();
                updateStatistics();
                
                showSuccess("Student added successfully!");
                    
            } catch (NumberFormatException e) {
                showError("Invalid ID! Please enter a number.");
            }
        }
    }
    
    private void addGradesDialog() {
        if (students.isEmpty()) {
            showError("No students available!");
            return;
        }
        
        String[] studentNames = new String[students.size()];
        for (int i = 0; i < students.size(); i++) {
            studentNames[i] = students.get(i).getId() + " - " + students.get(i).getName();
        }
        
        JComboBox<String> studentCombo = new JComboBox<>(studentNames);
        JTextField gradeField = new JTextField(10);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(new Color(240, 240, 240));
        panel.add(createLabel("Select Student:"));
        panel.add(studentCombo);
        panel.add(createLabel("Enter Grade (0-100):"));
        panel.add(gradeField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Add Grade", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int selectedIndex = studentCombo.getSelectedIndex();
                double grade = Double.parseDouble(gradeField.getText().trim());
                
                if (grade < 0 || grade > 100) {
                    showError("Grade must be between 0 and 100!");
                    return;
                }
                
                Student student = students.get(selectedIndex);
                student.addGrade(grade);
                refreshTable();
                updateStatistics();
                
                showSuccess("Grade added successfully!");
                    
            } catch (NumberFormatException e) {
                showError("Invalid grade! Please enter a number.");
            }
        }
    }
    
    private void removeStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a student to remove!");
            return;
        }
        
        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        int studentId = (int) tableModel.getValueAt(modelRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove this student?", 
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            students.removeIf(s -> s.getId() == studentId);
            refreshTable();
            updateStatistics();
            detailsArea.setText("");
            
            showSuccess("Student removed successfully!");
        }
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        for (Student student : students) {
            Object[] row = {
                student.getId(),
                student.getName(),
                String.format("%.2f", student.calculateAverage()),
                String.format("%.2f", student.getHighestGrade()),
                String.format("%.2f", student.getLowestGrade()),
                student.getGradeCount(),
                student.getGradeLetter()
            };
            tableModel.addRow(row);
        }
    }
    
    private void displaySelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            detailsArea.setText("");
            return;
        }
        
        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        int studentId = (int) tableModel.getValueAt(modelRow, 0);
        
        for (Student student : students) {
            if (student.getId() == studentId) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════\n");
                details.append("   STUDENT DETAILS\n");
                details.append("═══════════════════════\n\n");
                details.append("• ID: ").append(student.getId()).append("\n");
                details.append("• Name: ").append(student.getName()).append("\n");
                details.append("• Average: ").append(String.format("%.2f", student.calculateAverage())).append("\n");
                details.append("• Highest Grade: ").append(String.format("%.2f", student.getHighestGrade())).append("\n");
                details.append("• Lowest Grade: ").append(String.format("%.2f", student.getLowestGrade())).append("\n");
                details.append("• Letter Grade: ").append(student.getGradeLetter()).append("\n");
                details.append("• Number of Grades: ").append(student.getGradeCount()).append("\n\n");
                details.append("═══════════════════════\n");
                details.append("     ALL GRADES\n");
                details.append("═══════════════════════\n");
                
                ArrayList<Double> grades = student.getGrades();
                if (grades.isEmpty()) {
                    details.append("\n  No grades entered yet.\n");
                } else {
                    for (int i = 0; i < grades.size(); i++) {
                        details.append(String.format("\n  Grade %d: %8.2f", i+1, grades.get(i)));
                    }
                }
                
                detailsArea.setText(details.toString());
                break;
            }
        }
    }
    
    private void updateStatistics() {
        if (students.isEmpty()) {
            statsLabel.setText("Total Students: 0 | Class Average: 0.00");
            return;
        }
        
        double totalAverage = 0;
        for (Student student : students) {
            totalAverage += student.calculateAverage();
        }
        double classAverage = totalAverage / students.size();
        
        statsLabel.setText(String.format("Total Students: %d | Class Average: %.2f", 
            students.size(), classAverage));
    }
    
    private void showStatistics() {
        if (students.isEmpty()) {
            showError("No students available!");
            return;
        }
        
        double classTotal = 0;
        double highestInClass = Double.MIN_VALUE;
        double lowestInClass = Double.MAX_VALUE;
        String topStudent = "";
        int totalGrades = 0;
        
        int[] gradeCategories = new int[5]; // A, B, C, D, F
        
        for (Student student : students) {
            double avg = student.calculateAverage();
            classTotal += avg;
            totalGrades += student.getGradeCount();
            
            if (avg > highestInClass) {
                highestInClass = avg;
                topStudent = student.getName();
            }
            
            if (avg < lowestInClass) {
                lowestInClass = avg;
            }
            
            if (avg >= 90) gradeCategories[0]++;
            else if (avg >= 80) gradeCategories[1]++;
            else if (avg >= 70) gradeCategories[2]++;
            else if (avg >= 60) gradeCategories[3]++;
            else gradeCategories[4]++;
        }
        
        double classAverage = classTotal / students.size();
        
        StringBuilder stats = new StringBuilder();
        stats.append("═══════════════════════\n");
        stats.append("   CLASS STATISTICS\n");
        stats.append("═══════════════════════\n\n");
        stats.append("• Number of Students: ").append(students.size()).append("\n");
        stats.append("• Total Grades Entered: ").append(totalGrades).append("\n");
        stats.append("• Class Average: ").append(String.format("%.2f", classAverage)).append("\n");
        stats.append("• Highest Average: ").append(String.format("%.2f", highestInClass))
             .append(" (").append(topStudent).append(")\n");
        stats.append("• Lowest Average: ").append(String.format("%.2f", lowestInClass)).append("\n\n");
        
        stats.append("═══════════════════════\n");
        stats.append("   GRADE DISTRIBUTION\n");
        stats.append("═══════════════════════\n");
        stats.append("• A (90-100): ").append(gradeCategories[0]).append(" students\n");
        stats.append("• B (80-89):  ").append(gradeCategories[1]).append(" students\n");
        stats.append("• C (70-79):  ").append(gradeCategories[2]).append(" students\n");
        stats.append("• D (60-69):  ").append(gradeCategories[3]).append(" students\n");
        stats.append("• F (0-59):   ").append(gradeCategories[4]).append(" students\n");
        
        JTextArea statsArea = new JTextArea(stats.toString());
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        statsArea.setBackground(new Color(245, 245, 245));
        statsArea.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Class Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear ALL data?\nThis action cannot be undone!", 
            "Confirm Clear All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            students.clear();
            refreshTable();
            updateStatistics();
            detailsArea.setText("");
            showSuccess("All data cleared successfully!");
        }
    }
    
    private void exportReport() {
        if (students.isEmpty()) {
            showError("No data to export!");
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("══════════════════════════════════════════════\n");
        report.append("    STUDENT GRADE MANAGEMENT SYSTEM - REPORT\n");
        report.append("══════════════════════════════════════════════\n\n");
        report.append("Generated on: ").append(new java.util.Date()).append("\n\n");
        
        report.append("STUDENTS LIST:\n");
        report.append("══════════════════════════════════════════════════════════════════════════════════\n");
        report.append(String.format("%-8s %-20s %-10s %-10s %-10s %-8s %-6s\n", 
            "ID", "Name", "Average", "Highest", "Lowest", "#Grades", "Grade"));
        report.append("══════════════════════════════════════════════════════════════════════════════════\n");
        
        for (Student student : students) {
            report.append(String.format("%-8d %-20s %-10.2f %-10.2f %-10.2f %-8d %-6s\n",
                student.getId(), student.getName(), student.calculateAverage(),
                student.getHighestGrade(), student.getLowestGrade(),
                student.getGradeCount(), student.getGradeLetter()));
        }
        
        report.append("\n\nDETAILED VIEW:\n");
        report.append("══════════════════════════════════════════════\n");
        for (Student student : students) {
            report.append("\n").append(student.getName()).append(" (ID: ").append(student.getId()).append(")\n");
            report.append("Average: ").append(String.format("%.2f", student.calculateAverage()))
                 .append(" | Grade: ").append(student.getGradeLetter()).append("\n");
            report.append("Grades: ").append(student.getGradesAsString()).append("\n");
        }
        
        JTextArea reportArea = new JTextArea(report.toString());
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportArea.setBackground(Color.WHITE);
        reportArea.setForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(650, 450));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Export Report", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void addSampleStudent() {
        String[] names = {"Alex Johnson", "Maria Garcia", "David Smith", "Lisa Wong", "Kevin Brown"};
        int startId = 2000 + students.size();
        
        Student sample = new Student(names[students.size() % names.length], startId);
        sample.addGrade(75 + Math.random() * 25);
        sample.addGrade(80 + Math.random() * 20);
        sample.addGrade(85 + Math.random() * 15);
        
        students.add(sample);
        refreshTable();
        updateStatistics();
        
        showSuccess("Sample student added successfully!");
    }
    
    // Helper methods for better dialog visibility
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.BLACK);
        return label;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Try different look and feels for better visibility
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            // Set system colors for better contrast
            UIManager.put("Panel.background", new Color(240, 240, 240));
            UIManager.put("OptionPane.background", new Color(240, 240, 240));
            UIManager.put("OptionPane.messageForeground", Color.BLACK);
            
            new GradeManagementGUI();
        });
    }
}