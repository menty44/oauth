package com.main;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Nagyp on 2016.03.06..
 */
@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = "User.getFollowed",resultClass = User.class, query = "SELECT * FROM user LEFT JOIN relationship on relationship.follower = :id WHERE relationship.followed = user.id"),
        @NamedNativeQuery(name = "User.findByIdIs", query = "SELECT DISTINCT photo.uploader_Id, user.name, photo.date_updated,photo.caption, photo.id FROM user,relationship,photo WHERE relationship.follower = :id AND (uploader_id = user.id AND (user.id = relationship.followed) AND (path <> '' OR path iS NOT NULL)) OR (uploader_id = :id AND uploader_id = user.id) ORDER BY photo.date_updated DESC"),
        @NamedNativeQuery(name = "User.getFollower", resultClass = User.class, query = "SELECT * FROM user LEFT JOIN relationship on relationship.followed = :id WHERE relationship.follower = user.id\n"),
        @NamedNativeQuery(name = "User.getPostsCount", query="SELECT COUNT(id) FROM photo WHERE uploader_id = :id"),
        @NamedNativeQuery(name = "User.isFollowingId", resultClass = Relationship.class, query = "SELECT DISTINCT * FROM relationship WHERE follower = :id AND followed = :profileid"),
        @NamedNativeQuery(name = "User.removeRelationship", query = "DELETE FROM relationship WHERE follower = :id AND followed = :profileid"),
        @NamedNativeQuery(name = "User.findUserByNameLike", query = "SELECT id,name FROM user WHERE name LIKE :keyword")

})
public class User implements Serializable, UserDetails {

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_USER";
            }
        };
        grantedAuthorities.add(grantedAuthority);
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return facebookId.toString();
    }

    @Override
    public String getUsername() {
        return name.replaceAll("\\s+","");
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
