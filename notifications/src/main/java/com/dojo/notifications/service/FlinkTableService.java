package com.dojo.notifications.service;

import com.dojo.notifications.model.leaderboard.Leaderboard;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FlinkTableService {

    private static final String TABLE_NAME = "Leaderboard";

    public Leaderboard executeSingleQuery(Set<Participant> participants, String query) throws Exception {

        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(executionEnvironment, settings);

        DataStream<Tuple3<String, String, Long>> tuple3DataStream = convertToTuple3DataStream(executionEnvironment, participants);

        Table table = tableEnvironment.fromDataStream(tuple3DataStream).as("id", "name", "points");
        tableEnvironment.createTemporaryView(TABLE_NAME, table);

        table = tableEnvironment.sqlQuery(query);

        tableEnvironment.dropTemporaryView(TABLE_NAME);

        DataStream<Tuple3<String, String, Long>> tupleStream = tableEnvironment.toAppendStream(
                table,
                new TypeHint<Tuple3<String, String, Long>>() {
                }.getTypeInfo()
        );

        return new Leaderboard(convertDataStreamToList(tupleStream.executeAndCollect()));
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

    private Set<Participant> convertDataStreamToList(Iterator<Tuple3<String, String, Long>> leaderboard) {

        TreeSet<Participant> participants = new TreeSet<>();

        leaderboard.forEachRemaining(user -> {
            UserInfo userInfo = new UserInfo(user.f0, user.f1);
            Participant participant = new Participant(userInfo, user.f2);
            participants.add(participant);
        });
        return participants;
    }

}
