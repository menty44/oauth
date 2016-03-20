package com.main;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Nagyp on 2016.03.06..
 */
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = "User.getFollowed",resultClass = User.class,query = "SELECT * FROM user LEFT JOIN relationship on relationship.follower = :id WHERE relationship.followed = user.id"),
        @NamedNativeQuery(name = "User.findByIdIs", query = "SELECT  photo.uploader_Id, user.name, user.profile_picture_path ,photo.path,photo.date_updated,photo.caption FROM user,relationship,photo WHERE relationship.follower = :id AND uploader_id = user.id AND (path <> '' OR path iS NOT NULL) ORDER BY photo.date_updated DESC"),
        @NamedNativeQuery(name = "User.getFollower",resultClass = User.class,query = "SELECT * FROM user LEFT JOIN relationship on relationship.followed = :id WHERE relationship.follower = user.id\n")
})
public class User implements Serializable {

    @Id
    @GeneratedValue
    private Long Id;

    public Long facebookId;
    public String name;
    public String email;
    public Date dateRegistered;
    public String description;
    public String website;

    public String profilePicturePath;


    @OneToMany(mappedBy = "uploader")
    @JsonManagedReference
    private List<Photo> photos;


    @OneToMany(mappedBy = "follower")
    @JsonManagedReference
    private Collection<Relationship> following;


    public Collection<Relationship> getFollowing() {
        return following;
    }

    public void setFollowing(List<Relationship> following) {
        this.following = following;
    }


    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(Long facebookId) {
        this.facebookId = facebookId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User() {
        this.setPhotos(new ArrayList<>());
        this.setFollowing(new ArrayList<>());
    }

}
