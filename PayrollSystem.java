import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Employee {
    private String id;
    private String name;
    private double basicSalary;
    private String email;
    private String phone;
    private String department;

    public Employee(String id, String name, double basicSalary, String email, String phone, String department) {
        this.id = id;
        this.name = name;
        this.basicSalary = basicSalary;
        this.email = email;
        this.phone = phone;
        this.department = department;
    }

    public double calculateSalary() {
        return basicSalary + (0.2 * basicSalary) + (0.1 * basicSalary); // HRA + DA
    }

    public String generatePayslip() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        String monthYear = sdf.format(new Date());
        
        return "===== PAYSLIP =====\n" +
               "Employee ID: " + id + "\n" +
               "Name: " + name + "\n" +
               "Department: " + department + "\n" +
               "Month: " + monthYear + "\n\n" +
               "Basic Salary: ₹" + String.format("%.2f", basicSalary) + "\n" +
               "HRA (20%): ₹" + String.format("%.2f", 0.2 * basicSalary) + "\n" +
               "DA (10%): ₹" + String.format("%.2f", 0.1 * basicSalary) + "\n" +
               "-------------------\n" +
               "Total Salary: ₹" + String.format("%.2f", calculateSalary()) + "\n" +
               "===================";
    }

    public String toString() {
        return "ID: " + id + "\nName: " + name + "\nBasic Salary: ₹" + basicSalary + 
               "\nEmail: " + email + "\nPhone: " + phone + "\nDepartment: " + department +
               "\nTotal Salary: ₹" + String.format("%.2f", calculateSalary());
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getBasicSalary() { return basicSalary; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
}

public class PayrollSystem{
    private static Connection conn;
    private static String currentUserId;
    private static String currentUserRole;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            connectToDatabase();
            showWelcomeScreen();
        });
    }

    // Database connection
    private static void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/payroll_system", 
                "root", 
                "qwerty123" // Replace with your MySQL password
            );
            System.out.println("Connected to database successfully");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
        }
    }

    // Welcome screen
    private static void showWelcomeScreen() {
        JFrame frame = new JFrame("Payroll System - Corporate Solutions");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Welcome to Corporate Solutions Payroll System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        JButton managerBtn = new JButton("Manager Login");
        managerBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        managerBtn.addActionListener(e -> {
            frame.dispose();
            showLoginScreen("manager");
        });
        
        JButton employeeBtn = new JButton("Employee Login");
        employeeBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        employeeBtn.addActionListener(e -> {
            frame.dispose();
            showLoginScreen("employee");
        });
        
        buttonPanel.add(managerBtn);
        buttonPanel.add(employeeBtn);
        
        panel.add(buttonPanel, BorderLayout.CENTER);
        frame.add(panel);
        frame.setVisible(true);
    }

    // Login screen
    private static void showLoginScreen(String role) {
        JFrame frame = new JFrame(role.substring(0, 1).toUpperCase() + role.substring(1) + " Login");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(role.substring(0, 1).toUpperCase() + role.substring(1) + " Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(title);
        
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        JLabel userLabel = new JLabel("User ID:");
        JTextField userField = new JTextField();
        userPanel.add(userLabel, BorderLayout.WEST);
        userPanel.add(userField, BorderLayout.CENTER);
        panel.add(userPanel);
        
        JPanel passPanel = new JPanel(new BorderLayout(5, 5));
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        passPanel.add(passLabel, BorderLayout.WEST);
        passPanel.add(passField, BorderLayout.CENTER);
        panel.add(passPanel);
        
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            String userId = userField.getText();
            String password = new String(passField.getPassword());
            
            if (authenticateUser(userId, password, role)) {
                currentUserId = userId;
                currentUserRole = role;
                frame.dispose();
                if (role.equals("manager")) {
                    showManagerDashboard();
                } else {
                    showEmployeeDashboard();
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(loginBtn);
        
        JButton forgotPassBtn = new JButton("Forgot Password?");
        forgotPassBtn.addActionListener(e -> showForgotPasswordScreen(role));
        panel.add(forgotPassBtn);
        
        frame.add(panel);
        frame.setVisible(true);
    }

    // Authentication
    private static boolean authenticateUser(String userId, String password, String role) {
        try {
            String query = "SELECT * FROM users WHERE user_id = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, userId);
            stmt.setString(2, password);
            stmt.setString(3, role);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Forgot password screen
    private static void showForgotPasswordScreen(String role) {
        JFrame frame = new JFrame("Password Recovery");
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Password Recovery", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(title);
        
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        JLabel userLabel = new JLabel("User ID:");
        JTextField userField = new JTextField();
        userPanel.add(userLabel, BorderLayout.WEST);
        userPanel.add(userField, BorderLayout.CENTER);
        panel.add(userPanel);
        
        JPanel answerPanel = new JPanel(new BorderLayout(5, 5));
        JLabel answerLabel = new JLabel();
        JTextField answerField = new JTextField();
        
        userField.addActionListener(e -> {
            try {
                String query = "SELECT security_question FROM users WHERE user_id = ? AND role = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, userField.getText());
                stmt.setString(2, role);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    answerLabel.setText(rs.getString("security_question"));
                    answerPanel.add(answerLabel, BorderLayout.WEST);
                    answerPanel.add(answerField, BorderLayout.CENTER);
                    panel.revalidate();
                    panel.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "User ID not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        
        panel.add(answerPanel);
        
        JButton recoverBtn = new JButton("Recover Password");
        recoverBtn.addActionListener(e -> {
            try {
                String query = "SELECT password FROM users WHERE user_id = ? AND security_answer = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, userField.getText());
                stmt.setString(2, answerField.getText());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    JOptionPane.showMessageDialog(frame, "Your password is: " + rs.getString("password"));
                    frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Incorrect security answer", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(recoverBtn);
        
        frame.add(panel);
        frame.setVisible(true);
    }

    // Manager Dashboard
    private static void showManagerDashboard() {
        JFrame frame = new JFrame("Manager Dashboard");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Employee Management Tab
        JPanel employeePanel = new JPanel(new BorderLayout());
        JTextArea employeeTextArea = new JTextArea();
        employeeTextArea.setEditable(false);
        JScrollPane employeeScrollPane = new JScrollPane(employeeTextArea);
        
        JPanel employeeButtonPanel = new JPanel(new GridLayout(1, 6, 5, 5));
        
        JButton viewBtn = new JButton("View All");
        viewBtn.addActionListener(e -> displayAllEmployees(employeeTextArea));
        
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addEmployee());
        
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editEmployee());
        
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteEmployee());
        
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchEmployee(employeeTextArea));
        
        JButton exportBtn = new JButton("Export to CSV");
        exportBtn.addActionListener(e -> exportToCSV());
        
        employeeButtonPanel.add(viewBtn);
        employeeButtonPanel.add(addBtn);
        employeeButtonPanel.add(editBtn);
        employeeButtonPanel.add(deleteBtn);
        employeeButtonPanel.add(searchBtn);
        employeeButtonPanel.add(exportBtn);
        
        employeePanel.add(employeeScrollPane, BorderLayout.CENTER);
        employeePanel.add(employeeButtonPanel, BorderLayout.SOUTH);
        
        // Salary Payment Tab
        JPanel salaryPanel = new JPanel(new BorderLayout());
        JTextArea salaryTextArea = new JTextArea();
        salaryTextArea.setEditable(false);
        JScrollPane salaryScrollPane = new JScrollPane(salaryTextArea);
        
        JPanel salaryButtonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        
        JButton paySalaryBtn = new JButton("Pay Salaries");
        paySalaryBtn.addActionListener(e -> paySalaries(salaryTextArea));
        
        JButton viewPaymentsBtn = new JButton("View Payments");
        viewPaymentsBtn.addActionListener(e -> viewSalaryPayments(salaryTextArea));
        
        JButton failedPaymentsBtn = new JButton("View Failed Payments");
        failedPaymentsBtn.addActionListener(e -> viewFailedPayments(salaryTextArea));
        
        salaryButtonPanel.add(paySalaryBtn);
        salaryButtonPanel.add(viewPaymentsBtn);
        salaryButtonPanel.add(failedPaymentsBtn);
        
        salaryPanel.add(salaryScrollPane, BorderLayout.CENTER);
        salaryPanel.add(salaryButtonPanel, BorderLayout.SOUTH);
        
        // Leave Management Tab
        JPanel leavePanel = new JPanel(new BorderLayout());
        JTextArea leaveTextArea = new JTextArea();
        leaveTextArea.setEditable(false);
        JScrollPane leaveScrollPane = new JScrollPane(leaveTextArea);
        
        JPanel leaveButtonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        
        JButton viewLeavesBtn = new JButton("View All Leaves");
        viewLeavesBtn.addActionListener(e -> viewAllLeaves(leaveTextArea));
        
        JButton pendingLeavesBtn = new JButton("Pending Approvals");
        pendingLeavesBtn.addActionListener(e -> viewPendingLeaves(leaveTextArea));
        
        JButton approveLeaveBtn = new JButton("Approve/Reject");
        approveLeaveBtn.addActionListener(e -> approveRejectLeave());
        
        leaveButtonPanel.add(viewLeavesBtn);
        leaveButtonPanel.add(pendingLeavesBtn);
        leaveButtonPanel.add(approveLeaveBtn);
        
        leavePanel.add(leaveScrollPane, BorderLayout.CENTER);
        leavePanel.add(leaveButtonPanel, BorderLayout.SOUTH);
        
        // Settings Tab
        JPanel settingsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton changePassBtn = new JButton("Change My Password");
        changePassBtn.addActionListener(e -> changePassword());
        
        JButton resetEmpPassBtn = new JButton("Reset Employee Password");
        resetEmpPassBtn.addActionListener(e -> resetEmployeePassword());
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            showWelcomeScreen();
        });
        
        settingsPanel.add(changePassBtn);
        settingsPanel.add(resetEmpPassBtn);
        settingsPanel.add(logoutBtn);
        
        tabbedPane.addTab("Employee Management", employeePanel);
        tabbedPane.addTab("Salary Payments", salaryPanel);
        tabbedPane.addTab("Leave Management", leavePanel);
        tabbedPane.addTab("Settings", settingsPanel);
        
        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    // Employee Dashboard
    private static void showEmployeeDashboard() {
        JFrame frame = new JFrame("Employee Dashboard");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Profile Tab
        JPanel profilePanel = new JPanel(new BorderLayout());
        JTextArea profileTextArea = new JTextArea();
        profileTextArea.setEditable(false);
        JScrollPane profileScrollPane = new JScrollPane(profileTextArea);
        
        JButton refreshProfileBtn = new JButton("Refresh Profile");
        refreshProfileBtn.addActionListener(e -> displayEmployeeProfile(profileTextArea, currentUserId));
        
        profilePanel.add(profileScrollPane, BorderLayout.CENTER);
        profilePanel.add(refreshProfileBtn, BorderLayout.SOUTH);
        
        // Salary Tab
        JPanel salaryPanel = new JPanel(new BorderLayout());
        JTextArea salaryTextArea = new JTextArea();
        salaryTextArea.setEditable(false);
        JScrollPane salaryScrollPane = new JScrollPane(salaryTextArea);
        
        JPanel salaryButtonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        
        JButton viewSalaryBtn = new JButton("View Salary");
        viewSalaryBtn.addActionListener(e -> displayEmployeeSalary(salaryTextArea, currentUserId));
        
        JButton downloadPayslipBtn = new JButton("Download Payslip");
        downloadPayslipBtn.addActionListener(e -> downloadPayslip(currentUserId));
        
        salaryButtonPanel.add(viewSalaryBtn);
        salaryButtonPanel.add(downloadPayslipBtn);
        
        salaryPanel.add(salaryScrollPane, BorderLayout.CENTER);
        salaryPanel.add(salaryButtonPanel, BorderLayout.SOUTH);
        
        // Leave Tab
        JPanel leavePanel = new JPanel(new BorderLayout());
        JTextArea leaveTextArea = new JTextArea();
        leaveTextArea.setEditable(false);
        JScrollPane leaveScrollPane = new JScrollPane(leaveTextArea);
        
        JPanel leaveButtonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        
        JButton viewLeavesBtn = new JButton("View My Leaves");
        viewLeavesBtn.addActionListener(e -> viewEmployeeLeaves(leaveTextArea, currentUserId));
        
        JButton applyLeaveBtn = new JButton("Apply for Leave");
        applyLeaveBtn.addActionListener(e -> applyForLeave());
        
        leaveButtonPanel.add(viewLeavesBtn);
        leaveButtonPanel.add(applyLeaveBtn);
        
        leavePanel.add(leaveScrollPane, BorderLayout.CENTER);
        leavePanel.add(leaveButtonPanel, BorderLayout.SOUTH);
        
        // Settings Tab
        JPanel settingsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton changePassBtn = new JButton("Change Password");
        changePassBtn.addActionListener(e -> changePassword());
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            showWelcomeScreen();
        });
        
        settingsPanel.add(changePassBtn);
        settingsPanel.add(logoutBtn);
        
        tabbedPane.addTab("My Profile", profilePanel);
        tabbedPane.addTab("Salary", salaryPanel);
        tabbedPane.addTab("Leave", leavePanel);
        tabbedPane.addTab("Settings", settingsPanel);
        
        frame.add(tabbedPane);
        frame.setVisible(true);
        
        // Display profile immediately
        displayEmployeeProfile(profileTextArea, currentUserId);
    }

    // Employee Management Methods
    private static void displayAllEmployees(JTextArea textArea) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM employees");
            
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDouble("basic_salary"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department")
                );
                sb.append(emp.toString()).append("\n-------------------\n");
            }
            
            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching employee data: " + e.getMessage());
        }
    }

    private static void addEmployee() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField salaryField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField deptField = new JTextField();
        
        panel.add(new JLabel("Employee ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Basic Salary:"));
        panel.add(salaryField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Department:"));
        panel.add(deptField);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Add Employee", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText();
                String name = nameField.getText();
                double salary = Double.parseDouble(salaryField.getText());
                String email = emailField.getText();
                String phone = phoneField.getText();
                String dept = deptField.getText();
                
                // Insert into employees table
                String empQuery = "INSERT INTO employees (id, name, basic_salary, email, phone, department) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement empStmt = conn.prepareStatement(empQuery);
                empStmt.setString(1, id);
                empStmt.setString(2, name);
                empStmt.setDouble(3, salary);
                empStmt.setString(4, email);
                empStmt.setString(5, phone);
                empStmt.setString(6, dept);
                empStmt.executeUpdate();
                
                // Insert into users table with default password
                String userQuery = "INSERT INTO users (user_id, password, role) VALUES (?, ?, 'employee')";
                PreparedStatement userStmt = conn.prepareStatement(userQuery);
                userStmt.setString(1, id);
                userStmt.setString(2, "password123"); // Default password
                userStmt.executeUpdate();
                
                JOptionPane.showMessageDialog(null, "Employee added successfully with default password: password123");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid salary format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void editEmployee() {
        String empId = JOptionPane.showInputDialog("Enter Employee ID to edit:");
        if (empId == null || empId.trim().isEmpty()) return;
        
        try {
            String query = "SELECT * FROM employees WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
                
                JTextField nameField = new JTextField(rs.getString("name"));
                JTextField salaryField = new JTextField(String.valueOf(rs.getDouble("basic_salary")));
                JTextField emailField = new JTextField(rs.getString("email"));
                JTextField phoneField = new JTextField(rs.getString("phone"));
                JTextField deptField = new JTextField(rs.getString("department"));
                
                panel.add(new JLabel("Employee ID:"));
                panel.add(new JLabel(empId));
                panel.add(new JLabel("Name:"));
                panel.add(nameField);
                panel.add(new JLabel("Basic Salary:"));
                panel.add(salaryField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);
                panel.add(new JLabel("Phone:"));
                panel.add(phoneField);
                panel.add(new JLabel("Department:"));
                panel.add(deptField);
                
                int result = JOptionPane.showConfirmDialog(null, panel, "Edit Employee", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
                if (result == JOptionPane.OK_OPTION) {
                    String updateQuery = "UPDATE employees SET name = ?, basic_salary = ?, email = ?, phone = ?, department = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, nameField.getText());
                    updateStmt.setDouble(2, Double.parseDouble(salaryField.getText()));
                    updateStmt.setString(3, emailField.getText());
                    updateStmt.setString(4, phoneField.getText());
                    updateStmt.setString(5, deptField.getText());
                    updateStmt.setString(6, empId);
                    updateStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(null, "Employee updated successfully");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid salary format", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteEmployee() {
        String empId = JOptionPane.showInputDialog("Enter Employee ID to delete:");
        if (empId == null || empId.trim().isEmpty()) return;
        
        try {
            // First delete from users table (foreign key constraint)
            String userQuery = "DELETE FROM users WHERE user_id = ?";
            PreparedStatement userStmt = conn.prepareStatement(userQuery);
            userStmt.setString(1, empId);
            int userRows = userStmt.executeUpdate();
            
            // Then delete from employees table
            String empQuery = "DELETE FROM employees WHERE id = ?";
            PreparedStatement empStmt = conn.prepareStatement(empQuery);
            empStmt.setString(1, empId);
            int empRows = empStmt.executeUpdate();
            
            if (empRows > 0) {
                JOptionPane.showMessageDialog(null, "Employee deleted successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void searchEmployee(JTextArea textArea) {
        String searchTerm = JOptionPane.showInputDialog("Enter employee name or ID to search:");
        if (searchTerm == null || searchTerm.trim().isEmpty()) return;
        
        try {
            String query = "SELECT * FROM employees WHERE id LIKE ? OR name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDouble("basic_salary"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department")
                );
                sb.append(emp.toString()).append("\n-------------------\n");
            }
            
            if (sb.length() == 0) {
                textArea.setText("No employees found matching: " + searchTerm);
            } else {
                textArea.setText(sb.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error searching employees: " + e.getMessage());
        }
    }

    private static void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Employee Data");
        fileChooser.setSelectedFile(new File("employees.csv"));
        
        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Write CSV header
                writer.write("ID,Name,Basic Salary,Email,Phone,Department\n");
                
                // Write data
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM employees");
                
                while (rs.next()) {
                    writer.write(String.format("\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\"\n",
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getDouble("basic_salary"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("department")
                    ));
                }
                
                JOptionPane.showMessageDialog(null, "Employee data exported successfully to " + fileToSave.getAbsolutePath());
            } catch (IOException | SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error exporting data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Salary Payment Methods
    private static void paySalaries(JTextArea textArea) {
        // Verify manager password for security
        String password = JOptionPane.showInputDialog("Enter your manager password to proceed:");
        if (password == null) return;
        
        try {
            String verifyQuery = "SELECT * FROM users WHERE user_id = ? AND password = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
            verifyStmt.setString(1, currentUserId);
            verifyStmt.setString(2, password);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Incorrect password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get all employees
            Statement empStmt = conn.createStatement();
            ResultSet empRs = empStmt.executeQuery("SELECT id, name, basic_salary FROM employees");
            
            // Current date for payment
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String paymentDate = sdf.format(new Date());
            
            StringBuilder sb = new StringBuilder();
            sb.append("Salary Payment Results:\n\n");
            
            // Process each employee
            while (empRs.next()) {
                String empId = empRs.getString("id");
                String empName = empRs.getString("name");
                double basicSalary = empRs.getDouble("basic_salary");
                double totalSalary = basicSalary * 1.3; // HRA + DA
                
                // Randomly determine if payment fails (10% chance for simulation)
                boolean paymentSuccess = Math.random() > 0.1;
                String status = paymentSuccess ? "paid" : "failed";
                
                // Record payment
                String paymentQuery = "INSERT INTO salary_payments (employee_id, amount, payment_date, status) VALUES (?, ?, ?, ?)";
                PreparedStatement paymentStmt = conn.prepareStatement(paymentQuery);
                paymentStmt.setString(1, empId);
                paymentStmt.setDouble(2, totalSalary);
                paymentStmt.setString(3, paymentDate);
                paymentStmt.setString(4, status);
                paymentStmt.executeUpdate();
                
                sb.append(empName).append(" (ID: ").append(empId).append("): ");
                if (paymentSuccess) {
                    sb.append("Successfully paid ₹").append(String.format("%.2f", totalSalary)).append("\n");
                } else {
                    sb.append("Payment failed due to technical issues\n");
                }
            }
            
            textArea.setText(sb.toString());
            JOptionPane.showMessageDialog(null, "Salary payment process completed");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error processing payments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void viewSalaryPayments(JTextArea textArea) {
        try {
            String query = "SELECT p.*, e.name FROM salary_payments p JOIN employees e ON p.employee_id = e.id ORDER BY p.payment_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Salary Payment History:\n\n");
            
            while (rs.next()) {
                sb.append("Payment ID: ").append(rs.getInt("payment_id")).append("\n");
                sb.append("Employee: ").append(rs.getString("name")).append(" (ID: ").append(rs.getString("employee_id")).append(")\n");
                sb.append("Amount: ₹").append(String.format("%.2f", rs.getDouble("amount"))).append("\n");
                sb.append("Date: ").append(rs.getDate("payment_date")).append("\n");
                sb.append("Status: ").append(rs.getString("status")).append("\n");
                sb.append("-------------------\n");
            }
            
            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching payment history: " + e.getMessage());
        }
    }

    private static void viewFailedPayments(JTextArea textArea) {
        try {
            String query = "SELECT p.*, e.name FROM salary_payments p JOIN employees e ON p.employee_id = e.id WHERE p.status = 'failed' ORDER BY p.payment_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Failed Salary Payments:\n\n");
            
            while (rs.next()) {
                sb.append("Payment ID: ").append(rs.getInt("payment_id")).append("\n");
                sb.append("Employee: ").append(rs.getString("name")).append(" (ID: ").append(rs.getString("employee_id")).append(")\n");
                sb.append("Amount: ₹").append(String.format("%.2f", rs.getDouble("amount"))).append("\n");
                sb.append("Date: ").append(rs.getDate("payment_date")).append("\n");
                sb.append("-------------------\n");
            }
            
            if (sb.toString().equals("Failed Salary Payments:\n\n")) {
                sb.append("No failed payments found");
            }
            
            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching failed payments: " + e.getMessage());
        }
    }

    // Leave Management Methods
    private static void viewAllLeaves(JTextArea textArea) {
        try {
            String query = "SELECT l.*, e.name FROM leave_applications l JOIN employees e ON l.employee_id = e.id ORDER BY l.start_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            StringBuilder sb = new StringBuilder();
            sb.append("All Leave Applications:\n\n");
            
            while (rs.next()) {
                sb.append("Leave ID: ").append(rs.getInt("leave_id")).append("\n");
                sb.append("Employee: ").append(rs.getString("name")).append(" (ID: ").append(rs.getString("employee_id")).append(")\n");
                sb.append("From: ").append(rs.getDate("start_date")).append(" To: ").append(rs.getDate("end_date")).append("\n");
                sb.append("Reason: ").append(rs.getString("reason")).append("\n");
                sb.append("Status: ").append(rs.getString("status")).append("\n");
                sb.append("-------------------\n");
            }
            
            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching leave applications: " + e.getMessage());
        }
    }

    private static void viewPendingLeaves(JTextArea textArea) {
        try {
            String query = "SELECT l.*, e.name FROM leave_applications l JOIN employees e ON l.employee_id = e.id WHERE l.status = 'pending' ORDER BY l.start_date";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Pending Leave Applications:\n\n");
            
            while (rs.next()) {
                sb.append("Leave ID: ").append(rs.getInt("leave_id")).append("\n");
                sb.append("Employee: ").append(rs.getString("name")).append(" (ID: ").append(rs.getString("employee_id")).append(")\n");
                sb.append("From: ").append(rs.getDate("start_date")).append(" To: ").append(rs.getDate("end_date")).append("\n");
                sb.append("Reason: ").append(rs.getString("reason")).append("\n");
                sb.append("-------------------\n");
            }
            
            if (sb.toString().equals("Pending Leave Applications:\n\n")) {
                sb.append("No pending leave applications");
            }
            
            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching pending leaves: " + e.getMessage());
        }
    }

    private static void approveRejectLeave() {
        String leaveId = JOptionPane.showInputDialog("Enter Leave ID to approve/reject:");
        if (leaveId == null || leaveId.trim().isEmpty()) return;
        
        try {
            String query = "SELECT l.*, e.name FROM leave_applications l JOIN employees e ON l.employee_id = e.id WHERE l.leave_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(leaveId));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Object[] options = {"Approve", "Reject", "Cancel"};
                int choice = JOptionPane.showOptionDialog(null, 
                    "Leave ID: " + rs.getInt("leave_id") + "\n" +
                    "Employee: " + rs.getString("name") + "\n" +
                    "From: " + rs.getDate("start_date") + " To: " + rs.getDate("end_date") + "\n" +
                    "Reason: " + rs.getString("reason") + "\n\n" +
                    "Approve or reject this leave application?",
                    "Leave Approval",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);
                
                if (choice == 0) { // Approve
                    String updateQuery = "UPDATE leave_applications SET status = 'approved' WHERE leave_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, Integer.parseInt(leaveId));
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Leave approved successfully");
                } else if (choice == 1) { // Reject
                    String updateQuery = "UPDATE leave_applications SET status = 'rejected' WHERE leave_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, Integer.parseInt(leaveId));
                    updateStmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Leave rejected");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Leave application not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Leave ID format", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Employee Profile Methods
    private static void displayEmployeeProfile(JTextArea textArea, String empId) {
        try {
            String query = "SELECT * FROM employees WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Employee emp = new Employee(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDouble("basic_salary"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department")
                );
                textArea.setText(emp.toString());
            } else {
                textArea.setText("Employee not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching employee profile: " + e.getMessage());
        }
    }

    private static void displayEmployeeSalary(JTextArea textArea, String empId) {
        try {
            String query = "SELECT * FROM employees WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Employee emp = new Employee(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDouble("basic_salary"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department")
                );
                
                // Get payment history
                String paymentQuery = "SELECT * FROM salary_payments WHERE employee_id = ? ORDER BY payment_date DESC";
                PreparedStatement paymentStmt = conn.prepareStatement(paymentQuery);
                paymentStmt.setString(1, empId);
                ResultSet paymentRs = paymentStmt.executeQuery();
                
                StringBuilder sb = new StringBuilder();
                sb.append("Salary Information:\n\n");
                sb.append("Employee: ").append(emp.getName()).append("\n");
                sb.append("Basic Salary: ₹").append(String.format("%.2f", emp.getBasicSalary())).append("\n");
                sb.append("HRA (20%): ₹").append(String.format("%.2f", 0.2 * emp.getBasicSalary())).append("\n");
                sb.append("DA (10%): ₹").append(String.format("%.2f", 0.1 * emp.getBasicSalary())).append("\n");
                sb.append("Total Salary: ₹").append(String.format("%.2f", emp.calculateSalary())).append("\n\n");
                
                sb.append("Payment History:\n");
                while (paymentRs.next()) {
                    sb.append("Date: ").append(paymentRs.getDate("payment_date")).append(" - ");
                    sb.append("Amount: ₹").append(String.format("%.2f", paymentRs.getDouble("amount"))).append(" - ");
                    sb.append("Status: ").append(paymentRs.getString("status")).append("\n");
                }
                
                textArea.setText(sb.toString());
            } else {
                textArea.setText("Employee not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching salary information: " + e.getMessage());
        }
    }

    private static void downloadPayslip(String empId) {
        try {
            String query = "SELECT * FROM employees WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Employee emp = new Employee(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getDouble("basic_salary"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department")
                );
                
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Payslip");
                fileChooser.setSelectedFile(new File(empId + "_payslip.txt"));
                
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    
                    try (FileWriter writer = new FileWriter(fileToSave)) {
                        writer.write(emp.generatePayslip());
                        JOptionPane.showMessageDialog(null, "Payslip saved to " + fileToSave.getAbsolutePath());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Error saving payslip: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating payslip: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void viewEmployeeLeaves(JTextArea textArea, String empId) {
        try {
            String query = "SELECT * FROM leave_applications WHERE employee_id = ? ORDER BY start_date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder sb = new StringBuilder();
            sb.append("Your Leave Applications:\n\n");
            
            while (rs.next()) {
                sb.append("Leave ID: ").append(rs.getInt("leave_id")).append("\n");
                sb.append("From: ").append(rs.getDate("start_date")).append(" To: ").append(rs.getDate("end_date")).append("\n");
                sb.append("Reason: ").append(rs.getString("reason")).append("\n");
                sb.append("Status: ").append(rs.getString("status")).append("\n");
                sb.append("-------------------\n");
            }
            
            if (sb.toString().equals("Your Leave Applications:\n\n")) {
                sb.append("No leave applications found");
            }
            
            textArea.setText(sb.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            textArea.setText("Error fetching leave applications: " + e.getMessage());
        }
    }

    private static void applyForLeave() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField startDateField = new JTextField();
        JTextField endDateField = new JTextField();
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        
        panel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        panel.add(startDateField);
        panel.add(new JLabel("End Date (YYYY-MM-DD):"));
        panel.add(endDateField);
        panel.add(new JLabel("Reason:"));
        panel.add(reasonScroll);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Apply for Leave", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String query = "INSERT INTO leave_applications (employee_id, start_date, end_date, reason) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, currentUserId);
                stmt.setString(2, startDateField.getText());
                stmt.setString(3, endDateField.getText());
                stmt.setString(4, reasonArea.getText());
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(null, "Leave application submitted successfully");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error submitting leave application: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Password Management Methods
    private static void changePassword() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JPasswordField oldPassField = new JPasswordField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();
        
        panel.add(new JLabel("Current Password:"));
        panel.add(oldPassField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPassField);
        panel.add(new JLabel("Confirm New Password:"));
        panel.add(confirmPassField);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Change Password", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String oldPass = new String(oldPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());
            
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(null, "New passwords don't match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Verify old password
                String verifyQuery = "SELECT * FROM users WHERE user_id = ? AND password = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
                verifyStmt.setString(1, currentUserId);
                verifyStmt.setString(2, oldPass);
                ResultSet rs = verifyStmt.executeQuery();
                
                if (rs.next()) {
                    // Update password
                    String updateQuery = "UPDATE users SET password = ? WHERE user_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, newPass);
                    updateStmt.setString(2, currentUserId);
                    updateStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(null, "Password changed successfully");
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect current password", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error changing password: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void resetEmployeePassword() {
        String empId = JOptionPane.showInputDialog("Enter Employee ID to reset password:");
        if (empId == null || empId.trim().isEmpty()) return;
        
        // Verify manager password for security
        String password = JOptionPane.showInputDialog("Enter your manager password to proceed:");
        if (password == null) return;
        
        try {
            String verifyQuery = "SELECT * FROM users WHERE user_id = ? AND password = ? AND role = 'manager'";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
            verifyStmt.setString(1, currentUserId);
            verifyStmt.setString(2, password);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Incorrect manager password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if employee exists
            String empQuery = "SELECT * FROM users WHERE user_id = ? AND role = 'employee'";
            PreparedStatement empStmt = conn.prepareStatement(empQuery);
            empStmt.setString(1, empId);
            ResultSet empRs = empStmt.executeQuery();
            
            if (empRs.next()) {
                // Reset to default password
                String updateQuery = "UPDATE users SET password = 'password123' WHERE user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, empId);
                updateStmt.executeUpdate();
                
                JOptionPane.showMessageDialog(null, "Password reset successfully for employee " + empId + "\nNew password: password123");
            } else {
                JOptionPane.showMessageDialog(null, "Employee not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error resetting password: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}