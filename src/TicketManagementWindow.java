import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.*;
import java.sql.*;

public class TicketManagementWindow {
    private TableView<TicketType> table;

    public void show() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Create table
        table = new TableView<>();

        TableColumn<TicketType, String> nameCol = new TableColumn<>("Ticket Type");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        nameCol.setPrefWidth(150);

        TableColumn<TicketType, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<TicketType, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        TableColumn<TicketType, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, priceCol, descCol, activeCol);

        // Add buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add New");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        addButton.setOnAction(e -> showAddEditDialog(null));
        editButton.setOnAction(e -> {
            TicketType selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showAddEditDialog(selected);
            } else {
                showAlert("Error", "Please select a ticket type to edit");
            }
        });
        deleteButton.setOnAction(e -> deleteTicketType());

        root.getChildren().addAll(
                new Label("Manage Ticket Types"),
                table,
                buttonBox
        );

        loadTicketTypes();

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Manage Ticket Types");
        stage.setScene(scene);
        stage.show();
    }

    private void loadTicketTypes() {
        ObservableList<TicketType> ticketTypes = FXCollections.observableArrayList();
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ticket_types ORDER BY type_id");

            while (rs.next()) {
                ticketTypes.add(new TicketType(
                        rs.getInt("type_id"),
                        rs.getString("type_name"),
                        rs.getDouble("price"),
                        rs.getString("description"),
                        rs.getInt("is_active") == 1
                ));
            }

            table.setItems(ticketTypes);

        } catch (SQLException e) {
            showAlert("Error", "Could not load ticket types: " + e.getMessage());
        }
    }

    private void showAddEditDialog(TicketType ticketType) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);
        CheckBox activeCheck = new CheckBox("Active");

        if (ticketType != null) {
            nameField.setText(ticketType.getTypeName());
            priceField.setText(String.valueOf(ticketType.getPrice()));
            descField.setText(ticketType.getDescription());
            activeCheck.setSelected(ticketType.isActive());
        } else {
            activeCheck.setSelected(true);
        }

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price <= 0) {
                    showAlert("Error", "Price must be greater than 0");
                    return;
                }

                if (nameField.getText().trim().isEmpty()) {
                    showAlert("Error", "Please enter ticket name");
                    return;
                }

                if (ticketType == null) {
                    // Insert new ticket type
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(
                                "INSERT INTO ticket_types (type_name, price, description, is_active) " +
                                        "VALUES (?, ?, ?, ?)"
                        );
                        pstmt.setString(1, nameField.getText().trim());
                        pstmt.setDouble(2, price);
                        pstmt.setString(3, descField.getText().trim());
                        pstmt.setInt(4, activeCheck.isSelected() ? 1 : 0);
                        pstmt.executeUpdate();

                        showAlert("Success", "Ticket type added successfully!");
                        loadTicketTypes();
                        dialog.close();
                    } catch (SQLException ex) {
                        showAlert("Error", "Could not add ticket type: " + ex.getMessage());
                    }
                } else {
                    // Update existing ticket type
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        PreparedStatement pstmt = conn.prepareStatement(
                                "UPDATE ticket_types SET type_name = ?, price = ?, " +
                                        "description = ?, is_active = ? WHERE type_id = ?"
                        );
                        pstmt.setString(1, nameField.getText().trim());
                        pstmt.setDouble(2, price);
                        pstmt.setString(3, descField.getText().trim());
                        pstmt.setInt(4, activeCheck.isSelected() ? 1 : 0);
                        pstmt.setInt(5, ticketType.getTypeId());
                        pstmt.executeUpdate();

                        showAlert("Success", "Ticket type updated successfully!");
                        loadTicketTypes();
                        dialog.close();
                    } catch (SQLException ex) {
                        showAlert("Error", "Could not update ticket type: " + ex.getMessage());
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter a valid price");
            }
        });

        grid.addRow(0, new Label("Ticket Name:"), nameField);
        grid.addRow(1, new Label("Price:"), priceField);
        grid.addRow(2, new Label("Description:"), descField);
        grid.addRow(3, activeCheck);
        grid.addRow(4, new Label(""), saveButton);

        Scene scene = new Scene(grid);
        dialog.setTitle(ticketType == null ? "Add Ticket Type" : "Edit Ticket Type");
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void deleteTicketType() {
        TicketType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a ticket type to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this ticket type?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM ticket_types WHERE type_id = ?"
                );
                pstmt.setInt(1, selected.getTypeId());
                pstmt.executeUpdate();

                showAlert("Success", "Ticket type deleted successfully!");
                loadTicketTypes();
            } catch (SQLException e) {
                showAlert("Error", "Could not delete ticket type: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(
                title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}