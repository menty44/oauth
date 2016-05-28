package com.main;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Nagyp on 2016.03.09..
 */
@Entity
public class Relationship implements Serializable {

    @Id
    @GeneratedValue
    private  long Id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "follower")
    private User follower;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "followed")
    private User followed;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowed() {
        return followed;
    }

    public void setFollowed(User followed) {
        this.followed = followed;
    }

    public Relationship(){

    }
}
