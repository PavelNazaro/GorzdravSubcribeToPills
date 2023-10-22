SELECT * FROM user_subscriptions_table;
SELECT user_id, subscriptions_name, benefit_id FROM user_subscriptions_table;

INSERT INTO user_subscriptions_table VALUES (1,"Юпер",1);
INSERT INTO user_subscriptions_table VALUES (674781717,'Юперио, 100 мг (51,4 мг + 48,6 мг) № 56, таблетки покрытые плёночной оболочкой',1);

DELETE FROM user_subscriptions_table WHERE (user_id = '674781717');
DELETE FROM user_subscriptions_table WHERE (user_id = '674781717' and subscriptions_name = 'Юперио, 100 мг (51,4 мг + 48,6 мг) № 56, таблетки покрытые плёночной оболочкой');

SELECT subscriptions_name, name FROM user_subscriptions_table JOIN benefits_table ON user_subscriptions_table.benefit_id=benefits_table.id where user_id=674781717;