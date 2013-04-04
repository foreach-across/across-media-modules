package com.foreach.imageserver.business.taxonomy;

import org.apache.commons.lang.StringUtils;

public class UserGroup {
    private String userKey;
    private int groupId;

    public final String getUserKey() {
        return userKey;
    }

    public final void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public final int getGroupId() {
        return groupId;
    }

    public final void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        UserGroup other = (UserGroup) o;

        if (StringUtils.equals(userKey, other.userKey)) {
            return false;
        }
        if (groupId != other.groupId) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public final int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userKey != null ? userKey.hashCode() : 0);
        result = 31 * result + Long.valueOf(groupId).intValue();

        return result;
    }

}
