package com.main;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Nagyp on 2016.03.08..
 */

@Entity
@NamedNativeQueries({
        @NamedNativeQuery(name = "Likes.findByUserIdAndPostId", resultClass = Likes.class, query = "SELECT * FROM likes WHERE user_id = :id AND photo_id = :postId")
})
public class Likes implements Serializable{

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

    public Likes(){

    }

    public Likes(Long userId, long photoId, Date dateLiked){
        this.userId = userId;
        this.photoId = photoId;
        this.dateLiked = dateLiked;
    }


}
