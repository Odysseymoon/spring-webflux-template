INSERT INTO `user`(`user_id`, `password`) VALUES ('testUser', '{bcrypt}$2a$10$NQi6Y7tS2VdOyBBvW7Kc8OGl21PHZiqvgmQLNMJL4jH.Ae5QnpM7O');

INSERT INTO `scope`(`authority`, `description`) VALUES ('read', 'read authority');
INSERT INTO `scope`(`authority`, `description`) VALUES ('write', 'write authority');
INSERT INTO `scope`(`authority`, `description`) VALUES ('refresh', 'refresh authority');

INSERT INTO `user_scope`(`user_id`, `authority`) VALUES ('testUser', 'read');
INSERT INTO `user_scope`(`user_id`, `authority`) VALUES ('testUser', 'write');
INSERT INTO `user_scope`(`user_id`, `authority`) VALUES ('testUser', 'refresh');