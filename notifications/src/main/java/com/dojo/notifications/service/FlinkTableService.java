package com.dojo.notifications.service;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserInfo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FlinkTableService {

    public static final int USER_ID_POSITION = 0;
    private static final int USER_NAME_POSITION = 1;
    public static final int USER_SCORE_POSITION = 2;


    public Leaderboard executeSingleQuery(Set<Participant> participants, String tableName, String query) {

        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(executionEnvironment, settings);

        DataStream<Tuple3<String, String, Long>> tuple3DataStream = convertToTuple3DataStream(executionEnvironment, participants);

        tableEnvironment.createTemporaryView(tableName, tuple3DataStream);
        String queryFormatted = String.format(query, tableName);
        TableResult tableLeaderboard = tableEnvironment.sqlQuery(queryFormatted).execute();
        tableEnvironment.dropTemporaryView(tableName);

        return new Leaderboard(convertTableResultToList(tableLeaderboard));
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

    private TreeSet<Participant> convertTableResultToList(TableResult leaderboard) {
        Iterator<Row> rowsNew = leaderboard.collect();

        TreeSet<Participant> participants = new TreeSet<>();

        rowsNew.forEachRemaining(row -> {
            UserInfo userInfo = new UserInfo((String) row.getField(USER_ID_POSITION), (String) row.getField(USER_NAME_POSITION));
            Participant participant = new Participant(userInfo, (long) row.getField(USER_SCORE_POSITION));
            participants.add(participant);
        });
        return participants;
    }

}
