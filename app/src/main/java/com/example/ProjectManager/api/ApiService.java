package com.example.ProjectManager.api;

import com.example.ProjectManager.utils.Constants;
import java.util.Map;
// import retrofit2.Call;
// import retrofit2.http.Body;
// import retrofit2.http.DELETE;
// import retrofit2.http.GET;
// import retrofit2.http.PATCH;
// import retrofit2.http.POST;
// import retrofit2.http.PUT;
// import retrofit2.http.Path;
// import retrofit2.http.QueryMap;

/**
 * API endpoints interface (placeholders). Methods are declared but not used.
 * When backend integration starts, uncomment and implement as needed.
 */
public interface ApiService {

    // ==== AUTH ====
    // @POST(Constants.AUTH_PREFIX + "/login")
    // Call<AuthResponseDto> login(@Body LoginRequestDto request);
    //
    // @POST(Constants.AUTH_PREFIX + "/register")
    // Call<UserResponseDto> register(@Body UserRequestDto request);

    // ==== USERS ====
    // @GET(Constants.USERS_PREFIX)
    // Call<PageResponse<UserResponseDto>> getUsers(@QueryMap Map<String, String>
    // pageable);
    //
    // @GET(Constants.USERS_PREFIX + "/{id}")
    // Call<UserResponseDto> getUser(@Path("id") long id);
    //
    // @DELETE(Constants.USERS_PREFIX + "/{id}")
    // Call<Void> deleteUser(@Path("id") long id);

    // ==== PROJECTS ====
    // @POST(Constants.PROJECTS_PREFIX)
    // Call<ProjectResponse> createProject(@Body CreateProjectRequest request);
    //
    // @GET(Constants.PROJECTS_PREFIX + "/{id}")
    // Call<ProjectResponse> getProject(@Path("id") long id);
    //
    // @PUT(Constants.PROJECTS_PREFIX + "/{id}")
    // Call<ProjectResponse> updateProject(@Path("id") long id, @Body
    // UpdateProjectRequest request);
    //
    // @DELETE(Constants.PROJECTS_PREFIX + "/{id}")
    // Call<Void> deleteProject(@Path("id") long id);
    //
    // @POST(Constants.PROJECTS_PREFIX + "/{id}/members")
    // Call<ProjectMemberResponse> addMember(@Path("id") long id, @Body
    // AddMemberRequest request);
    //
    // @DELETE(Constants.PROJECTS_PREFIX + "/{id}/members/{userId}")
    // Call<Void> removeMember(@Path("id") long id, @Path("userId") long userId);
    //
    // @GET(Constants.PROJECTS_PREFIX + "/{id}/members")
    // Call<PageResponse<RichProjectMembers>> listMembers(@Path("id") long id,
    // @QueryMap Map<String, String> pageable);

    // ==== TASKS ====
    // @POST(Constants.TASKS_PREFIX)
    // Call<TaskResponse> createTask(@Body CreateTaskRequest request);
    //
    // @GET(Constants.TASKS_PREFIX + "/{id}")
    // Call<TaskResponse> getTask(@Path("id") long id);
    //
    // @PUT(Constants.TASKS_PREFIX + "/{id}")
    // Call<TaskResponse> updateTask(@Path("id") long id, @Body UpdateTaskRequest
    // request);
    //
    // @DELETE(Constants.TASKS_PREFIX + "/{id}")
    // Call<Void> deleteTask(@Path("id") long id);
    //
    // @PATCH(Constants.TASKS_PREFIX + "/{id}/status")
    // Call<TaskResponse> updateTaskStatus(@Path("id") long id, @Body
    // UpdateTaskStatusRequest request);
    //
    // @GET(Constants.TASKS_PREFIX)
    // Call<PageResponse<RichTaskResponse>> filterTasks(@QueryMap Map<String,
    // String> query);

    // Note: Define PageResponse<T> later if needed when hooking backend.
}
