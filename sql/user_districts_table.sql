SELECT * FROM user_districts_table;
SELECT user_id, districts_id FROM user_districts_table;

INSERT INTO user_districts_table VALUES (1,2);
INSERT INTO user_districts_table VALUES (2,2);
INSERT INTO user_districts_table VALUES (1,3);
INSERT INTO user_districts_table VALUES (5700852179,1);

INSERT INTO user_districts_table VALUES(3, 1), (3, 2);

DELETE FROM user_districts_table WHERE (user_id = 3);
DELETE FROM user_districts_table WHERE (user_id = 5700852179);
DELETE FROM user_districts_table WHERE (user_id = 5700852179 and districts_id=8);


SELECT user_id, name FROM user_districts_table JOIN districts_table ON user_districts_table.districts_id=districts_table.id where user_id=1;