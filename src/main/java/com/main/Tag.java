package com.main;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Nagyp on 2016.03.08..
 */
@Entity
public class Tag {
    @Id
    @GeneratedValue
    private Long Id;

    private String name;

    @ManyToMany
    private List<Photo> photo;

    public Long getId() {
        return Id;
    }

    public List<Photo> getPhoto() {
        return photo;
    }

    public void setPhoto(List<Photo> photo) {
        this.photo = photo;
    }

    public void setId(Long id) {
        Id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
