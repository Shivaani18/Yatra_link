import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class TempleManagementSystem extends Application {
    private Stage primaryStage;
    private VBox mainContainer;
    private VBox contentArea;
    private Label dateTimeLabel;
    private Label userLabel;
    private ToggleGroup navigationGroup;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        initializeMainWindow();
        primaryStage.show();
    }

    private void initializeMainWindow() {
        mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-background-color: #9dabc2;");

        // Create menu bar
        MenuBar menuBar = createMenuBar();

        // Create header
        VBox header = createHeader();

        // Create navigation buttons
        HBox navigationButtons = createNavigationButtons();

        // Create content area
        contentArea = new VBox(10);
        contentArea.setPadding(new Insets(10));

        // Default view is ticket booking
        showTicketBookingForm();

        mainContainer.getChildren().addAll(menuBar, header, new Separator(),
                navigationButtons, contentArea);

        Scene scene = new Scene(mainContainer, 1024, 768);
        primaryStage.setTitle("YATRA LINK");
        primaryStage.setScene(scene);

        startClock();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Reports Menu
        Menu reportsMenu = new Menu("Reports");
        MenuItem todayBookings = new MenuItem("Today's Bookings");
        MenuItem todayDonations = new MenuItem("Today's Donations");
        MenuItem revenue = new MenuItem("Revenue Report");

        todayBookings.setOnAction(e -> showTodayBookings());
        todayDonations.setOnAction(e -> showTodayDonations());
        revenue.setOnAction(e -> showRevenueReport());

        reportsMenu.getItems().addAll(todayBookings, todayDonations,
                new SeparatorMenuItem(), revenue);

        // Management Menu
        Menu managementMenu = new Menu("Management");
        MenuItem ticketTypes = new MenuItem("Manage Ticket Types");
        ticketTypes.setOnAction(e -> new TicketManagementWindow().show());

        managementMenu.getItems().add(ticketTypes);

        menuBar.getMenus().addAll(reportsMenu, managementMenu);
        return menuBar;
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);

        dateTimeLabel = new Label();
        dateTimeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        userLabel = new Label("YATHRA LINK ");
        userLabel.setFont(Font.font("Book Antiqua", FontWeight.BOLD, 18));

        header.getChildren().addAll(dateTimeLabel, userLabel);
        return header;
    }

    private HBox createNavigationButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        navigationGroup = new ToggleGroup();

        ToggleButton ticketsBtn = createNavButton("Ticket Booking");
        ToggleButton donationsBtn = createNavButton("Donations");

        ticketsBtn.setSelected(true);
        ticketsBtn.setOnAction(e -> showTicketBookingForm());
        donationsBtn.setOnAction(e -> showDonationForm());

        buttonBox.getChildren().addAll(ticketsBtn, donationsBtn);
        return buttonBox;
    }

    private ToggleButton createNavButton(String text) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(navigationGroup);
        btn.setMinWidth(150);
        btn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 15px;");
        return btn;
    }

    private void showTicketBookingForm() {
        contentArea.getChildren().clear();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        DatePicker visitDate = new DatePicker(LocalDate.now());

        ComboBox<String> timeSlot = new ComboBox<>(FXCollections.observableArrayList(
                "Morning (6:00 AM - 12:00 PM)",
                "Afternoon (12:00 PM - 4:00 PM)",
                "Evening (4:00 PM - 8:00 PM)"
        ));

        // Add ticket type selection
        ComboBox<TicketType> ticketTypeCombo = new ComboBox<>();
        loadTicketTypes(ticketTypeCombo);

        Spinner<Integer> ticketCount = new Spinner<>(1, 10, 1);
        Label totalAmount = new Label("Total Amount: ₹0.00");

        // Update total amount when ticket count or type changes
        ticketCount.valueProperty().addListener((obs, old, newVal) ->
                updateTotalAmount(totalAmount, ticketTypeCombo.getValue(), newVal));

        ticketTypeCombo.setOnAction(e ->
                updateTotalAmount(totalAmount, ticketTypeCombo.getValue(),
                        ticketCount.getValue()));

        grid.addRow(0, new Label("Devotee Name:"), nameField);
        grid.addRow(1, new Label("Phone Number:"), phoneField);
        grid.addRow(2, new Label("Visit Date:"), visitDate);
        grid.addRow(3, new Label("Time Slot:"), timeSlot);
        grid.addRow(4, new Label("Ticket Type:"), ticketTypeCombo);
        grid.addRow(5, new Label("Number of Tickets:"), ticketCount);
        grid.addRow(6, new Label(""), totalAmount);

        Button bookButton = new Button("Book Tickets");
        bookButton.setOnAction(e -> {
            if (validateTicketForm(nameField.getText(), phoneField.getText(),
                    visitDate.getValue(), timeSlot.getValue(),
                    ticketTypeCombo.getValue())) {
                bookTickets(nameField.getText(), phoneField.getText(),
                        visitDate.getValue(), timeSlot.getValue(),
                        ticketTypeCombo.getValue(), ticketCount.getValue());
            }
        });

        grid.add(bookButton, 1, 7);

        contentArea.getChildren().add(grid);
    }

    private void updateTotalAmount(Label totalAmount, TicketType ticketType, int count) {
        if (ticketType != null) {
            totalAmount.setText(String.format("Total Amount: ₹%.2f",count * ticketType.getPrice()));
        }
    }

    private void loadTicketTypes(ComboBox<TicketType> combo) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM ticket_types WHERE is_active = 1 ORDER BY type_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ObservableList<TicketType> types = FXCollections.observableArrayList();
            while (rs.next()) {
                types.add(new TicketType(
                        rs.getInt("type_id"),
                        rs.getString("type_name"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        true
                ));
            }

            combo.setItems(types);

        } catch (SQLException e) {
            showAlert("Error", "Could not load ticket types: " + e.getMessage());
        }
    }

    private void showDonationForm() {
        contentArea.getChildren().clear();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField amountField = new TextField();
        TextArea purposeField = new TextArea();
        purposeField.setPrefRowCount(3);

        ComboBox<String> paymentMode = new ComboBox<>(FXCollections.observableArrayList(
                "Cash", "Card", "UPI"
        ));

        grid.addRow(0, new Label("Donor Name:"), nameField);
        grid.addRow(1, new Label("Phone Number:"), phoneField);
        grid.addRow(2, new Label("Amount:"), amountField);
        grid.addRow(3, new Label("Purpose:"), purposeField);
        grid.addRow(4, new Label("Payment Mode:"), paymentMode);

        Button donateButton = new Button("Make Donation");
        donateButton.setOnAction(e -> {
            if (validateDonationForm(nameField.getText(), amountField.getText(),
                    paymentMode.getValue())) {
                makeDonation(nameField.getText(), phoneField.getText(),
                        amountField.getText(), purposeField.getText(),
                        paymentMode.getValue());
            }
        });

        grid.add(donateButton, 1, 5);

        contentArea.getChildren().add(grid);
    }

    private void showTodayBookings() {
        Stage reportStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TableView<TicketBooking> table = new TableView<>();

        TableColumn<TicketBooking, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("devoteName"));

        TableColumn<TicketBooking, String> timeCol = new TableColumn<>("Time Slot");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timeSlot"));

        TableColumn<TicketBooking, Integer> ticketsCol = new TableColumn<>("Tickets");
        ticketsCol.setCellValueFactory(new PropertyValueFactory<>("numTickets"));

        TableColumn<TicketBooking, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(nameCol, timeCol, ticketsCol, amountCol);

        loadTodayBookings(table);

        root.getChildren().add(table);

        Scene scene = new Scene(root, 600, 400);
        reportStage.setTitle("Today's Bookings");
        reportStage.setScene(scene);
        reportStage.show();
    }

    private void loadTodayBookings(TableView<TicketBooking> table) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM temple_tickets WHERE TRUNC(booking_time) = TRUNC(SYSDATE)";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ObservableList<TicketBooking> bookings = FXCollections.observableArrayList();

            while (rs.next()) {
                bookings.add(new TicketBooking(
                        rs.getString("devotee_name"),
                        rs.getString("visit_time"),
                        rs.getInt("num_tickets"),
                        rs.getDouble("total_amount")
                ));
            }

            table.setItems(bookings);

        } catch (SQLException e) {
            showAlert("Error", "Could not load bookings: " + e.getMessage());
        }
    }

    private void showTodayDonations() {
        Stage reportStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TableView<DonationRecord> table = new TableView<>();

        TableColumn<DonationRecord, String> nameCol = new TableColumn<>("Donor Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("donorName"));

        TableColumn<DonationRecord, String> modeCol = new TableColumn<>("Payment Mode");
        modeCol.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));

        TableColumn<DonationRecord, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<DonationRecord, String> purposeCol = new TableColumn<>("Purpose");
        purposeCol.setCellValueFactory(new PropertyValueFactory<>("purpose"));

        table.getColumns().addAll(nameCol, modeCol, amountCol, purposeCol);

        loadTodayDonations(table); // Helper method

        root.getChildren().add(table);

        Scene scene = new Scene(root, 600, 400);
        reportStage.setTitle("Today's Donations");
        reportStage.setScene(scene);
        reportStage.show();
    }

    private void loadTodayDonations(TableView<DonationRecord> table) {
        ObservableList<DonationRecord> donations = FXCollections.observableArrayList();

        String sql = "SELECT donor_name, payment_mode, amount, purpose " +
                "FROM temple_donations " +
                "WHERE TRUNC(donation_date) = TRUNC(SYSDATE)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn != null ? conn.createStatement() : null;
             ResultSet rs = stmt != null ? stmt.executeQuery(sql) : null) {

            if (rs != null) {
                while (rs.next()) {
                    donations.add(new DonationRecord(
                            rs.getString("donor_name"),
                            rs.getString("payment_mode"),
                            rs.getDouble("amount"),
                            rs.getString("purpose")
                    ));
                }
            }

            table.setItems(donations);

        } catch (SQLException e) {
            e.printStackTrace();  // For debugging, remove in production
            showAlert("Error", "Could not load donations: " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Unexpected error: " + ex.getMessage());
        }
    }

    private void showRevenueReport() {
        Stage reportStage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label ticketRevenueLabel = new Label("Ticket Revenue: ₹0.00");
        Label donationRevenueLabel = new Label("Donation Revenue: ₹0.00");
        Label totalRevenueLabel = new Label("Total Revenue: ₹0.00");

        ticketRevenueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        donationRevenueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        totalRevenueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");

        try {
            Connection conn = DatabaseConnection.getConnection();

            // Ticket revenue
            String ticketSql = "SELECT SUM(total_amount) AS total_ticket FROM temple_tickets WHERE TRUNC(booking_time) = TRUNC(SYSDATE)";
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(ticketSql);
            double ticketRevenue = 0.0;
            if (rs1.next()) {
                ticketRevenue = rs1.getDouble("total_ticket");
            }

            // Donation revenue
            String donationSql = "SELECT SUM(amount) AS total_donation FROM temple_donations WHERE TRUNC(donation_date) = TRUNC(SYSDATE)";
            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery(donationSql);
            double donationRevenue = 0.0;
            if (rs2.next()) {
                donationRevenue = rs2.getDouble("total_donation");
            }

            double totalRevenue = ticketRevenue + donationRevenue;

            ticketRevenueLabel.setText("Ticket Revenue: ₹" + String.format("%.2f", ticketRevenue));
            donationRevenueLabel.setText("Donation Revenue: ₹" + String.format("%.2f", donationRevenue));
            totalRevenueLabel.setText("Total Revenue: ₹" + String.format("%.2f", totalRevenue));

        } catch (SQLException e) {
            showAlert("Error", "Could not load revenue report: " + e.getMessage());
        }

        root.getChildren().addAll(ticketRevenueLabel, donationRevenueLabel, totalRevenueLabel);

        Scene scene = new Scene(root, 400, 200);
        reportStage.setTitle("Revenue Report - Today");
        reportStage.setScene(scene);
        reportStage.show();
    }


    private boolean validateTicketForm(String name, String phone, LocalDate date,
                                       String timeSlot, TicketType ticketType) {
        if (name == null || name.trim().isEmpty()) {
            showAlert("Error", "Please enter devotee name");
            return false;
        }
        if (phone == null || phone.trim().isEmpty()) {
            showAlert("Error", "Please enter phone number");
            return false;
        }
        if (date == null) {
            showAlert("Error", "Please select visit date");
            return false;
        }
        if (timeSlot == null) {
            showAlert("Error", "Please select time slot");
            return false;
        }
        if (ticketType == null) {
            showAlert("Error", "Please select ticket type");
            return false;
        }
        return true;
    }

    private boolean validateDonationForm(String name, String amount, String paymentMode) {
        if (name == null || name.trim().isEmpty()) {
            showAlert("Error", "Please enter donor name");
            return false;
        }
        try {
            double amt = Double.parseDouble(amount);
            if (amt <= 0) {
                showAlert("Error", "Please enter a valid amount");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount");
            return false;
        }
        if (paymentMode == null) {
            showAlert("Error", "Please select payment mode");
            return false;
        }
        return true;
    }

    private void bookTickets(String name, String phone, LocalDate visitDate,
                             String timeSlot, TicketType ticketType, int numTickets) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO temple_tickets (devotee_name, phone, visit_date, " +
                    "visit_time, ticket_type_id, num_tickets, rate_per_ticket, total_amount) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setDate(3, java.sql.Date.valueOf(visitDate));
            pstmt.setString(4, timeSlot);
            pstmt.setInt(5, ticketType.getTypeId());
            pstmt.setInt(6, numTickets);
            pstmt.setDouble(7, ticketType.getPrice());
            pstmt.setDouble(8, numTickets * ticketType.getPrice());

            pstmt.executeUpdate();

            showAlert("Success", "Tickets booked successfully!");
            showTicketBookingForm(); // Reset form

        } catch (SQLException e) {
            showAlert("Error", "Could not book tickets: " + e.getMessage());
        }
    }

    private void makeDonation(String name, String phone, String amount,
                              String purpose, String paymentMode) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO temple_donations (donor_name, phone, amount, " +
                    "purpose, payment_mode) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setDouble(3, Double.parseDouble(amount));
            pstmt.setString(4, purpose);
            pstmt.setString(5, paymentMode);

            pstmt.executeUpdate();

            showAlert("Success", "Donation recorded successfully!");
            showDonationForm(); // Reset form

        } catch (SQLException e) {
            showAlert("Error", "Could not process donation: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR
                : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                dateTimeLabel.setText("Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        ));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}