package com.cft.subscription.repository;

import com.cft.subscription.model.SubscriptionExpiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionExpiryRepository extends JpaRepository<SubscriptionExpiry, Integer> {
}
