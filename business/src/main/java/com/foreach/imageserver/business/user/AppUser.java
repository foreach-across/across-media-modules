package com.foreach.imageserver.business.user;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

public class AppUser implements Serializable {

	private static final long serialVersionUID = 1L;

    private int id;

    private String username;
    private String password;

    private boolean administrator;
    private boolean active;

    public final int getId() {
	    return id;
	}

	public final void setId(int id) {
	    this.id = id;
	}

    public final String getUsername() {
        return username;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final String getPassword() {
        return password;
    }

    public final void setPassword(String password) {
        this.password = password;
    }

    public final boolean isAdministrator()
	{
		return administrator;
	}

	public final void setAdministrator( boolean administrator )
	{
		this.administrator = administrator;
	}

    public final boolean isActive()
	{
		return active;
	}

	public final void setActive( boolean active )
	{
		this.active = active;
	}

    @SuppressWarnings("all")
    @Override
    public String toString() {
        return username + " " + password;
    }

    @SuppressWarnings("all")
    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AppUser appAppUser = (AppUser) o;

        if (StringUtils.equals(username, appAppUser.username)) {
            return false;
        }
        if (StringUtils.equals(password, appAppUser.password)) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("all")
    @Override
    public final int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);

        return result;
    }

}
