SELECT * FROM users_table;

SELECT id FROM users_table;
SELECT id, is_available FROM users_table;
SELECT id, is_available, last_action_time, name FROM users_table;

SELECT is_available FROM users_table WHERE id=5700852179;
SELECT last_action_time FROM users_table WHERE id = 5700852179;

INSERT INTO users_table VALUES (5700852179,1,"2023-08-14 14:14:14","Ашот");

UPDATE users_table SET last_action_time='2023-10-10 23:02:54' WHERE id = 5;
SELECT * FROM mydatabase.users_table;

INSERT INTO users_table (id, is_available, last_action_time, name) VALUES (674781717,true,'2023-10-08 23:02:54','PavelNazaro') ON DUPLICATE KEY UPDATE is_available=true, last_action_time='2023-10-08 23:02:54', name='PavelNazaro';
INSERT INTO users_table (id, is_available, last_action_time, name) VALUES (674781717,true,2023-10-08,'PavelNazaro') ON DUPLICATE KEY UPDATE is_available=true, last_action_time=2023-10-08, name='PavelNazaro';

INSERT INTO users_table (id, is_available, last_action_time, name) VALUES (4,0,"2023-08-14 14:14:14","Ашот") ON DUPLICATE KEY UPDATE is_available=1, last_action_time="2023-08-14 14:14:14", name="PavelNazaro";
INSERT INTO users_table (id, is_available, last_action_time, name) VALUES (4,0,"2023-08-14 14:14:14","Ашот") ON DUPLICATE KEY UPDATE is_available=0;
SELECT * FROM mydatabase.users_table;

SET foreign_key_checks = 0;