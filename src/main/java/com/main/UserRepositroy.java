package com.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

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
    int getPostsCount(@Param("id") Long id);
    Relationship isFollowingId(@Param("id") Long id,@Param("profileid") Long profileId);
    List<HashMap<Long,String>> findUserByNameLike(@Param("keyword") String keyword);
    @Modifying
    @Transactional
    void removeRelationship(@Param("id") Long id,@Param("profileid") Long profileId);
}
