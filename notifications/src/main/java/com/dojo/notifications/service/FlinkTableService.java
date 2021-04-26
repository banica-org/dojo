package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FlinkTableService {

    private static final String TABLE_NAME = "Leaderboard";

    public Set<Participant> executeSingleQuery(SelectRequest request, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(executionEnvironment, settings);

        DataStream<Tuple3<String, String, Long>> tuple3DataStream = convertToTuple3DataStream(executionEnvironment, newLeaderboard.getParticipants());

        Table table = tableEnvironment.fromDataStream(tuple3DataStream).as("id", "name", "points");
        tableEnvironment.createTemporaryView(TABLE_NAME, table);

        try {
            table = tableEnvironment.sqlQuery(request.getQuery());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }

        tableEnvironment.dropTemporaryView(TABLE_NAME);

        DataStream<Tuple3<String, String, Long>> tupleStream = tableEnvironment.toAppendStream(
                table,
                new TypeHint<Tuple3<String, String, Long>>() {
                }.getTypeInfo()
        );

        return checkCondition(tupleStream, request, newLeaderboard, oldLeaderboard);
    }

    private Set<Participant> checkCondition(DataStream<Tuple3<String, String, Long>> tupleStream, SelectRequest request, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) throws Exception {
        Set<Participant> queriedParticipants = convertDataStreamToSet(tupleStream.executeAndCollect());

        if (request.getEventType().equals(String.valueOf(EventType.POSITION_CHANGES))) {
            return isPositionConditionMet(queriedParticipants, request, newLeaderboard, oldLeaderboard);
        } else if (request.getEventType().equals(String.valueOf(EventType.SCORE_CHANGES))) {
            return isScoreConditionMet(queriedParticipants, request, newLeaderboard, oldLeaderboard);
        }

        return Collections.emptySet();
    }

    private Set<Participant> isPositionConditionMet(Set<Participant> participants, SelectRequest request, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {
        Set<Participant> conditionMet = new TreeSet<>();

        for (Participant p : participants) {
            int oldPos = oldLeaderboard.getPositionByUserId(p.getUser().getId());
            int currentPos = newLeaderboard.getPositionByUserId(p.getUser().getId());

            if (request.getCondition() > 0 && oldPos - currentPos >= request.getCondition() && oldPos > currentPos) {
                conditionMet.add(p);
            } else if (request.getCondition() < 0 && oldPos - currentPos <= request.getCondition() && oldPos < currentPos) {
                conditionMet.add(p);
            }
        }
        return conditionMet;
    }

    private Set<Participant> isScoreConditionMet(Set<Participant> participants, SelectRequest request, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {
        Set<Participant> conditionMet = new TreeSet<>();

        for (Participant p : participants) {
            long oldScore = oldLeaderboard.getScoreByUserId(p.getUser().getId());
            long newScore = newLeaderboard.getScoreByUserId(p.getUser().getId());

            if (newScore - oldScore >= request.getCondition()) {
                conditionMet.add(p);
            }
        }
        return conditionMet;
    }

    private DataStream<Tuple3<String, String, Long>> convertToTuple3DataStream(StreamExecutionEnvironment env, Set<Participant> participants) {
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

    private Set<Participant> convertDataStreamToSet(Iterator<Tuple3<String, String, Long>> leaderboard) {

        TreeSet<Participant> participants = new TreeSet<>();

        leaderboard.forEachRemaining(user -> {
            UserInfo userInfo = new UserInfo(user.f0, user.f1);
            Participant participant = new Participant(userInfo, user.f2);
            participants.add(participant);
        });
        return participants;
    }

}
