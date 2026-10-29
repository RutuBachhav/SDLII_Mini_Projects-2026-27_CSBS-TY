package com.splitease.dto;

/** JSON body of POST /api/groups/{id}/members */
public class MemberRequest {

    private int userId;

    public MemberRequest() { }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
