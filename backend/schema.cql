CREATE keyspace reactive_kanban with REPLICATION = {'class':'SimpleStrategy', 'replication_factor': 3};

/* Board */
CREATE TABLE reactive_kanban.board (
	id timeuuid,
	description text,
	PRIMARY KEY (id)
);

/* CardList */
CREATE TABLE reactive_kanban.card_list (
	id timeuuid,
	board_id uuid,
	title text,
	list_order int,
	PRIMARY KEY (board_id, id)
);

/* Card */
CREATE TABLE reactive_kanban.card (
    id timeuuid,
    card_list_id uuid,
    title text,
    description text,
    card_order int,
    PRIMARY KEY (card_list_id,id)
);