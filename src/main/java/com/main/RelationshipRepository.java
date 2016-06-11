package com.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Nagyp on 2016.03.09..
 */
@Repository
public interface RelationshipRepository extends JpaRepository<Relationship,Long>{
}
