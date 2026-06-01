package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NoticeData {

    private int    noticeId;
    private String title;
    private String description;
    private String category;
    private String priority;
    private boolean pinned;
    private LocalDateTime dateIssued;

    public NoticeData() {}

    public NoticeData(String title, String description, String category,
                      String priority, boolean pinned) {
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.priority    = priority;
        this.pinned      = pinned;
    }

    // Getters / setters
    public int     getNoticeId()               { return noticeId; }
    public void    setNoticeId(int id)          { this.noticeId = id; }

    public String  getTitle()                  { return title; }
    public void    setTitle(String t)           { this.title = t; }

    public String  getDescription()            { return description; }
    public void    setDescription(String d)    { this.description = d; }

    public String  getCategory()               { return category; }
    public void    setCategory(String c)       { this.category = c; }

    public String  getPriority()               { return priority; }
    public void    setPriority(String p)       { this.priority = p; }

    public boolean isPinned()                  { return pinned; }
    public void    setPinned(boolean p)        { this.pinned = p; }

    public LocalDateTime getDateIssued()       { return dateIssued; }
    public void    setDateIssued(LocalDateTime d) { this.dateIssued = d; }

    /** Returns formatted date string for display, e.g. "May 28, 2026" */
    public String getFormattedDate() {
        if (dateIssued == null) return "";
        return dateIssued.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}