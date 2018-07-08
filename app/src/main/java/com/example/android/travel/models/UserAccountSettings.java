package com.example.android.travel.models;

/**
 * Created by user on 07-02-2018.
 */

public class UserAccountSettings {

    private String description;
    private String display_name;
    private long following;
    private long followers;
    private long posts;
    private String profile_photo;
    private String username;
    private String location;
    private String website;
    private String user_id;

    public UserAccountSettings(String description, String display_name, long following,
                               long followers, long posts, String profile_photo, String username,
                               String location, String website, String user_id) {
        this.description = description;
        this.display_name = display_name;
        this.following = following;
        this.followers = followers;
        this.posts = posts;
        this.profile_photo = profile_photo;
        this.username = username;
        this.location = location;
        this.website = website;
        this.user_id = user_id;
    }

    public UserAccountSettings() {
    }

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public long getFollowes() {
        return followers;
    }

    public void setFollowes(long followers) {
        this.followers = followers;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", following=" + following +
                ", followers=" + followers +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                ", location='" + location + '\'' +
                ", website='" + website + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
