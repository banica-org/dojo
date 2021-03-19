package com.dojo.notifications.service;

import com.dojo.notifications.contest.Contest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class NotificationManagingService {

    private static final int INITIAL_DELAY = 0;
    private final Map<String, ScheduledFuture<?>> subscriptions;
    private final LeaderboardNotifierService leaderboardService;

    @Value("${thread-pool-size}")
    private int poolSize;
    @Value("${thread-pool-schedule-period-seconds}")
    private int schedulePeriod;
    private final ScheduledExecutorService executorService;

    @Autowired
    public NotificationManagingService(LeaderboardNotifierService leaderboardService) {
        this.leaderboardService = leaderboardService;
        this.subscriptions = new ConcurrentHashMap<>();
        this.executorService = Executors.newScheduledThreadPool(poolSize);
    }

    public void startNotifications(final Contest contest){
        ScheduledFuture<?> future = executorService.scheduleAtFixedRate(() ->leaderboardService.lookForLeaderboardChanges(contest), INITIAL_DELAY, schedulePeriod, TimeUnit.SECONDS);
        subscriptions.put(contest.getContestId(), future);
    }

    public void stopNotifications(final String contestId){
        Optional.ofNullable(subscriptions.get(contestId)).ifPresent(future -> {
            future.cancel(false);
            subscriptions.remove(contestId);
        });
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
