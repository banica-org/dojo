package com.dojo.notifications.model.client;

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
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.methods.params.conversations.ConversationOpenParams;
import com.hubspot.slack.client.methods.params.users.UserEmailParams;
import com.hubspot.slack.client.models.conversations.Conversation;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import com.hubspot.slack.client.models.response.conversations.ConversationsOpenResponse;
import com.hubspot.slack.client.models.response.users.UsersInfoResponse;
import com.hubspot.slack.client.models.users.SlackUser;
import com.hubspot.slack.client.ratelimiting.ByMethodRateLimiter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class CustomSlackWebClient extends SlackWebClient implements CustomSlackClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSlackWebClient.class);

    private final SlackClientRuntimeConfig config;

    public CustomSlackWebClient(SlackClientRuntimeConfig runtimeConfig) {
        super(new DefaultHttpRequestDebugger(), new DefaultHttpResponseDebugger(), NioHttpClientFactory.defaultFactory(), new ByMethodRateLimiter(), runtimeConfig);
        this.config = runtimeConfig;
    }

    @Override
    public CompletableFuture<Result<ChatPostMessageResponse, SlackError>> postMessage(ChatPostMessageParams chatPostMessageParams) {
        CompletableFuture<Result<ChatPostMessageResponse, SlackError>> post = super.postMessage(chatPostMessageParams);
        Result<ChatPostMessageResponse, SlackError> postResult = post.join();
        try {
            postResult.unwrapOrElseThrow(); // release failure here as a RTE
        } catch (IllegalStateException e) {
            LOGGER.warn("Error occurred while trying to send Slack notification to channel {}.", chatPostMessageParams.getChannelId());
            return post;
        }
        LOGGER.info("Slack notification send to channel {}.", chatPostMessageParams.getChannelId());
        return post;
    }

    public String getConversationId(String email) {
        try {
            ConversationOpenParams conversationOpenParams = ConversationOpenParams.builder()
                    .addUsers(getSlackUserId(email))
                    .build();
            ConversationsOpenResponse response = super.openConversation(conversationOpenParams)
                    .join().unwrapOrElseThrow();
            Conversation conversation = response.getConversation();
            return conversation.getId();
        } catch (IllegalStateException e) {
            LOGGER.warn("Could not find conversation for user with email {}.", email);
            return "";
        }
    }

    private String getSlackUserId(String email) {
        try {
            return getUser(email).getId();
        } catch (IllegalStateException e) {
            LOGGER.warn("Could not find user with email {}.", email);
            return "";
        }
    }

    private SlackUser getUser(String email) {
        UserEmailParams userEmailParams = UserEmailParams.builder()
                .setEmail(email)
                .build();
        UsersInfoResponse response = this
                .lookupUserByEmail(userEmailParams)
                .join().unwrapOrElseThrow();
        return response.getUser();
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
