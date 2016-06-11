package com.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Nagyp on 2016.03.07..
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo,Long>{
    List<Photo> findByUploader(User user);
}