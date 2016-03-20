package com.main;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Nagyp on 2016.03.08..
 */
@Entity
public class Likes {

    @Id
    @GeneratedValue
    private Long Id;

    private Long userId;
    private Long photoId;
    private Date dateLiked;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Date getDateLiked() {
        return dateLiked;
    }

    public void setDateLiked(Date dateLiked) {
        this.dateLiked = dateLiked;
    }


}
