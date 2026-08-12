create table fake_users (
	first_name varchar(10),
	last_name varchar(10),
	email varchar(20)
);

insert into fake_users (first_name, last_name, email)
values
  ('olivia','dunham','olivia@example.org'),
  ('peter','bishop','peter@example.org'),
  ('walter','bishop','walter@example.org'),
  ('astrid','farnsworth','astrid@example.org');
