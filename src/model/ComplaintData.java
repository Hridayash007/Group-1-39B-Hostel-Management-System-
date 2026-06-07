package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComplaintData {

    private int    complaintId;
    private int    userId;
    private String username;     // joined from users table for admin view
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private LocalDateTime dateFiled;

    public ComplaintData() {}

    // Getters / setters
    public int    getComplaintId()              { return complaintId; }
    public void   setComplaintId(int id)         { this.complaintId = id; }

    public int    getUserId()                   { return userId; }
    public void   setUserId(int id)              { this.userId = id; }

    public String getUsername()                 { return username; }
    public void   setUsername(String u)          { this.username = u; }

    public String getTitle()                    { return title; }
    public void   setTitle(String t)             { this.title = t; }

    public String getDescription()              { return description; }
    public void   setDescription(String d)       { this.description = d; }

    public String getCategory()                 { return category; }
    public void   setCategory(String c)          { this.category = c; }

    public String getPriority()                 { return priority; }
    public void   setPriority(String p)          { this.priority = p; }

    public String getStatus()                   { return status; }
    public void   setStatus(String s)            { this.status = s; }

    public LocalDateTime getDateFiled()         { return dateFiled; }
    public void   setDateFiled(LocalDateTime d)  { this.dateFiled = d; }

    public String getFormattedDate() {
        if (dateFiled == null) return "";
        return dateFiled.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}