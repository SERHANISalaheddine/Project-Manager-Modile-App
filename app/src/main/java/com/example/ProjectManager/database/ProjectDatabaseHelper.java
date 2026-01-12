package com.example.ProjectManager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.Project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SQLite database helper for managing projects and members.
 * Handles all database operations including CRUD for projects and
 * project-member relationships.
 */
public class ProjectDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "ProjectManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_PROJECTS = "projects";
    private static final String TABLE_MEMBERS = "members";
    private static final String TABLE_PROJECT_MEMBERS = "project_members";

    // Projects Table Columns
    private static final String KEY_PROJECT_ID = "id";
    private static final String KEY_PROJECT_TITLE = "title";
    private static final String KEY_PROJECT_DESCRIPTION = "description";
    private static final String KEY_PROJECT_STATUS = "status";
    private static final String KEY_PROJECT_CREATED_AT = "created_at";
    private static final String KEY_PROJECT_DUE_DATE = "due_date";
    private static final String KEY_PROJECT_CREATOR_ID = "creator_id";

    // Members Table Columns
    private static final String KEY_MEMBER_ID = "id";
    private static final String KEY_MEMBER_NAME = "name";
    private static final String KEY_MEMBER_ROLE = "role";
    private static final String KEY_MEMBER_AVATAR_URL = "avatar_url";

    // Project Members Table Columns (Junction Table)
    private static final String KEY_PM_ID = "id";
    private static final String KEY_PM_PROJECT_ID = "project_id";
    private static final String KEY_PM_MEMBER_ID = "member_id";

    // Date format for database operations
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Singleton instance
    private static ProjectDatabaseHelper instance;

    /**
     * Get singleton instance of database helper
     */
    public static synchronized ProjectDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ProjectDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Constructor
     */
    public ProjectDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Projects table
        String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
                + KEY_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PROJECT_TITLE + " TEXT NOT NULL,"
                + KEY_PROJECT_DESCRIPTION + " TEXT,"
                + KEY_PROJECT_STATUS + " TEXT DEFAULT 'created',"
                + KEY_PROJECT_CREATED_AT + " TEXT,"
                + KEY_PROJECT_DUE_DATE + " TEXT,"
                + KEY_PROJECT_CREATOR_ID + " INTEGER"
                + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);

        // Create Members table
        String CREATE_MEMBERS_TABLE = "CREATE TABLE " + TABLE_MEMBERS + "("
                + KEY_MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_NAME + " TEXT NOT NULL,"
                + KEY_MEMBER_ROLE + " TEXT,"
                + KEY_MEMBER_AVATAR_URL + " TEXT"
                + ")";
        db.execSQL(CREATE_MEMBERS_TABLE);

        // Create Project Members junction table
        String CREATE_PROJECT_MEMBERS_TABLE = "CREATE TABLE " + TABLE_PROJECT_MEMBERS + "("
                + KEY_PM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PM_PROJECT_ID + " INTEGER,"
                + KEY_PM_MEMBER_ID + " INTEGER,"
                + "FOREIGN KEY(" + KEY_PM_PROJECT_ID + ") REFERENCES " + TABLE_PROJECTS + "(" + KEY_PROJECT_ID + "),"
                + "FOREIGN KEY(" + KEY_PM_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(" + KEY_MEMBER_ID + ")"
                + ")";
        db.execSQL(CREATE_PROJECT_MEMBERS_TABLE);

        // Insert sample members
        insertSampleMembers(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECT_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Insert sample members into the database
     */
    private void insertSampleMembers(SQLiteDatabase db) {
        insertMember(db, new Member(0, "Ivankov", "Sr Front End Developer"));
        insertMember(db, new Member(0, "Brahm", "Mid Front End Developer"));
        insertMember(db, new Member(0, "Alice", "Sr Front End Developer"));
        insertMember(db, new Member(0, "Jeane", "Jr Front End Developer"));
        insertMember(db, new Member(0, "Claudia", "Jr Front End Developer"));
    }

    /**
     * Insert a member into the database
     */
    private long insertMember(SQLiteDatabase db, Member member) {
        ContentValues values = new ContentValues();
        values.put(KEY_MEMBER_NAME, member.getName());
        values.put(KEY_MEMBER_ROLE, member.getRole());
        values.put(KEY_MEMBER_AVATAR_URL, member.getAvatarUrl());

        return db.insert(TABLE_MEMBERS, null, values);
    }

    // ===================== PROJECT OPERATIONS =====================

    /**
     * Insert a new project into the database
     * 
     * @param project The project to insert
     * @return The row ID of the newly inserted project, or -1 if an error occurred
     */
    public long insertProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();
        long projectId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_PROJECT_TITLE, project.getTitle());
            values.put(KEY_PROJECT_DESCRIPTION, project.getDescription());
            values.put(KEY_PROJECT_STATUS, project.getStatus());
            values.put(KEY_PROJECT_CREATED_AT, dateFormat.format(new Date()));

            if (project.getDueDate() != null) {
                values.put(KEY_PROJECT_DUE_DATE, dateFormat.format(project.getDueDate()));
            }
            values.put(KEY_PROJECT_CREATOR_ID, project.getCreatorId());

            projectId = db.insertOrThrow(TABLE_PROJECTS, null, values);

            // Insert project members
            if (project.getMembers() != null && !project.getMembers().isEmpty()) {
                for (Member member : project.getMembers()) {
                    addMemberToProject(db, projectId, member.getId());
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            projectId = -1;
        } finally {
            db.endTransaction();
        }

        return projectId;
    }

    /**
     * Add a member to a project
     */
    private void addMemberToProject(SQLiteDatabase db, long projectId, long memberId) {
        ContentValues values = new ContentValues();
        values.put(KEY_PM_PROJECT_ID, projectId);
        values.put(KEY_PM_MEMBER_ID, memberId);
        db.insert(TABLE_PROJECT_MEMBERS, null, values);
    }

    /**
     * Get all projects from the database
     * 
     * @return List of all projects
     */
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();

        String SELECT_QUERY = "SELECT * FROM " + TABLE_PROJECTS + " ORDER BY " + KEY_PROJECT_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Project project = cursorToProject(cursor);
                    // Load project members
                    project.setMembers(getProjectMembers(project.getId()));
                    projects.add(project);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return projects;
    }

    /**
     * Get a project by ID
     * 
     * @param projectId The project ID
     * @return The project, or null if not found
     */
    public Project getProjectById(int projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Project project = null;

        String SELECT_QUERY = "SELECT * FROM " + TABLE_PROJECTS + " WHERE " + KEY_PROJECT_ID + " = ?";
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[] { String.valueOf(projectId) });

        try {
            if (cursor.moveToFirst()) {
                project = cursorToProject(cursor);
                project.setMembers(getProjectMembers(projectId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return project;
    }

    /**
     * Get members of a project
     * 
     * @param projectId The project ID
     * @return List of members assigned to the project
     */
    public List<Member> getProjectMembers(int projectId) {
        List<Member> members = new ArrayList<>();

        String SELECT_QUERY = "SELECT m.* FROM " + TABLE_MEMBERS + " m "
                + "INNER JOIN " + TABLE_PROJECT_MEMBERS + " pm ON m." + KEY_MEMBER_ID + " = pm." + KEY_PM_MEMBER_ID
                + " WHERE pm." + KEY_PM_PROJECT_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[] { String.valueOf(projectId) });

        try {
            if (cursor.moveToFirst()) {
                do {
                    Member member = cursorToMember(cursor);
                    members.add(member);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return members;
    }

    /**
     * Update a project
     * 
     * @param project The project to update
     * @return Number of rows affected
     */
    public int updateProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_TITLE, project.getTitle());
        values.put(KEY_PROJECT_DESCRIPTION, project.getDescription());
        values.put(KEY_PROJECT_STATUS, project.getStatus());

        if (project.getDueDate() != null) {
            values.put(KEY_PROJECT_DUE_DATE, dateFormat.format(project.getDueDate()));
        }

        int rowsAffected = db.update(TABLE_PROJECTS, values,
                KEY_PROJECT_ID + " = ?", new String[] { String.valueOf(project.getId()) });

        // Update project members
        if (project.getMembers() != null) {
            // Remove existing members
            db.delete(TABLE_PROJECT_MEMBERS, KEY_PM_PROJECT_ID + " = ?",
                    new String[] { String.valueOf(project.getId()) });

            // Add updated members
            for (Member member : project.getMembers()) {
                addMemberToProject(db, project.getId(), member.getId());
            }
        }

        return rowsAffected;
    }

    /**
     * Delete a project
     * 
     * @param projectId The project ID to delete
     * @return Number of rows affected
     */
    public int deleteProject(int projectId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete project members first
        db.delete(TABLE_PROJECT_MEMBERS, KEY_PM_PROJECT_ID + " = ?",
                new String[] { String.valueOf(projectId) });

        // Delete project
        return db.delete(TABLE_PROJECTS, KEY_PROJECT_ID + " = ?",
                new String[] { String.valueOf(projectId) });
    }

    // ===================== MEMBER OPERATIONS =====================

    /**
     * Get all members from the database
     * 
     * @return List of all members
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();

        String SELECT_QUERY = "SELECT * FROM " + TABLE_MEMBERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Member member = cursorToMember(cursor);
                    members.add(member);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return members;
    }

    /**
     * Get a member by ID
     * 
     * @param memberId The member ID
     * @return The member, or null if not found
     */
    public Member getMemberById(int memberId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Member member = null;

        String SELECT_QUERY = "SELECT * FROM " + TABLE_MEMBERS + " WHERE " + KEY_MEMBER_ID + " = ?";
        Cursor cursor = db.rawQuery(SELECT_QUERY, new String[] { String.valueOf(memberId) });

        try {
            if (cursor.moveToFirst()) {
                member = cursorToMember(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return member;
    }

    // ===================== HELPER METHODS =====================

    /**
     * Convert cursor to Project object
     */
    private Project cursorToProject(Cursor cursor) {
        Project project = new Project();
        project.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROJECT_ID)));
        project.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_TITLE)));
        project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_DESCRIPTION)));
        project.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_STATUS)));
        project.setCreatorId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PROJECT_CREATOR_ID)));

        // Parse dates
        try {
            String createdAtStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_CREATED_AT));
            if (createdAtStr != null) {
                project.setCreatedAt(dateFormat.parse(createdAtStr));
            }

            String dueDateStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROJECT_DUE_DATE));
            if (dueDateStr != null) {
                project.setDueDate(dateFormat.parse(dueDateStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return project;
    }

    /**
     * Convert cursor to Member object
     */
    private Member cursorToMember(Cursor cursor) {
        Member member = new Member();
        member.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_MEMBER_ID)));
        member.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEMBER_NAME)));
        member.setRole(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEMBER_ROLE)));
        member.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEMBER_AVATAR_URL)));
        return member;
    }

    /**
     * Get the count of projects
     * 
     * @return Number of projects in the database
     */
    public int getProjectCount() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_PROJECTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }
}
