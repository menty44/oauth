package com.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

@RestController
@PropertySources(value = {@PropertySource("classpath:application.properties")})
@RequestMapping("/")
class UserRestController{


    private final UserRepositroy userRepositroy;
    private final PhotoRepository photoRepository;
    private final RelationshipRepository relationshipRepository;


    @Autowired
    private MultipartResolver multipartResolver;

    @RequestMapping({"/user","/me"})
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

            User doe = new User();
            doe.setName("Peti");
            doe.setFacebookId(Long.valueOf(111).longValue());
            doe.setDateRegistered(new Date());
            doe.setProfilePicturePath("images/3.jpeg");

            Photo photo = new Photo();
            photo.setDateUpdated(new Date());
            photo.setCaption("nice photo");
            photo.setPath("images/1.jpg");
            photoRepository.save(photo);

            Photo photo2 = new Photo();
            photo.setDateUpdated(new Date());
            photo.setCaption("nice photo");
            photo.setPath("images/2.jpg");
            photoRepository.save(photo2);

            doe.getPhotos().add(photo);
            doe.getPhotos().add(photo2);
            userRepositroy.save(doe);
            photo.setUploader(doe);
            photo2.setUploader(doe);
            photoRepository.save(photo);
            photoRepository.save(photo2);


            userRepositroy.save(result);
            Relationship rel =  new Relationship();
            rel.setFollowed(doe);
            rel.setFollower(result);
            relationshipRepository.save(rel);
            result = userRepositroy.findByFacebookId(facebookTokenId);
            result.getFollowing().add(rel);

            User harmadik = new User();
            harmadik.setName("John Doe");
            harmadik.setDateRegistered(new Date());
            harmadik.setFacebookId((long)112233);
            userRepositroy.save(harmadik);
            result = userRepositroy.findByFacebookId(facebookTokenId);
            result.getFollowing().add(rel);

        }

        HashMap<String,Object> map = new LinkedHashMap();
        map.put("name",name);
        map.put("id",result.getId());
        return map;
    }

    @RequestMapping(value = "/adduser",method = RequestMethod.POST)
    ResponseEntity<User> addUser(@RequestBody User user){
        User newUser = user;
        if(newUser != null)
            userRepositroy.save(newUser);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @RequestMapping(value = "/getProfilePicture/{id}",method = RequestMethod.GET)
    public HttpEntity<byte[]> getSingleProfilePicture(@PathVariable String id, WebRequest request) throws IOException{
        User usr = userRepositroy.getOne(Long.valueOf(id));

        String name = usr.getProfilePicturePath().split("/")[2];
        System.out.println(name);
        File photo = new File("images",name);
        if (!photo.exists()){
            System.out.print("Photo not found");
        }

        if (request.checkNotModified(photo.lastModified()))
            return null;
        System.out.print(photo.getPath());
        byte[] photoFile = Files.readAllBytes(Paths.get(photo.getPath()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(photoFile.length);
        headers.setLastModified(photo.lastModified());
        return new HttpEntity<byte[]>(photoFile,headers);

    }

    @RequestMapping(value = "uploadProfilePicture/{id}",method = RequestMethod.POST)
    public void saveSingleProfileImage(@PathVariable String id,@RequestParam("name") String name,@RequestParam("img") MultipartFile file){
        if(!file.isEmpty()){
            try{
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File("images/"+name)));
                FileCopyUtils.copy(file.getInputStream(),stream);
                stream.close();

                User usr = userRepositroy.findOne(Long.valueOf(id));
                usr.setProfilePicturePath("/images/"+name);
                userRepositroy.save(usr);
                System.out.print("Uploaded & saved");
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            System.out.print("Empty file");
        }
    }

    @RequestMapping(value = "updateProfile/{id}",method = RequestMethod.POST)
    public void updateProfile(@PathVariable String id,@RequestParam("email") String email,@RequestParam("desc") String desc,@RequestParam String weburl){

        User usr = userRepositroy.findOne(Long.valueOf(id));
        if(!email.isEmpty())
            usr.setEmail(email);
        if(!desc.isEmpty())
            usr.setDescription(desc);
        if(!weburl.isEmpty())
            usr.setWebsite(weburl);
        userRepositroy.save(usr);
    }


    @Autowired
    public UserRestController(UserRepositroy userRepositroy,PhotoRepository photoRepository,RelationshipRepository relationshipRepository){
        this.userRepositroy = userRepositroy;
        this.photoRepository = photoRepository;
        this.relationshipRepository = relationshipRepository;
    }

}