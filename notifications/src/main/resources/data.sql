INSERT INTO SELECT_REQUESTS(QUERY,QUERY_DESCRIPTION,MESSAGE)
VALUES ('SELECT * FROM leaderboard WHERE score>=5', 'Notify when any person gets at least 5 points.','You went up at least 5 points! CONGRATS!');
INSERT INTO SELECT_REQUESTS(QUERY,QUERY_DESCRIPTION,MESSAGE)
VALUES ('SELECT * FROM leaderboard WHERE place=1', 'Notify when any person climbs at least 1 position.','You went up at least 1 position! CONGRATS!');
INSERT INTO SELECT_REQUESTS(QUERY,QUERY_DESCRIPTION,MESSAGE)
VALUES ('SELECT * FROM leaderboard WHERE place=-1', 'Notify when any person goes down at least 1 position.','You went down at least 1 position! Get back in there!');
