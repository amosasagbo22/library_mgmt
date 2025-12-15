package com.example.library;

import com.mongodb.client.*;
import com.mongodb.ConnectionString;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class MongoDBUtil {
    private static final String CONNECTION_STRING = "mongodb+srv:/MongoSecretUrl/?retryWrites=true&w=majority&appName=libraryDB";
    private static final String DATABASE_NAME = "libraryDB";

    private static final MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
    private static final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

    // BOOK COLLECTION & METHODS
    private static final MongoCollection<Document> bookCollection = database.getCollection("books");

    public static void insertBook(Book book) {
        Document doc = new Document("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("publishedDate", book.getPublishedDate())
                .append("quantity", book.getQuantity());
        if (book.getId() != null && !book.getId().isEmpty())
            doc.append("bookId", book.getId());
        bookCollection.insertOne(doc);
        System.out.println("Inserted Book: " + doc.toJson());
    }

    public static void updateBook(String bookId, Document updateFields) {
        Document updateDoc = new Document("$set", updateFields);
        UpdateResult result = bookCollection.updateOne(Filters.eq("bookId", bookId), updateDoc);
        System.out.println("Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
    }

    public static void deleteBook(String bookId) {
        DeleteResult result = bookCollection.deleteOne(Filters.eq("bookId", bookId));
        System.out.println("Deleted Count: " + result.getDeletedCount());
    }

    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        FindIterable<Document> docs = bookCollection.find();
        for (Document doc : docs) {
            Book book = new Book();
            book.setId(doc.getString("bookId"));
            book.setTitle(doc.getString("title"));
            book.setAuthor(doc.getString("author"));
            book.setPublishedDate(doc.getDate("publishedDate"));
            book.setQuantity(doc.getInteger("quantity", 0));
            books.add(book);
        }
        return books;
    }

    public static List<Book> searchBooksByTitle(String titlePattern) {
        List<Book> books = new ArrayList<>();
        FindIterable<Document> docs = bookCollection.find(
                Filters.regex("title", ".*" + titlePattern + ".*", "i")
        );
        for (Document doc : docs) {
            Book book = new Book();
            book.setId(doc.getString("bookId"));
            book.setTitle(doc.getString("title"));
            book.setAuthor(doc.getString("author"));
            book.setPublishedDate(doc.getDate("publishedDate"));
            book.setQuantity(doc.getInteger("quantity", 0));
            books.add(book);
        }
        return books;
    }

    public static int getTotalQuantity() {
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$group", new Document("_id", null)
                .append("totalQuantity", new Document("$sum", "$quantity"))));
        AggregateIterable<Document> result = bookCollection.aggregate(pipeline);
        int total = 0;
        for (Document doc : result) {
            total = doc.getInteger("totalQuantity", 0);
        }
        return total;
    }

    public static long getBooksCount() {
        return bookCollection.countDocuments();
    }

    public static List<Book> getBooksWithPagination(int skip, int limit) {
        List<Book> books = new ArrayList<>();
        FindIterable<Document> docs = bookCollection.find().skip(skip).limit(limit);
        for (Document doc : docs) {
            Book book = new Book();
            book.setId(doc.getString("bookId"));
            book.setTitle(doc.getString("title"));
            book.setAuthor(doc.getString("author"));
            book.setPublishedDate(doc.getDate("publishedDate"));
            book.setQuantity(doc.getInteger("quantity", 0));
            books.add(book);
        }
        return books;
    }

    // STAFF FUNCTIONS: Borrow and Return Books.
    public static boolean borrowBook(String bookId) {
        Document bookDoc = bookCollection.find(Filters.eq("bookId", bookId)).first();
        if (bookDoc != null) {
            int qty = bookDoc.getInteger("quantity", 0);
            if (qty > 0) {
                bookCollection.updateOne(
                        Filters.eq("bookId", bookId),
                        new Document("$inc", new Document("quantity", -1))
                );
                System.out.println("Book borrowed. New quantity: " + (qty - 1));
                return true;
            }
        }
        return false;
    }

    public static boolean returnBook(String bookId) {
        Document bookDoc = bookCollection.find(Filters.eq("bookId", bookId)).first();
        if (bookDoc != null) {
            bookCollection.updateOne(
                    Filters.eq("bookId", bookId),
                    new Document("$inc", new Document("quantity", 1))
            );
            System.out.println("Book returned.");
            return true;
        }
        return false;
    }

    // ADMIN COLLECTION & METHODS
    private static MongoCollection<Document> adminCollection = database.getCollection("admins");

    public static void insertAdmin(Admin admin) {
        Document doc = new Document("username", admin.getUsername())
                .append("password", admin.getPassword());
        if (admin.getId() != null && !admin.getId().isEmpty())
            doc.append("adminId", admin.getId());
        adminCollection.insertOne(doc);
        System.out.println("Inserted Admin: " + doc.toJson());
    }

    public static void updateAdmin(String adminId, Document updateFields) {
        Document updateDoc = new Document("$set", updateFields);
        UpdateResult result = adminCollection.updateOne(Filters.eq("adminId", adminId), updateDoc);
        System.out.println("Admin Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
    }

    public static void deleteAdmin(String adminId) {
        DeleteResult result = adminCollection.deleteOne(Filters.eq("adminId", adminId));
        System.out.println("Deleted Admin Count: " + result.getDeletedCount());
    }

    // STAFF COLLECTION & METHODS (managing staff records)
    private static MongoCollection<Document> staffCollection = database.getCollection("staff");

    public static void insertStaff(Staff staff) {
        Document doc = new Document("username", staff.getUsername())
                .append("password", staff.getPassword());
        if (staff.getId() != null && !staff.getId().isEmpty())
            doc.append("staffId", staff.getId());
        staffCollection.insertOne(doc);
        System.out.println("Inserted Staff: " + doc.toJson());
    }

    public static void updateStaff(String staffId, Document updateFields) {
        Document updateDoc = new Document("$set", updateFields);
        UpdateResult result = staffCollection.updateOne(Filters.eq("staffId", staffId), updateDoc);
        System.out.println("Staff Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
    }

    public static void deleteStaff(String staffId) {
        DeleteResult result = staffCollection.deleteOne(Filters.eq("staffId", staffId));
        System.out.println("Deleted Staff Count: " + result.getDeletedCount());
    }

    // MEMBER COLLECTION & METHODS
    private static MongoCollection<Document> memberCollection = database.getCollection("members");

    public static void insertMember(Member member) {
        Document doc = new Document("name", member.getName())
                .append("membershipNumber", member.getMembershipNumber())
                .append("password", member.getPassword());
        if (member.getId() != null && !member.getId().isEmpty())
            doc.append("memberId", member.getId());
        memberCollection.insertOne(doc);
        System.out.println("Inserted Member: " + doc.toJson());
    }

    public static void updateMember(String memberId, Document updateFields) {
        Document updateDoc = new Document("$set", updateFields);
        UpdateResult result = memberCollection.updateOne(Filters.eq("memberId", memberId), updateDoc);
        System.out.println("Member Matched: " + result.getMatchedCount() + ", Modified: " + result.getModifiedCount());
    }

    public static void deleteMember(String memberId) {
        DeleteResult result = memberCollection.deleteOne(Filters.eq("memberId", memberId));
        System.out.println("Deleted Member Count: " + result.getDeletedCount());
    }

}
