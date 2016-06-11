package com.main;

import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@RestController
@PropertySources(value = {@PropertySource("classpath:application.properties")})
@RequestMapping("/")
class UserRestController{


    private final UserRepositroy userRepositroy;
    private final PhotoRepository photoRepository;
    private final RelationshipRepository relationshipRepository;


    @Autowired
    private MultipartResolver multipartResolver;


    @RequestMapping({"/user"})
    public HashMap user(OAuth2Authentication principal){

        LinkedHashMap o = (LinkedHashMap) principal.getUserAuthentication().getDetails();
        String name = (String) o.get("name");
        Long facebookTokenId = Long.valueOf((String)o.get("id")).longValue();

        User result =  userRepositroy.findByFacebookId(facebookTokenId);

        if (result != null){
            if(result.getFacebookId().equals(facebookTokenId)){
                System.out.println("Welcome back");
            }else
                System.out.println("new user");
        }else {
            System.out.println("user not found, so creating it");
            result = new User();
            result.setName(name);
            result.setFacebookId(facebookTokenId);
            result.setDateRegistered(new Date());
            userRepositroy.save(result);

        }

        HashMap<String,Object> map = new LinkedHashMap();
        map.put("name",name);
        map.put("id",result.getId());
        return map;
    }

    @RequestMapping({"/me"})
    public Map<String,String> user(Principal principal){
        Map<String,String> map = new LinkedHashMap<>();
        map.put("name",principal.getName());
        return map;
    }

