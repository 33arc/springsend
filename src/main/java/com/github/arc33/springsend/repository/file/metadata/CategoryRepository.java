package com.github.arc33.springsend.repository.file;

import com.github.arc33.springsend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@RepositoryRestResource(collectionResourceRel="fileCategories",path="fileCategories")
public interface CategoryRepository extends JpaRepository<Category, Long> {
}