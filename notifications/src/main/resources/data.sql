INSERT INTO SELECT_REQUESTS(QUERY,RECEIVER,EVENT_TYPE,NOTIFICATION_LEVEL,QUERY_DESCRIPTION,MESSAGE)
VALUES ('SELECT * FROM Leaderboard WHERE score>=500', 'Participant', 'SCORE_CHANGES','ON_CHANGED_SCORE', 'Notify when any person gets at least 500 points.','You went up at least 500 points! CONGRATS!');
INSERT INTO SELECT_REQUESTS(QUERY,RECEIVER,EVENT_TYPE,NOTIFICATION_LEVEL,QUERY_DESCRIPTION,MESSAGE)
VALUES ('SELECT * FROM Leaderboard WHERE place=1', 'Participant', 'POSITION_CHANGES','ON_CHANGED_POSITION', 'Notify when any person climbs at least 1 position.','You went up at least 1 position! CONGRATS!');
INSERT INTO SELECT_REQUESTS(QUERY,RECEIVER,EVENT_TYPE,NOTIFICATION_LEVEL,QUERY_DESCRIPTION,MESSAGE)
VALUES ('SELECT * FROM Leaderboard WHERE place=-1', 'Participant', 'POSITION_CHANGES','ON_CHANGED_POSITION', 'Notify when any person goes down at least 1 position.','You went down at least 1 position! Get back in there!');
