package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.CommonLeaderboardNotification;
import com.dojo.notifications.model.notification.PersonalLeaderboardNotification;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeaderboardNotifierService {

    private static final String NOTIFYING_MESSAGE = "There are changes in leaderboard!";
    private final String NEW_LEADERBOARD_NAME = "NewLeaderboard";
    private final String OLD_LEADERBOARD_NAME = "OldLeaderboard";


    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardNotifierService.class);

    private final UserDetailsService userDetailsService;
    private final LeaderboardService leaderboardService;
    private final SelectRequestService selectRequestService;
    private final Map<NotifierType, NotificationService> notificationServices;
    private final Map<String, Leaderboard> leaderboards;

    @Autowired
    public LeaderboardNotifierService(UserDetailsService userDetailsService, LeaderboardService leaderboardService,
                                      SelectRequestService selectRequestService, Collection<NotificationService> notificationServices) {
        this.userDetailsService = userDetailsService;
        this.leaderboardService = leaderboardService;
        this.selectRequestService = selectRequestService;
        this.leaderboards = new ConcurrentHashMap<>();
        this.notificationServices = notificationServices.stream()
                .collect(Collectors.toMap(NotificationService::getNotificationServiceTypeMapping, Function.identity()));
    }

    public DataStream<Tuple3<String, String, Long>> convertToDataStreamFromTuple(StreamExecutionEnvironment env, List<Participant> participants) {
        List<Tuple3<String, String, Long>> tupleLeaderboard = new ArrayList<>();

        participants.forEach(participant -> {
            Tuple3<String, String, Long> oneRow = new Tuple3<>();
            oneRow.f0 = participant.getUser().getId();
            oneRow.f1 = participant.getUser().getName();
            oneRow.f2 = participant.getScore();

            tupleLeaderboard.add(oneRow);
        });
        return env.fromCollection(tupleLeaderboard);
    }

    public TableResult executeQuery(String query, String table, StreamTableEnvironment tableEnv) {
        String leaderboardQuery = String.format(query, table);
        return tableEnv.sqlQuery(leaderboardQuery).execute();
    }

    public List<Participant> convertTableResultToList(TableResult leaderboard) {
        CloseableIterator<Row> rowsNew = leaderboard.collect();

        List<Participant> participants = new ArrayList<>();
        rowsNew.forEachRemaining(row -> {
            Participant p = new Participant();
            p.setUser(new UserInfo((String) row.getField(0), (String) row.getField(1)));
            p.setScore((long) row.getField(2));
            participants.add(p);
        });
        return participants;
    }


    public void getGRPCLeaderboardChanges(Contest contest, List<Participant> updated, List<Participant> old) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        DataStream<Tuple3<String, String, Long>> updatedL = convertToDataStreamFromTuple(env, updated);
        DataStream<Tuple3<String, String, Long>> oldL = old.size() == 0 ? updatedL : convertToDataStreamFromTuple(env, old);

        tableEnv.createTemporaryView(NEW_LEADERBOARD_NAME, updatedL);
        tableEnv.createTemporaryView(OLD_LEADERBOARD_NAME, oldL);

        List<SelectRequest> queries = selectRequestService.getRequests();

        queries.forEach(selectRequest -> {

            TableResult newLeaderboard = executeQuery(selectRequest.getQUERY(), NEW_LEADERBOARD_NAME, tableEnv);
            TableResult oldLeaderboard = executeQuery(selectRequest.getQUERY(), OLD_LEADERBOARD_NAME, tableEnv);


            List<Participant> participantsNew = convertTableResultToList(newLeaderboard);
            List<Participant> participantsOld = convertTableResultToList(oldLeaderboard);

            if (!participantsNew.equals(participantsOld)) {
                notifyAbout(contest, participantsNew, participantsOld, selectRequest);
            }
        });

    }

    public void notifyAbout(Contest contest, List<Participant> participantsNew, List<Participant> participantsOld, SelectRequest selectRequest) {
        if (!participantsNew.equals(participantsOld)) {

            Leaderboard notifyPeople = new Leaderboard(participantsNew);
            Leaderboard oldLeaderboardN = new Leaderboard(participantsOld);
            EventType e = EventType.valueOf(selectRequest.getEVENT_TYPE());
            EventType actual = leaderboardService.determineEventType(notifyPeople, oldLeaderboardN);

            if (e.equals(actual)) {
                notifyPersonal(contest, notifyPeople, oldLeaderboardN);
            }

            notifyAboutChanges(contest, notifyPeople, oldLeaderboardN, e);
        }
    }

    public void lookForLeaderboardChanges(final Contest contest, Leaderboard newLeaderboard) {
        Leaderboard oldLeaderboard = leaderboards.get(contest.getContestId());

        List<Participant> participants = newLeaderboard.sort();
        if (oldLeaderboard != null && !newLeaderboard.equals(oldLeaderboard)) {

            try {
                getGRPCLeaderboardChanges(contest, participants, oldLeaderboard.getParticipants());
            } catch (Exception e) {
                e.printStackTrace();
            }
            //this.notifyAboutChanges(contest, newLeaderboard, oldLeaderboard);
        }

        leaderboards.put(contest.getContestId(), new Leaderboard(participants));
    }

    public void notifyAboutChanges(final Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard, EventType type) {
        LOGGER.info(NOTIFYING_MESSAGE);

        EventType changesEventType = leaderboardService.determineEventType(newLeaderboard, oldLeaderboard);
//
//        if (type == EventType.POSITION_CHANGES) {
//            notifyPersonal(contest, newLeaderboard, oldLeaderboard);
//        }

        contest.getCommonNotificationsLevel().entrySet().stream()
                .filter(entry -> entry.getValue().getIncludedEventTypes().contains(type))
                .forEach(entry -> notifyCommon(contest, newLeaderboard, entry.getKey()));
    }

    private void notifyPersonal(Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        List<UserDetails> userDetails = leaderboardService.getUserDetails(newLeaderboard, oldLeaderboard);

        userDetails.forEach(user -> {
            for (NotifierType notifierType : contest.getPersonalNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new PersonalLeaderboardNotification(userDetailsService, newLeaderboard, user), contest);
            }
        });
    }

    private void notifyCommon(Contest contest, Leaderboard newLeaderboard, NotifierType notifierType) {
        notificationServices.get(notifierType)
                .notify(new CommonLeaderboardNotification(userDetailsService, newLeaderboard), contest);
    }

}
