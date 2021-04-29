package com.dojo.notifications.service;

import com.dojo.notifications.model.request.SelectRequest;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class FlinkTableService {

    private static final String TABLE_NAME = "Leaderboard";

    public Set<String> executeSingleQuery(SelectRequest request, List<Tuple4<String,String, Integer, Long>> changedUsers) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnvironment = StreamTableEnvironment.create(executionEnvironment, settings);

        DataStream<Tuple4<String,String, Integer, Long>> tuple3DataStream = executionEnvironment.fromCollection(changedUsers);

        Table table = tableEnvironment.fromDataStream(tuple3DataStream).as("id", "name", "place", "score");
        tableEnvironment.createTemporaryView(TABLE_NAME, table);

        try {
            table = tableEnvironment.sqlQuery(request.getQuery());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }

        tableEnvironment.dropTemporaryView(TABLE_NAME);

        DataStream<Tuple4<String,String, Integer, Long>> tupleStream = tableEnvironment.toAppendStream(
                table,
                new TypeHint<Tuple4<String,String, Integer, Long>>() {
                }.getTypeInfo()
        );

        return convertDataStreamToSet(tupleStream.executeAndCollect());
    }

    private Set<String> convertDataStreamToSet(Iterator<Tuple4<String,String, Integer, Long>> leaderboard) {

        Set<String> userIds = new TreeSet<>();

        leaderboard.forEachRemaining(user -> userIds.add(user.f0));
        return userIds;
    }

}
