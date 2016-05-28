package com.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;


/**
 * Created by Nagyp on 2016.03.07..
 */
@RestController
@RequestMapping("/{id}")
class DashController {

    private final UserRepositroy userRepositroy;
    private final PhotoRepository photoRepository;
    private final RelationshipRepository relationshipRepository;
    private final LikeRepository likeRepository;

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
        List<Map<String,Object>> list = new ArrayList<>();
        System.out.println("dash");
        List followed = userRepositroy.findByIdIs(id);

        for (int i = 0; i < followed.size();i++){
            Map<String,Object> objectMap = new HashMap<>();
            Object[] obj = (Object[]) followed.get(i);
            objectMap.put("id",obj[0]);
            objectMap.put("name",obj[1]);
            objectMap.put("uploaded",obj[2]);
            objectMap.put("caption",obj[3]);
            objectMap.put("photoId",obj[4]);
            list.add(objectMap);
        }
        return list;
    }

    @RequestMapping(value = "/profile",method = RequestMethod.GET)
    public User getUserProfileById(@PathVariable Long id){
        return userRepositroy.findOne(id);
    }

    @RequestMapping(value = "/posts/count",method = RequestMethod.GET)
    public HashMap<String,Integer> getPostsCountById(@PathVariable Long id) {
        HashMap<String,Integer> map = new LinkedHashMap<>();
        map.put("count",userRepositroy.getPostsCount(id));
        return map;
    }

    @RequestMapping(value = "/following/count",method = RequestMethod.GET)
    public HashMap<String,Integer> getFollowingCountById(@PathVariable Long id){
        HashMap<String,Integer> map = new LinkedHashMap<>();
        User usr =  userRepositroy.findOne(id);
        if (usr != null)
            map.put("count", usr.getFollowing().size());
        return map;
    }

    @RequestMapping(value = "/follower/count",method = RequestMethod.GET)
    public HashMap<String,Integer> getFollowerCountById(@PathVariable Long id){
        HashMap<String,Integer> map = new LinkedHashMap<>();
        List<User> list =  userRepositroy.getFollower(id);
        if(list!=null)
            map.put("count", list.size());
        return map;
    }


    @RequestMapping(value = "/isfollowing/{profileid}",method = RequestMethod.GET)
    public HashMap<String,Boolean> isFollowing(@PathVariable Long id,@PathVariable long profileid){
        HashMap<String,Boolean> map = new LinkedHashMap<>();
        Relationship relationship = userRepositroy.isFollowingId(id, profileid);
        map.put("following",relationship == null);
        return map;
    }

    @RequestMapping(value = "/likes/info/{postId}",method = RequestMethod.GET)
    public Map<String,Object> gePostLikeCountById(@PathVariable Long id,@PathVariable Long postId){
        HashMap<String,Object> map = new LinkedHashMap<>();

        Likes l = likeRepository.findByUserIdAndPostId(id,postId);
        int size = photoRepository.getOne(postId).getLikes().size();
        if(l == null){
            map.put("count",size);
            map.put("class","");
        }else{
            map.put("count",size);
            map.put("class","heart-liked");
        }
        return map;
    }

    @RequestMapping(value = "/post/belongstouser/{postId}",method = RequestMethod.GET)
    public HashMap<String,Boolean> belongsToUser(@PathVariable Long id,@PathVariable Long postId){
        HashMap<String,Boolean> map = new LinkedHashMap<>();
        if(photoRepository.getOne(postId).getUploader().getId() == id){
            map.put("belongs",true);
        }else{
            map.put("belongs",false);
        }
        return map;
    }

        @RequestMapping(value = "/post/remove/{postId}", method = RequestMethod.DELETE)
    public HashMap<String,Boolean> removePostById(@PathVariable Long id,@PathVariable Long postId){
            HashMap<String,Boolean> map = new LinkedHashMap<>();

            Photo photo = photoRepository.getOne(postId);
            User usr = userRepositroy.getOne(id);

            if(photo.getUploader().getId() == usr.getId()){
                try{
                    Files.delete(FileSystems.getDefault().getPath(photo.getPath()));
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                usr.getPhotos().remove(photo);
                userRepositroy.save(usr);
                photoRepository.delete(photo);
                map.put("success",true);
            }else{
                map.put("success",false);
            }
            return map;
        }

    @RequestMapping(value = "/likes/set/{postId}",method = RequestMethod.POST)
    public Map<String,String> setPostLikeById(@PathVariable Long id,@PathVariable Long postId) {
        HashMap<String,String> map = new LinkedHashMap<>();

        Likes l = likeRepository.findByUserIdAndPostId(id,postId);
        Photo photo = photoRepository.getOne(postId);
        if(l == null){
            Likes like = new Likes(id, postId, new Date());
            likeRepository.save(like);
            photo.getLikes().add(like);
            photoRepository.save(photo);
            map.put("action","like");
        }else{
            photo.getLikes().remove(l);
            photoRepository.save(photo);
            likeRepository.delete(l);
            map.put("action","unlike");
        }
        return map;
    }


    @RequestMapping(value = "/followAction/{profileId}",method = RequestMethod.GET)
    public Map<String,String> followAction( @PathVariable Long id,@PathVariable long profileId){
        HashMap<String,String> map = new LinkedHashMap<>();

        Relationship rel = userRepositroy.isFollowingId(id,profileId);
        if(rel == null){
            Relationship relationship = new Relationship();
            User followed = userRepositroy.findOne(profileId);
            User following = userRepositroy.findOne(id);
            User saveUser = userRepositroy.findOne(id);
            relationship.setFollowed(followed);
            relationship.setFollower(following);
            relationshipRepository.save(relationship);
            saveUser.getFollowing().add(relationship);
            userRepositroy.save(saveUser);
            map.put("label","UNFOLLOW");
        }else{
            userRepositroy.removeRelationship(id,profileId);
            map.put("label","FOLLOW");
        }
        return map;
    }


    @Autowired
    public DashController(UserRepositroy userRepositroy, PhotoRepository photoRepository, RelationshipRepository relationshipRepository,LikeRepository likeRepository){
        this.userRepositroy = userRepositroy;
        this.photoRepository = photoRepository;
        this.relationshipRepository = relationshipRepository;
        this.likeRepository = likeRepository;
    }

}
