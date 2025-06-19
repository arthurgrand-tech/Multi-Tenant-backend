package com.ArthurGrand.module.Client.repository;

import com.ArthurGrand.module.Client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    boolean existsByEmail(String email);

    Optional<Client> findByEmail(String email);

    List<Client> findByIsActive(Boolean isActive);

    List<Client> findByClientNameContainingIgnoreCase(String clientName);

    @Query("SELECT c FROM Client c WHERE c.isActive = true ORDER BY c.clientName")
    List<Client> findAllActiveClients();

    @Query("SELECT c FROM Client c WHERE " +
            "(:clientName IS NULL OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:industry IS NULL OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :industry, '%'))) AND " +
            "(:isActive IS NULL OR c.isActive = :isActive)")
    List<Client> findClientsWithFilters(@Param("clientName") String clientName,
                                        @Param("email") String email,
                                        @Param("industry") String industry,
                                        @Param("isActive") Boolean isActive);
}