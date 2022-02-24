package com.dojo.notifications.service.notifierService;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.notification.NotifierRequestModel;
import com.dojo.notifications.model.notification.ParticipantNotification;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.FlinkTableService;
import com.dojo.notifications.service.SelectRequestService;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.notificationService.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DockerNotifierService {

    private static final String ERROR_MESSAGE = "Unable to execute select request %s";
    private static final String RECEIVER_COMMON = "Common";
    private static final String TABLE_NAME = "docker_events";

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerNotifierService.class);

    private final UserDetailsService userDetailsService;
    private final SelectRequestService selectRequestService;
    private final FlinkTableService flinkTableService;
    private final Map<NotifierType, NotificationService> notificationServices;
    private final Map<String, List<Object>> dockerEvents;

    @Autowired
    public DockerNotifierService(UserDetailsService userDetailsService, SelectRequestService selectRequestService, FlinkTableService flinkTableService, Collection<NotificationService> notificationServices) {
        this.userDetailsService = userDetailsService;
        this.selectRequestService = selectRequestService;
        this.flinkTableService = flinkTableService;
        this.notificationServices = notificationServices.stream()
                .collect(Collectors.toMap(NotificationService::getNotificationServiceTypeMapping, Function.identity()));
        this.dockerEvents = new HashMap<>();
    }

    public Map<String, List<Object>> getDockerEvents() {
        return Collections.unmodifiableMap(this.dockerEvents);
    }

    public void executeRequests(Contest contest, String id, Object object, String message) {

        if (object instanceof Container) {
            Container container = (Container) object;
            if (container.getStatus().equals("running")) {
                this.dockerEvents.put(id, new ArrayList<>());
            }
        }
        this.dockerEvents.get(id).add(object);

        Set<SelectRequest> contestRequests = selectRequestService.getSpecificRequests(contest.getQueryIds(), selectRequestService.getRequestsForTable(TABLE_NAME));
        for (SelectRequest request : contestRequests) {
            try {
                List<String> queriedIds = flinkTableService.executeDockerQuery(request, object, id);
                if (!queriedIds.isEmpty()) {
                    NotificationType finalType = (object instanceof Container) ? NotificationType.CONTAINER : NotificationType.TEST_RESULTS;
                    queriedIds.forEach(queriedId -> notify(new NotifierRequestModel(request.getReceivers(), queriedId, contest, object, message, finalType, request.getId())));
                }

            } catch (Exception e) {
                LOGGER.error(String.format(ERROR_MESSAGE, request.getQuery()) + e);
            }
        }
    }

    private void notify(NotifierRequestModel notifierRequestModel) {

        notifyParticipant(notifierRequestModel);

        if (notifierRequestModel.getReceivers() != null) {
            notifyListeners(notifierRequestModel, userDetailsService.turnUsersToUserIds(notifierRequestModel.getReceivers()));
            if (notifierRequestModel.getReceivers().contains(RECEIVER_COMMON)) {
                notifySensei(notifierRequestModel);
            }
        }
    }

    private void notifyListeners(NotifierRequestModel notifierRequestModel, Set<String> eventListenerIds) {
        List<UserDetails> userDetails = new ArrayList<>();
        eventListenerIds.forEach(id -> userDetails.add(userDetailsService.getUserDetailsById(id)));

        for (UserDetails user : userDetails) {
            for (NotifierType notifierType : notifierRequestModel.getContest().getNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new SenseiNotification(userDetailsService, notifierRequestModel.getObject(), notifierRequestModel.getMessage(), notifierRequestModel.getNotificationType()), notifierRequestModel.getContest());
            }
        }
    }

    public void notifyParticipant(NotifierRequestModel notifierRequestModel) {
        UserDetails userDetails = userDetailsService.getUserDetailsById(notifierRequestModel.getQueriedId());
        userDetailsService.getUserSubscriptionForQuery(userDetails, notifierRequestModel.getRequestId(), notifierRequestModel.getContest().getContestId());

        for (NotifierType notifierType : notifierRequestModel.getContest().getNotifiers()) {
            notificationServices.get(notifierType)
                    .notify(userDetails, new ParticipantNotification(userDetailsService, notifierRequestModel.getObject(), userDetails, notifierRequestModel.getMessage(), notifierRequestModel.getNotificationType()), notifierRequestModel.getContest());
        }
    }

    public void notifySensei(NotifierRequestModel notifierRequestModel) {
        notifierRequestModel.getContest().getNotifiers().forEach(notifierType -> notificationServices.get(notifierType)
                .notify(new SenseiNotification(userDetailsService, notifierRequestModel.getObject(), notifierRequestModel.getMessage(), notifierRequestModel.getNotificationType()), notifierRequestModel.getContest()));
    }
}
