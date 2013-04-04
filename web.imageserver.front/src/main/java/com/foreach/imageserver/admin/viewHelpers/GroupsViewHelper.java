package com.foreach.imageserver.admin.viewHelpers;

import com.foreach.imageserver.business.taxonomy.Group;

import java.util.List;

public class GroupsViewHelper {
    private List<Group> list;

    public GroupsViewHelper(List<Group> groups) {
        this.list = groups;
    }

    public final List<Group> getList() {
        return list;
    }

    public final void setList(List<Group> list) {
        this.list = list;
    }

    public final int getNumberOfGroups() {
        return list.size();
    }
}
