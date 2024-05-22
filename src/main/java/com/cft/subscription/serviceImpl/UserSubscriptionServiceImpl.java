package com.cft.subscription.serviceImpl;

import com.cft.appservice.constant.Status;
import com.cft.appservice.constant.UserSubscriptionNotification;
import com.cft.appservice.core.NotificationPackage;
import com.cft.appservice.service.CacheService;
import com.cft.appservice.service.UsersCache;
import com.cft.appservice.service.impl.NotificationServiceImpl;
import com.cft.persistence.model.UserSubscription;
import com.cft.persistence.model.dto.dto.EmailTemplateDTO;
import com.cft.persistence.model.dto.dto.UserDTO;
import com.cft.persistence.model.dto.dto.UserSubscriptionDTO;
import com.cft.persistence.repository.UserRepository;
import com.cft.persistence.repository.UserSubscriptionRepository;
import com.cft.persistence.service.DbEmailTemplateService;
import com.cft.subscription.model.SubscriptionExpiry;
import com.cft.subscription.repository.SubscriptionExpiryRepository;
import com.cft.subscription.service.UserSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private UsersCache userCache;

    @Autowired
    NotificationServiceImpl notificationService;

    @Autowired
    private DbEmailTemplateService emailTemplateService;

    private List<EmailTemplateDTO> templates;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SubscriptionExpiryRepository subscriptionExpiryRepository;

    private static final String UTILS_ONFIG_ID = "1";

   @Scheduled(cron= "${subscription.expiry.notification.cron}")
   @Override
    public void getUserSubscriptions() {
        List<UserSubscription> subscription= userSubscriptionRepository.findAll();
        Set<UserSubscriptionDTO> userSubscriptionDTO = null;

        if(subscription!= null){
            userSubscriptionDTO =  subscription.stream()
                    .map(UserSubscriptionDTO::getDTO)
                    .collect(Collectors.toSet());
        }
        expireExpiredSubscription(userSubscriptionDTO);
        subscriptionExpiryNotification(userSubscriptionDTO);

    }
    @Override
    public void updateUserStatusInCache(int userId, Status status) {
        userCache.getAndUpdate(userId, (user) -> user.setStatus(status));
    }
    public void subscriptionExpiryNotification(Set<UserSubscriptionDTO> userSubscriptionDTO) {

        NotificationPackage notificationPackage = new NotificationPackage();
        SubscriptionExpiry subscriptionExpiry = subscriptionExpiryRepository.findById(Integer.valueOf((UTILS_ONFIG_ID))).orElse(null);
        List<String> expiryDays = new ArrayList<>();
        if (subscriptionExpiry != null) {
            expiryDays = List.of(subscriptionExpiry.getConfig_value().split(","));
        }

        Calendar c = Calendar.getInstance();

        for (UserSubscriptionDTO subscription : userSubscriptionDTO) {
            if (isUserActive(subscription)) {
                // Check if subscription is about to expire
                for (String subscriptionExpiryDays : expiryDays) {
                    c.setTime(new Date());
                    c.add(Calendar.DATE, Integer.parseInt(subscriptionExpiryDays));
                    Date expiryDate = c.getTime();

                    LocalDate activeTillDate = subscription.getActiveTill().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    if (activeTillDate.equals(expiryLocalDate)) {
                        log.info("Subscription is about to expire for user: " + subscription.getUser());

                        setNotificationPackage(notificationPackage, subscription);
                        notificationService.send(notificationPackage);
                        //Send notification subscription about to expire email
                    }
                }

            }
        }
    }

    @Transactional(rollbackOn = Exception.class)
    public void expireExpiredSubscription(Set<UserSubscriptionDTO> userSubscriptionDTO) {
        for(UserSubscriptionDTO subscription : userSubscriptionDTO){
            if(isUserActive(subscription)){
                if (subscription.getActiveTill().before(new Date())) {
                    subscription.setStatus(Status.EXPIRED);
                    userSubscriptionRepository.save(subscription.getEntity());
                    updateUserStatusInCache(subscription.getUser(), subscription.getStatus());
                }
            }

        }

    }

    public boolean isUserActive(UserSubscriptionDTO subscription) {
        boolean isActive = false;
        if (subscription.getStatus() == Status.ACTIVE) {
            isActive = true;
            return isActive;
        }
        return isActive;
    }

    public void setNotificationPackage(NotificationPackage notificationPackage, UserSubscriptionDTO userSubscriptionDTO) {

        templates = emailTemplateService.getAll();
        UserDTO userDTO = UserDTO.getDTO(userRepository.findByUserId(userSubscriptionDTO.getUser()));
        notificationPackage.setNotification(UserSubscriptionNotification.SUBSCRIPTION_EXPIRY_NOTIFICATION);
        notificationPackage.setUser(userDTO);
        notificationPackage.setParams(userSubscriptionDTO.getNotificationParams());

    }


}