    @RequestMapping(value = "/adduser",method = RequestMethod.POST)
    ResponseEntity<User> addUser(@RequestBody User user){
        User newUser = user;
        if(newUser != null)
            userRepositroy.save(newUser);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @RequestMapping(value = "/addFbUser",method = RequestMethod.POST)
    HashMap<String,Object> addFbUser(@RequestParam("id") String id,@RequestParam("name") String name){
        HashMap<String,Object> map = new LinkedHashMap();
        Long uid = Long.valueOf(id);
        User existingUser = userRepositroy.findByFacebookId(uid);
        if (existingUser == null) {
            existingUser = new User();
            existingUser.setFacebookId(uid);
            existingUser.setName(name);
            userRepositroy.save(existingUser);
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(existingUser,null,existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        userRepositroy.save(existingUser);

        map.put("name",existingUser.getName());
        map.put("id",existingUser.getId());
        return map;
    }

    @RequestMapping(value = "/getProfilePicture/{id}",method = RequestMethod.GET)
    public HttpEntity<byte[]> getSingleProfilePicture(@PathVariable String id, WebRequest request) throws IOException{
        User usr = userRepositroy.getOne(Long.valueOf(id));
        String name = null;
        try{
             name = usr.getProfilePicturePath().split("/")[2];

        }catch (Exception e){
            name = "default.png";
        }
        File photo = new File("images",name);
        if (!photo.exists()){
            System.out.print("Photo not found");
        }

        if (request.checkNotModified(photo.lastModified()))
            return null;

        byte[] photoFile = Files.readAllBytes(Paths.get(photo.getPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(photoFile.length);
        headers.setLastModified(photo.lastModified());
        return new HttpEntity<byte[]>(photoFile,headers);

    }

    @RequestMapping(value = "uploadProfilePicture/{id}",method = RequestMethod.POST)
    public HashMap<String,Boolean> saveSingleProfileImage(@PathVariable String id,@RequestParam("name") String name,@RequestParam("img") MultipartFile file){
        HashMap<String,Boolean> map = new LinkedHashMap<>();
        if(!file.isEmpty()){
            try{
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File("images/"+name)));
                FileCopyUtils.copy(file.getInputStream(),stream);
                stream.close();

                User usr = userRepositroy.findOne(Long.valueOf(id));
                usr.setProfilePicturePath("/images/"+name);
                userRepositroy.save(usr);
                map.put("success",true);
            }catch (Exception e){
                System.out.println(e.getMessage());
                map.put("success",false);
            }
        }else{
            System.out.print("Empty file");
            map.put("success",false);
        }
        return map;
    }

    @RequestMapping(value = "updateProfile/{id}",method = RequestMethod.POST)
    public HashMap<String,Object> updateProfile(@PathVariable String id,@RequestParam("email") String email,@RequestParam("desc") String desc,@RequestParam("website") String weburl){
        HashMap<String,Object> map = new LinkedHashMap<>();
        User usr = userRepositroy.findOne(Long.valueOf(id));

        if(Validation.ValidateEmpty(email) == Validation.ErrorType.OK){
            if(Validation.ValidateEmail(email) == Validation.ErrorType.OK)
                usr.setEmail(email);
            else
                map.put("email_error","The given string is not an email address");
        }
        if(Validation.ValidateEmpty(desc) == Validation.ErrorType.OK) {
            usr.setDescription(desc);
        }
        if(Validation.ValidateEmpty(weburl) == Validation.ErrorType.OK){
            if(Validation.ValidateWebsite(weburl) == Validation.ErrorType.OK)
                usr.setWebsite(weburl);
            else
                map.put("website_error","The given url is not a web url");
        }
        userRepositroy.save(usr);
        map.put("success",true);
        return map;
    }

    @RequestMapping(value = "uploadPicture/{id}", method = RequestMethod.POST)
    public HashMap<String,Long> uploadPicture(@PathVariable String id,@RequestParam("name") String name,@RequestParam("caption") String caption,@RequestParam("image") MultipartFile img){
        HashMap<String,Long> map = new LinkedHashMap<>();
        if(!img.isEmpty()){
            try{
                int i = 0;
                String filename = "images/"+name;
                File file = new File(filename);
                while (file.exists()){
                    i++;
                    filename = "images/" +  Integer.toString(i) + name;
                    file = new File(filename);
                }
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                FileCopyUtils.copy(img.getInputStream(),stream);
                stream.close();
                User usr = userRepositroy.findOne(Long.valueOf(id));
                Photo p = new Photo();
                p.setUploader(usr);
                p.setCaption(caption);
                p.setDateUpdated(new Date());
                p.setPath(filename);
                photoRepository.save(p);
                usr.getPhotos().add(p);
                userRepositroy.save(usr);
                map.put("id",p.getId());

            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            System.out.print("Empty file");
        }
        return map;
    }

    @RequestMapping(value = "getPictureForId/{id}", method = RequestMethod.GET)
    public HttpEntity<byte[]> getPictureForId(@PathVariable String id, WebRequest request) throws IOException {

        String name = null;
        Photo img = photoRepository.findOne(Long.valueOf(id));
        try{
            name = img.getPath().split("/")[1];
        }catch (Exception e){
            name = "2.jpg";
        }
        File photo = new File("images",name);
        if (!photo.exists()){
            System.out.print("Photo not found");
        }

        if (request.checkNotModified(photo.lastModified()))
            return null;

        byte[] photoFile = Files.readAllBytes(Paths.get(photo.getPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(photoFile.length);
        headers.setLastModified(photo.lastModified());
        return new HttpEntity<byte[]>(photoFile,headers);
    }

    @RequestMapping(value = "/search/{keyword}",method = RequestMethod.GET)
    public Object search(@PathVariable String keyword){

        if(!keyword.isEmpty()) {
            List<HashMap<Long,String>> obj = userRepositroy.findUserByNameLike("%" + keyword + "%");
            return obj;
        }
        return null;
    }


    @Autowired
    public UserRestController(UserRepositroy userRepositroy,PhotoRepository photoRepository,RelationshipRepository relationshipRepository){
        this.userRepositroy = userRepositroy;
        this.photoRepository = photoRepository;
        this.relationshipRepository = relationshipRepository;
    }
}