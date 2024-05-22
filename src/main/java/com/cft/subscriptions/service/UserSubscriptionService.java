package com.cft.subscriptions.service;

import com.cft.appservice.constant.Status;
import com.cft.persistence.model.User;
import com.cft.persistence.model.dto.dto.UserDTO;
import com.cft.persistence.model.dto.dto.UserSubscriptionDTO;

public interface UserSubscriptionService {

    void getUserSubscriptions();
    void updateUserStatusInCache(int userId, Status status);

}
