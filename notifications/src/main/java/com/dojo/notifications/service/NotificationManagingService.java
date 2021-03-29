package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationManagingService {

    private final List<String> subscriptions;
    private final NotificationClient notificationClient;

    @Value("${thread-pool-size}")
    private int poolSize;
    @Value("${thread-pool-schedule-period-seconds}")
    private int schedulePeriod;
    private final ScheduledExecutorService executorService;


    @Autowired
    public NotificationManagingService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
        this.subscriptions = new ArrayList<>();
        this.executorService = Executors.newScheduledThreadPool(poolSize);
    }

    public void startNotifications(final Contest contest){
        notificationClient.startLeaderboardNotifications(contest);
        subscriptions.add(contest.getContestId());

    }

    public void stopNotifications(final String contestId){
        notificationClient.stopLeaderboardNotifications(contestId);
        subscriptions.remove(contestId);
    }

    @PreDestroy
    private void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
