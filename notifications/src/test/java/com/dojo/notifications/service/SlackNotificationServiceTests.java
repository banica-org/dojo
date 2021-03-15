package com.dojo.notifications.service;

import com.dojo.notifications.client.SlackWebClientProvider;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.NotifierType;
import com.dojo.notifications.model.notification.Notification;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.slackNotifier.SlackNotificationService;
import com.hubspot.algebra.Result;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import com.hubspot.slack.client.methods.params.conversations.ConversationOpenParams;
import com.hubspot.slack.client.methods.params.users.UserEmailParams;
import com.hubspot.slack.client.models.blocks.Divider;
import com.hubspot.slack.client.models.conversations.Conversation;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import com.hubspot.slack.client.models.response.conversations.ConversationsOpenResponse;
import com.hubspot.slack.client.models.response.users.UsersInfoResponse;
import com.hubspot.slack.client.models.users.SlackUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class SlackNotificationServiceTests {

    private final static String TOKEN = "token";
    private final static String USER_ID = "id";
    private final static String CONVERSATION_ID = "id";
    private final static String EMAIL = "email@email.com";

    @Mock
    private UserDetails userDetails;
    @Mock
    private Contest contest;
    @Mock
    private Notification notification;
    @Mock
    private SlackClient slackClient;
    @Mock
    private SlackWebClientProvider slackWebClientProvider;

    @Mock
    private CompletableFuture<Result<UsersInfoResponse, SlackError>> userCompletableFuture;
    @Mock
    Result<UsersInfoResponse, SlackError> userResult;
    @Mock
    private CompletableFuture<Result<ConversationsOpenResponse, SlackError>> convCompletableFuture;
    @Mock
    Result<ConversationsOpenResponse, SlackError> convResult;
    @Mock
    CompletableFuture<Result<ChatPostMessageResponse, SlackError>> postCompletableFuture;
    @Mock
    Result<ChatPostMessageResponse, SlackError> postResult;


    @InjectMocks
    private SlackNotificationService slackNotificationService;

    @Before
    public void setUp() {
        when(contest.getSlackToken()).thenReturn(TOKEN);
        when(slackWebClientProvider.getSlackClient(TOKEN)).thenReturn(slackClient);
    }


    @Test
    public void getNotificationServiceTypeMappingTest() {
        assertEquals(slackNotificationService.getNotificationServiceTypeMapping(), NotifierType.SLACK);
    }

    @Test
    public void notifyUserTest() {
        //Arrange
        when(userDetails.getEmail()).thenReturn(EMAIL);
        lookupUserSetUp();
        openConversationSetUp();
        postMessageSetUp();

        //Act
        slackNotificationService.notify(userDetails, notification, contest);

        //Assert
        verify(slackWebClientProvider, times(1)).getSlackClient(TOKEN);
        verify(notification, times(1)).convertToSlackNotification(any(), any());
        verify(slackClient, times(1)).lookupUserByEmail(any());
        verify(slackClient, times(1)).openConversation(any());
        verify(slackClient, times(1)).postMessage(any());
    }

    @Test
    public void notifyChannelTest() {
        //Arrange
        when(contest.getSlackChannel()).thenReturn(CONVERSATION_ID);
        postMessageSetUp();

        //Act
        slackNotificationService.notify(notification, contest);

        //Assert
        verify(slackWebClientProvider, times(1)).getSlackClient(TOKEN);
        verify(notification, times(1)).convertToSlackNotification(any(), any());
        verify(slackClient, times(1)).postMessage(any());
    }

    private void postMessageSetUp() {
        ChatPostMessageParams.Builder builder = ChatPostMessageParams.builder();
        when(notification.convertToSlackNotification(any(), any())).thenReturn(builder);
        ChatPostMessageParams chatPostMessageParams = builder.addBlocks(Divider.builder().build()).setChannelId(CONVERSATION_ID).build();

        when(slackClient.postMessage(chatPostMessageParams)).thenReturn(postCompletableFuture);
        when(postCompletableFuture.join()).thenReturn(postResult);
    }

    private void openConversationSetUp() {
        ConversationOpenParams conversationOpenParams = ConversationOpenParams.builder().addUsers(USER_ID).build();
        when(slackClient.openConversation(conversationOpenParams)).thenReturn(convCompletableFuture);
        when(convCompletableFuture.join()).thenReturn(convResult);

        Conversation conversation = Conversation.builder().setId(CONVERSATION_ID).build();
        ConversationsOpenResponse conversationsOpenResponse = ConversationsOpenResponse.builder().setConversation(conversation).setOk(true).build();
        when(convResult.unwrapOrElseThrow()).thenReturn(conversationsOpenResponse);
    }

    private void lookupUserSetUp() {
        UserEmailParams userEmailParams = UserEmailParams.builder().setEmail(EMAIL).build();
        when(slackClient.lookupUserByEmail(userEmailParams)).thenReturn(userCompletableFuture);
        when(userCompletableFuture.join()).thenReturn(userResult);

        SlackUser slackUser = SlackUser.builder().setId(USER_ID).build();
        UsersInfoResponse usersInfoResponse = UsersInfoResponse.builder().setUser(slackUser).setOk(true).build();

        when(userResult.unwrapOrElseThrow()).thenReturn(usersInfoResponse);
    }
}
