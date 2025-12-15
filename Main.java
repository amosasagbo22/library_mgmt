package com.example.library;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Main extends Application {

    // ------------------- BOOKS TAB FIELDS ---------------------
    private TableView<Book> bookTableView;
    private TextField bookIdField, bookTitleField, bookAuthorField, bookDateField, bookQuantityField;
    private TextField bookSearchField;
    private ObservableList<Book> bookData;
    private Label totalQuantityLabel;
    private int currentPage = 0;
    private final int pageSize = 10;
    private Label pageInfoLabel;
    private Button previousButton, nextButton;

    // ------------------- ADMIN TAB FIELDS ---------------------
    private TextField adminIdField, adminUsernameField, adminPasswordField;

    // ------------------- STAFF TAB FIELDS ---------------------
    private TextField staffBookIdField;
    private Label staffStatusLabel;

    // ------------------- MEMBER TAB FIELDS ---------------------
    private TextField memberIdField, memberNameField, memberMembershipField, memberPasswordField;
    private TableView<Book> memberBookTableView;
    private ObservableList<Book> memberBookData;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Library Management System");

        // Create tabs for different roles/features.
        TabPane tabPane = new TabPane();
        Tab booksTab = new Tab("Books", createBooksTab());
        Tab adminTab = new Tab("Admin", createAdminTab());
        Tab staffTab = new Tab("Staff", createStaffTab());
        Tab memberTab = new Tab("Members", createMemberTab());

        tabPane.getTabs().addAll(booksTab, adminTab, staffTab, memberTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // ------------------- BOOKS TAB ---------------------
    private VBox createBooksTab() {
        // TableView for books.
        bookTableView = new TableView<>();
        TableColumn<Book, String> idCol = new TableColumn<>("Book ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Book, Date> dateCol = new TableColumn<>("Published Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        TableColumn<Book, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        bookTableView.getColumns().addAll(idCol, titleCol, authorCol, dateCol, quantityCol);

        // Search controls.
        Label searchLabel = new Label("Search Title:");
        bookSearchField = new TextField();
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchBooks());
        HBox searchBox = new HBox(10, searchLabel, bookSearchField, searchButton);
        searchBox.setPadding(new Insets(10));

        // Pagination controls.
        previousButton = new Button("Previous");
        previousButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPaginatedBooks();
            }
        });
        nextButton = new Button("Next");
        nextButton.setOnAction(e -> {
            currentPage++;
            loadPaginatedBooks();
        });
        pageInfoLabel = new Label();
        HBox paginationBox = new HBox(10, previousButton, nextButton, pageInfoLabel);
        paginationBox.setPadding(new Insets(10));

        // Aggregate total quantity.
        totalQuantityLabel = new Label();
        updateTotalQuantity();

        // Form to add/update/delete books.
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        bookIdField = new TextField();
        bookTitleField = new TextField();
        bookAuthorField = new TextField();
        bookDateField = new TextField();
        bookQuantityField = new TextField();
        form.add(new Label("Book ID:"), 0, 0);
        form.add(bookIdField, 1, 0);
        form.add(new Label("Title:"), 0, 1);
        form.add(bookTitleField, 1, 1);
        form.add(new Label("Author:"), 0, 2);
        form.add(bookAuthorField, 1, 2);
        form.add(new Label("Published Date (yyyy-MM-dd):"), 0, 3);
        form.add(bookDateField, 1, 3);
        form.add(new Label("Quantity:"), 0, 4);
        form.add(bookQuantityField, 1, 4);

        Button addBookButton = new Button("Add Book");
        addBookButton.setOnAction(e -> addBook());
        Button updateBookButton = new Button("Update Book");
        updateBookButton.setOnAction(e -> updateBook());
        Button deleteBookButton = new Button("Delete Book");
        deleteBookButton.setOnAction(e -> deleteBook());
        HBox buttonBox = new HBox(10, addBookButton, updateBookButton, deleteBookButton);

        VBox vbox = new VBox(10, searchBox, bookTableView, paginationBox, totalQuantityLabel, form, buttonBox);
        vbox.setPadding(new Insets(15));
        loadPaginatedBooks();
        return vbox;
    }

    // ------------------- ADMIN TAB ---------------------
    private VBox createAdminTab() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        adminIdField = new TextField();
        adminUsernameField = new TextField();
        adminPasswordField = new TextField();

        form.add(new Label("Admin ID:"), 0, 0);
        form.add(adminIdField, 1, 0);
        form.add(new Label("Username:"), 0, 1);
        form.add(adminUsernameField, 1, 1);
        form.add(new Label("Password:"), 0, 2);
        form.add(adminPasswordField, 1, 2);

        Button addAdminButton = new Button("Add Admin");
        addAdminButton.setOnAction(e -> addAdmin());
        Button updateAdminButton = new Button("Update Admin");
        updateAdminButton.setOnAction(e -> updateAdmin());
        Button deleteAdminButton = new Button("Delete Admin");
        deleteAdminButton.setOnAction(e -> deleteAdmin());
        HBox buttonBox = new HBox(10, addAdminButton, updateAdminButton, deleteAdminButton);

        VBox vbox = new VBox(10, form, buttonBox);
        vbox.setPadding(new Insets(15));
        return vbox;
    }

    // ------------------- STAFF TAB ---------------------
    private VBox createStaffTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        Label infoLabel = new Label("Process Book Borrow/Return:");
        staffBookIdField = new TextField();
        staffBookIdField.setPromptText("Enter Book ID");
        Button borrowButton = new Button("Borrow Book");
        borrowButton.setOnAction(e -> processBorrow());
        Button returnButton = new Button("Return Book");
        returnButton.setOnAction(e -> processReturn());
        staffStatusLabel = new Label();
        vbox.getChildren().addAll(infoLabel, new HBox(10, new Label("Book ID:"), staffBookIdField),
                new HBox(10, borrowButton, returnButton), staffStatusLabel);
        return vbox;
    }

    // ------------------- MEMBER TAB ---------------------
    private VBox createMemberTab() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        memberIdField = new TextField();
        memberNameField = new TextField();
        memberMembershipField = new TextField();
        memberPasswordField = new TextField();

        form.add(new Label("Member ID:"), 0, 0);
        form.add(memberIdField, 1, 0);
        form.add(new Label("Name:"), 0, 1);
        form.add(memberNameField, 1, 1);
        form.add(new Label("Membership No.:"), 0, 2);
        form.add(memberMembershipField, 1, 2);
        form.add(new Label("Password:"), 0, 3);
        form.add(memberPasswordField, 1, 3);

        Button registerMemberButton = new Button("Register Member");
        registerMemberButton.setOnAction(e -> registerMember());

        // TableView for available books.
        memberBookTableView = new TableView<>();
        TableColumn<Book, String> idCol = new TableColumn<>("Book ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Book, Date> dateCol = new TableColumn<>("Published Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        TableColumn<Book, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        memberBookTableView.getColumns().addAll(idCol, titleCol, authorCol, dateCol, quantityCol);

        Button loadBooksButton = new Button("Load Available Books");
        loadBooksButton.setOnAction(e -> loadAvailableBooks());

        VBox vbox = new VBox(10, form, registerMemberButton, new Separator(), loadBooksButton, memberBookTableView);
        vbox.setPadding(new Insets(15));
        return vbox;
    }

    // ------------------- BOOKS OPERATIONS ---------------------

    // Load books with pagination—if no search query is active.
    private void loadPaginatedBooks() {
        if (bookSearchField.getText().trim().isEmpty()) {
            List<Book> books = MongoDBUtil.getBooksWithPagination(currentPage * pageSize, pageSize);
            bookData = FXCollections.observableArrayList(books);
            bookTableView.setItems(bookData);
            updatePageInfo();
        }
    }

    // Update pagination information.
    private void updatePageInfo() {
        long totalBooks = MongoDBUtil.getBooksCount();
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
        pageInfoLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);
        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * pageSize >= totalBooks);
    }

    // Update total quantity label using an aggregation query.
    private void updateTotalQuantity() {
        int totalQty = MongoDBUtil.getTotalQuantity();
        totalQuantityLabel.setText("Total Books Quantity: " + totalQty);
    }

    // Search books by title.
    private void searchBooks() {
        String pattern = bookSearchField.getText().trim();
        if (pattern.isEmpty()) {
            currentPage = 0;
            loadPaginatedBooks();
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pageInfoLabel.setText("");
        } else {
            List<Book> books = MongoDBUtil.searchBooksByTitle(pattern);
            bookData = FXCollections.observableArrayList(books);
            bookTableView.setItems(bookData);
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            pageInfoLabel.setText("Search results");
        }
    }

    // Add a new book.
    private void addBook() {
        try {
            String id = bookIdField.getText().trim();
            String title = bookTitleField.getText().trim();
            String author = bookAuthorField.getText().trim();
            String dateStr = bookDateField.getText().trim();
            int quantity = Integer.parseInt(bookQuantityField.getText().trim());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date publicationDate = sdf.parse(dateStr);
            Book book = new Book(id, title, author, publicationDate, quantity);
            MongoDBUtil.insertBook(book);
            if (bookSearchField.getText().trim().isEmpty())
                loadPaginatedBooks();
            else
                searchBooks();
            updateTotalQuantity();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid book input: " + ex.getMessage());
        }
    }

    // Update the selected book’s details.
    private void updateBook() {
        Book selected = bookTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a book to update.");
            return;
        }
        try {
            String title = bookTitleField.getText().trim();
            String author = bookAuthorField.getText().trim();
            String dateStr = bookDateField.getText().trim();
            int quantity = Integer.parseInt(bookQuantityField.getText().trim());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date publicationDate = sdf.parse(dateStr);
            Document updateDoc = new Document("title", title)
                    .append("author", author)
                    .append("publishedDate", publicationDate)
                    .append("quantity", quantity);
            MongoDBUtil.updateBook(selected.getId(), updateDoc);
            if (bookSearchField.getText().trim().isEmpty())
                loadPaginatedBooks();
            else
                searchBooks();
            updateTotalQuantity();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid book input: " + ex.getMessage());
        }
    }

    // Delete the selected book.
    private void deleteBook() {
        Book selected = bookTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a book to delete.");
            return;
        }
        MongoDBUtil.deleteBook(selected.getId());
        if (bookSearchField.getText().trim().isEmpty())
            loadPaginatedBooks();
        else
            searchBooks();
        updateTotalQuantity();
    }

    // ------------------- ADMIN OPERATIONS ---------------------
    private void addAdmin() {
        try {
            String id = adminIdField.getText().trim();
            String username = adminUsernameField.getText().trim();
            String password = adminPasswordField.getText().trim();
            Admin admin = new Admin(id, username, password);
            MongoDBUtil.insertAdmin(admin);
            showAlert("Info", "Admin added successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid admin input: " + ex.getMessage());
        }
    }

    private void updateAdmin() {
        try {
            String id = adminIdField.getText().trim();
            String username = adminUsernameField.getText().trim();
            String password = adminPasswordField.getText().trim();
            Document updateDoc = new Document("username", username)
                    .append("password", password);
            MongoDBUtil.updateAdmin(id, updateDoc);
            showAlert("Info", "Admin updated successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid admin input: " + ex.getMessage());
        }
    }

    private void deleteAdmin() {
        try {
            String id = adminIdField.getText().trim();
            MongoDBUtil.deleteAdmin(id);
            showAlert("Info", "Admin deleted successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid admin input: " + ex.getMessage());
        }
    }

    // ------------------- STAFF OPERATIONS ---------------------
    private void processBorrow() {
        String bookId = staffBookIdField.getText().trim();
        if (bookId.isEmpty()) {
            staffStatusLabel.setText("Enter a valid Book ID.");
            return;
        }
        boolean success = MongoDBUtil.borrowBook(bookId);
        if (success) {
            staffStatusLabel.setText("Book borrowed successfully.");
            updateTotalQuantity();
            loadPaginatedBooks();
        } else {
            staffStatusLabel.setText("Unable to borrow book: out of stock or invalid ID.");
        }
    }

    private void processReturn() {
        String bookId = staffBookIdField.getText().trim();
        if (bookId.isEmpty()) {
            staffStatusLabel.setText("Enter a valid Book ID.");
            return;
        }
        boolean success = MongoDBUtil.returnBook(bookId);
        if (success) {
            staffStatusLabel.setText("Book returned successfully.");
            updateTotalQuantity();
            loadPaginatedBooks();
        } else {
            staffStatusLabel.setText("Unable to return book. Check Book ID.");
        }
    }

    // ------------------- MEMBER OPERATIONS ---------------------
    private void registerMember() {
        try {
            String id = memberIdField.getText().trim();
            String name = memberNameField.getText().trim();
            String membership = memberMembershipField.getText().trim();
            String password = memberPasswordField.getText().trim();
            Member member = new Member(id, name, membership, password);
            MongoDBUtil.insertMember(member);
            showAlert("Info", "Member registered successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Invalid member input: " + ex.getMessage());
        }
    }

    private void loadAvailableBooks() {
        try {
            List<Book> books = MongoDBUtil.getAllBooks();
            memberBookData = FXCollections.observableArrayList(books);
            memberBookTableView.setItems(memberBookData);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Unable to load books: " + ex.getMessage());
        }
    }

    // ------------------- COMMON HELPER ---------------------
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}