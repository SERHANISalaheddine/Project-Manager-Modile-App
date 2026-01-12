package com.example.ProjectManager.mappers;


import com.example.ProjectManager.models.Member;
import com.example.ProjectManager.models.dto.UserResponseDto;

public class MemberMapper {

    public static Member fromUser(UserResponseDto u) {

        String fullName = safe(u.getFirstName()) + " " + safe(u.getLastName());
        fullName = fullName.trim();

        if (fullName.isEmpty()) {
            fullName = u.getEmail() != null ? u.getEmail() : "Unknown";
        }

        String role = "Jr Front End Developer";

        int id = (u.getId() == null) ? -1 : u.getId().intValue();

        return new Member(id, fullName, role);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

