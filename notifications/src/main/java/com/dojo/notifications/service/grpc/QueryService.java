package com.dojo.notifications.service.grpc;

import com.dojo.notifications.Query;
import com.dojo.notifications.QueryRequest;
import com.dojo.notifications.QueryResponse;
import com.dojo.notifications.QueryServiceGrpc;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.service.SelectRequestService;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QueryService extends QueryServiceGrpc.QueryServiceImplBase{

    private final SelectRequestService selectRequestService;

    @Autowired
    public QueryService(SelectRequestService selectRequestService) {
        this.selectRequestService = selectRequestService;
    }

    @Override
    public void getQueryRequestsForContest(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
        List<SelectRequest> requests = selectRequestService.getAllRequests();
        List<Query> queries = new ArrayList<>();
        requests.forEach(request1 -> queries.add(Query.newBuilder()
                .setId(request1.getId())
                .setDescription(request1.getQueryDescription())
                .build()));
        QueryResponse response = QueryResponse.newBuilder().addAllQuery(queries).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
