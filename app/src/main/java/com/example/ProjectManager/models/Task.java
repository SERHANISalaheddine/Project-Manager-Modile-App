package com.example.ProjectManager.models;

public class Task {
    private String title;
    private String date;
    private String status;   // "In Progress", "To Do", "Done"
    private int progress;    // 0..100

    public Task(String title, String date, String status, int progress) {
        this.title = title;
        this.date = date;
        this.status = status;
        this.progress = progress;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public int getProgress() { return progress; }
}
