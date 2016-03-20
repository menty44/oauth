package com.main;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Nagyp on 2016.03.07..
 */
@Entity
public class Photo implements Serializable{

    public Photo(){}

    @Id
    @GeneratedValue
    private Long Id;

    public String caption;
    public String path;
    public String tumbnailPath;
    public Date dateUpdated;
    public float lattitude;
    public float longitude;

    @OneToMany
    private List<Likes> likes;

    @ManyToMany(mappedBy = "photo")
    private List<Tag> tags;

    @ManyToOne
    @JsonBackReference
    private User uploader;

    public List<Likes> getLikes() {
        return likes;
    }

    public void setLikes(List<Likes> likes) {
        this.likes = likes;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public Long getId() {
        return Id;

    }

    public void setId(Long id) {
        Id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public float getLattitude() {
        return lattitude;
    }

    public void setLattitude(float lattitude) {
        this.lattitude = lattitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public void setTumbnailPath(String tumbnailPath){
        this.tumbnailPath = tumbnailPath;
    }

    public String getTumbnailPath(){
        return this.tumbnailPath;
    }



}
