package com.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.jws.soap.SOAPBinding;
import java.util.List;
import java.util.Optional;

/**
 * Created by Nagyp on 2016.03.06..
 */
@Repository
public interface UserRepositroy extends JpaRepository<User,Long>{
    User findByFacebookId(Long facebookId);
    @Query(nativeQuery = true)
    List findByIdIs(@Param("id") Long id);
    List<User> getFollowed(@Param("id") Long id);
    List<User> getFollower(@Param("id") Long id);
}
