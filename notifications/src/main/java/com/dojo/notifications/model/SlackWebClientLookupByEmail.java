package com.dojo.notifications.model;

import com.hubspot.algebra.Result;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.SlackClientFactory;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import com.hubspot.slack.client.SlackWebClient;
import com.hubspot.slack.client.http.NioHttpClientFactory;
import com.hubspot.slack.client.interceptors.http.DefaultHttpRequestDebugger;
import com.hubspot.slack.client.interceptors.http.DefaultHttpResponseDebugger;
import com.hubspot.slack.client.methods.SlackMethod;
import com.hubspot.slack.client.methods.SlackMethods;
import com.hubspot.slack.client.methods.params.users.UserEmailParams;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.users.UsersInfoResponse;
import com.hubspot.slack.client.ratelimiting.ByMethodRateLimiter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class SlackWebClientLookupByEmail extends SlackWebClient {

    private final SlackClientRuntimeConfig config;

    public SlackWebClientLookupByEmail(SlackClientRuntimeConfig runtimeConfig) {
        super(new DefaultHttpRequestDebugger(), new DefaultHttpResponseDebugger(), NioHttpClientFactory.defaultFactory(), new ByMethodRateLimiter(), runtimeConfig);
        this.config = runtimeConfig;
    }

    @SneakyThrows
    @Override
    public CompletableFuture<Result<UsersInfoResponse, SlackError>> lookupUserByEmail(UserEmailParams params) {
        HttpRequest request = this.getHttpRequestForLookupUser(params);
        SlackClient slackWebClient = SlackClientFactory.defaultFactory().build(config);
        Method execute = SlackWebClient.class.getDeclaredMethod("executeLoggedAs", SlackMethod.class, HttpRequest.class, Class.class);
        execute.setAccessible(true);
        return (CompletableFuture<Result<UsersInfoResponse, SlackError>>) execute.invoke(slackWebClient, SlackMethods.users_lookupByEmail, request, UsersInfoResponse.class);
    }

    private HttpRequest getHttpRequestForLookupUser(UserEmailParams params) {
        return HttpRequest
                .newBuilder()
                .setMethod(HttpRequest.Method.GET)
                .setUrl(config.getSlackApiBasePath().get() + "/" + SlackMethods.users_lookupByEmail.getMethod())
                .setContentType(HttpRequest.ContentType.FORM)
                .setQueryParam("email")
                .to(params.getEmail())
                .addHeader("Authorization", "Bearer " + config.getTokenSupplier().get())
                .build();
    }
}
