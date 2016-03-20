package com.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Nagyp on 2016.03.08..
 */
@Repository
public interface LikeRepository extends JpaRepository<Likes,Long> {
}
