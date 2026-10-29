package com.splitease.service;

import com.splitease.dao.GroupDAO;
import com.splitease.dao.UserDAO;
import com.splitease.dto.GroupRequest;
import com.splitease.exception.ApiException;
import com.splitease.model.Group;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Business rules for groups and their members. */
public class GroupService {

    private final GroupDAO groupDAO = new GroupDAO();
    private final UserDAO userDAO = new UserDAO();

    public List<Group> listGroups() {
        return groupDAO.findAll();
    }

    public Group getGroup(int id) {
        Group g = groupDAO.findById(id);
        if (g == null) {
            throw ApiException.notFound("Group " + id + " not found");
        }
        return g;
    }

    public Group createGroup(GroupRequest req) {
        if (req == null) {
            throw ApiException.badRequest("Request body is missing");
        }
        String name = UserService.trim(req.getName());
        if (name.length() < 2 || name.length() > 100) {
            throw ApiException.badRequest("Group name must be 2 to 100 characters");
        }
        Set<Integer> ids = new LinkedHashSet<>(req.getMemberIds());    // removes duplicates
        if (ids.size() < 2) {
            throw ApiException.badRequest("A group needs at least 2 members");
        }
        for (int id : ids) {
            if (userDAO.findById(id) == null) {
                throw ApiException.badRequest("User " + id + " does not exist");
            }
        }
        String desc = UserService.trim(req.getDescription());
        int groupId = groupDAO.insert(name, desc.isEmpty() ? null : desc, List.copyOf(ids));
        return getGroup(groupId);
    }

    public Group addMember(int groupId, int userId) {
        getGroup(groupId);
        if (userDAO.findById(userId) == null) {
            throw ApiException.badRequest("User " + userId + " does not exist");
        }
        if (groupDAO.isMember(groupId, userId)) {
            throw ApiException.conflict("This person is already in the group");
        }
        groupDAO.addMember(groupId, userId);
        return getGroup(groupId);
    }

    public void deleteGroup(int groupId) {
        getGroup(groupId);
        groupDAO.delete(groupId);
    }

    /** Used by other services: fails with 400 if the user is not part of the group. */
    void requireMember(int groupId, int userId, String role) {
        if (!groupDAO.isMember(groupId, userId)) {
            throw ApiException.badRequest(role + " (user " + userId + ") is not a member of this group");
        }
    }
}
