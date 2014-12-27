--create schema users authorization chemcentral;
--grant all on schema users to chemcentral;

drop table users.hit_list_data;
drop table users.hit_lists;

create table users.hit_lists (
  id SERIAL PRIMARY KEY,
  resource VARCHAR(200) NOT NULL, -- URL of the datasource
  list_owner VARCHAR(32) NOT NULL, -- the user who owns the list: needs to handle sharing etc
  list_name TEXT NOT NULL, -- user specified name
  list_status VARCHAR(16), -- Pending, OK
  list_size INTEGER, -- the number of items in the list
  created_timestamp TIMESTAMP NOT NULL DEFAULT NOW()
  );

create table users.hit_list_data (
  hit_list_id INTEGER NOT NULL,
  id_item INTEGER NOT NULL,
  PRIMARY KEY (hit_list_id, id_item),
  CONSTRAINT fk_hit_list_data2hit_lists FOREIGN KEY (hit_list_id) REFERENCES users.hit_lists (id) ON DELETE CASCADE
  );


grant all on all tables in schema users to chemcentral;
grant all on all sequences in schema users to chemcentral;