package com.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.util.List;


/**
 * Created by Nagyp on 2016.03.07..
 */
@RestController
@RequestMapping("/{id}")
class DashController {

    private final UserRepositroy userRepositroy;
    private final PhotoRepository photoRepository;

    @RequestMapping(value = "/following",method = RequestMethod.GET)
    public Object getFollowedById(@PathVariable Long id){

        return userRepositroy.getFollowed(id);
    }

    @RequestMapping(value = "/followers",method = RequestMethod.GET)
    public Object getFollowersForId(@PathVariable Long id){
        return userRepositroy.getFollower(id);
    }

    @RequestMapping(value = "/photos",method = RequestMethod.GET)
    public List<Photo> getPhotosByUserId(@PathVariable Long id){
        return userRepositroy.getOne(id).getPhotos();
    }


    @RequestMapping(value = "/dash",method = RequestMethod.GET)
    public Object getDashForId(@PathVariable Long id){
        System.out.println(id);
        List followed = userRepositroy.findByIdIs(id);

        return followed;
    }

    @RequestMapping(value = "/profile",method = RequestMethod.GET)
    public User getUserProfileById(@PathVariable Long id){
        return userRepositroy.findOne(id);
    }



    @Autowired
    public DashController(UserRepositroy userRepositroy, PhotoRepository photoRepository){
        this.userRepositroy = userRepositroy;
        this.photoRepository = photoRepository;
    }

}
