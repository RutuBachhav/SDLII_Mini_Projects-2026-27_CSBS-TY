package com.splitease.dto;

import java.util.ArrayList;
import java.util.List;

/** JSON body of POST /api/groups */
public class GroupRequest {

    private String name;
    private String description;
    private List<Integer> memberIds = new ArrayList<>();

    public GroupRequest() { }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Integer> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Integer> memberIds) { this.memberIds = memberIds; }
}
