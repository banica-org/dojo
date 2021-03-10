package com.dojo.notifications.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Stopwatch;
import com.google.inject.assistedinject.AssistedInject;
import com.hubspot.algebra.Result;
import com.hubspot.horizon.HttpConfig;
import com.hubspot.horizon.HttpRequest;
import com.hubspot.horizon.HttpResponse;
import com.hubspot.horizon.ning.NingAsyncHttpClient;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import com.hubspot.slack.client.SlackWebClient;
import com.hubspot.slack.client.http.NioHttpClient;
import com.hubspot.slack.client.http.NioHttpClientFactory;
import com.hubspot.slack.client.interceptors.http.DefaultHttpRequestDebugger;
import com.hubspot.slack.client.interceptors.http.DefaultHttpResponseDebugger;
import com.hubspot.slack.client.interceptors.http.RequestDebugger;
import com.hubspot.slack.client.interceptors.http.ResponseDebugger;
import com.hubspot.slack.client.jackson.ObjectMapperUtils;
import com.hubspot.slack.client.methods.SlackMethod;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.methods.params.conversations.ConversationOpenParams;
import com.hubspot.slack.client.methods.params.users.UserEmailParams;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.SlackErrorResponse;
import com.hubspot.slack.client.models.response.SlackErrorType;
import com.hubspot.slack.client.models.response.SlackResponse;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import com.hubspot.slack.client.models.response.conversations.ConversationsOpenResponse;
import com.hubspot.slack.client.models.response.users.UsersInfoResponse;
import com.hubspot.slack.client.ratelimiting.ByMethodRateLimiter;
import com.hubspot.slack.client.ratelimiting.SlackRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class SlackWebClientLookupByEmail extends SlackWebClient {

    private static final Logger LOG = LoggerFactory.getLogger(SlackWebClient.class);
    private static final HttpConfig DEFAULT_CONFIG = HttpConfig.newBuilder().setObjectMapper(ObjectMapperUtils.mapper()).build();
    private static final AtomicLong REQUEST_COUNTER = new AtomicLong(0L);
    private final NioHttpClient nioHttpClient;
    private final ByMethodRateLimiter defaultRateLimiter;
    private final SlackClientRuntimeConfig config;
    private final RequestDebugger requestDebugger;
    private final ResponseDebugger responseDebugger;

    @AssistedInject
    public SlackWebClientLookupByEmail(DefaultHttpRequestDebugger defaultHttpRequestDebugger, DefaultHttpResponseDebugger defaultHttpResponseDebugger, NioHttpClient.Factory nioHttpClientFactory, ByMethodRateLimiter defaultRateLimiter, SlackClientRuntimeConfig config) {
        super(defaultHttpRequestDebugger, defaultHttpResponseDebugger, nioHttpClientFactory, defaultRateLimiter, config);
        this.nioHttpClient = config.getHttpClient().orElseGet(() -> nioHttpClientFactory.wrap(new NingAsyncHttpClient(config.getHttpConfig().orElse(DEFAULT_CONFIG))));
        this.defaultRateLimiter = defaultRateLimiter;
        this.config = config;
        this.requestDebugger = config.getRequestDebugger().orElse(defaultHttpRequestDebugger);
        this.responseDebugger = config.getResponseDebugger().orElse(defaultHttpResponseDebugger);
    }

    public SlackWebClientLookupByEmail(NioHttpClientFactory defaultFactory, SlackClientRuntimeConfig runtimeConfig) {
        this(new DefaultHttpRequestDebugger(), new DefaultHttpResponseDebugger(), defaultFactory, new ByMethodRateLimiter(), runtimeConfig);
    }

    @Override
    public CompletableFuture<Result<UsersInfoResponse, SlackError>> lookupUserByEmail(UserEmailParams params) {

        HttpRequest request = HttpRequest
                .newBuilder()
                .setMethod(HttpRequest.Method.GET)
                .setUrl(config.getSlackApiBasePath().get() + "/" + SlackMethodsLookupByEmail.users_lookupByEmail.getMethod())
                .setContentType(HttpRequest.ContentType.FORM)
                .setQueryParam("email")
                .to(params.getEmail())
                .addHeader("Authorization", "Bearer " + config.getTokenSupplier().get())
                .build();

        return executeLoggedAs(SlackMethodsLookupByEmail.users_lookupByEmail, request, UsersInfoResponse.class);
    }


    <T extends SlackResponse> CompletableFuture<Result<T, SlackError>> executeLoggedAs(SlackMethod method, HttpRequest request, Class<T> responseType) {
        long requestId = REQUEST_COUNTER.getAndIncrement();
        this.requestDebugger.debug(requestId, method, request);
        Stopwatch timer = Stopwatch.createStarted();
        double acquireSeconds = this.acquirePermit(method);
        if (acquireSeconds == -1.0D) {
            this.responseDebugger.debugProactiveRateLimit(requestId, method, request);
            return CompletableFuture.completedFuture(Result.err(SlackError.of(SlackErrorType.RATE_LIMITED.key())));
        } else {
            return this.executeLogged(requestId, method, request, timer).thenApply((response) -> {
                try {
                    return this.parseSlackResponse(response, responseType, requestId, method, request);
                } catch (JsonProcessingException var8) {
                    this.responseDebugger.debugProcessingFailure(requestId, method, request, response, var8);
                    return Result.err(SlackError.builder().setError(SlackErrorType.JSON_PARSING_FAILED.getCode()).setType(SlackErrorType.JSON_PARSING_FAILED).build());
                } catch (RuntimeException var9) {
                    this.responseDebugger.debugProcessingFailure(requestId, method, request, response, var9);
                    throw var9;
                }
            });
        }
    }

    private <T extends SlackResponse> Result<T, SlackError> parseSlackResponse(HttpResponse response, Class<T> responseType, long requestId, SlackMethod method, HttpRequest request) throws JsonProcessingException {
        JsonNode responseJson = response.getAsJsonNode();
        boolean isOk = responseJson.get("ok").asBoolean();
        if (isOk) {
            return Result.ok(ObjectMapperUtils.mapper().treeToValue(responseJson, responseType));
        } else {
            SlackErrorResponse errorResponse = ObjectMapperUtils.mapper().treeToValue(response.getAsJsonNode(), SlackErrorResponse.class);
            this.responseDebugger.debugSlackApiError(requestId, method, request, response);
            return Result.err(errorResponse.getError().orElseGet(() -> errorResponse.getErrors().get(0)));
        }
    }

    private CompletableFuture<HttpResponse> executeLogged(long requestId, SlackMethod method, HttpRequest request, Stopwatch timer) {
        CompletableFuture<HttpResponse> responseFuture = this.nioHttpClient.executeCompletableFuture(request);
        responseFuture.whenComplete((httpResponse, throwable) -> {
            if (throwable != null) {
                this.responseDebugger.debugTransportException(requestId, method, request, throwable);
            } else {
                this.responseDebugger.debug(requestId, method, timer, request, httpResponse);
            }

        });
        return responseFuture;
    }

    private double acquirePermit(SlackMethod method) {
        double acquireSeconds = this.getSlackRateLimiter().acquire(this.config.getTokenSupplier().get(), method);
        if (acquireSeconds > 5.0D) {
            LOG.warn("Throttling {}, waited {} seconds to acquire permit to run", method, acquireSeconds);
        }

        return acquireSeconds;
    }

    private SlackRateLimiter getSlackRateLimiter() {
        return this.config.getSlackRateLimiter().orElse(this.defaultRateLimiter);
    }

}
