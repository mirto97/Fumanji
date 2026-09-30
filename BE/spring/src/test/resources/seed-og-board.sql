-- BOARD
INSERT INTO board_entity (id, finish_position, direction_choice_enabled, mandatory_checkpoint_enabled, backward_allowed_after_checkpoint)
VALUES (1, 110, 1, 1, 0);

-- SQUARE_ENTITY: righe posizionate (board_id = 1) + righe "effetto" (board_id = NULL, position = 0)
INSERT INTO square_entity (id, position, board_id, square_type) VALUES
(1, 1, 1, 'ObligationSquareEntity'),
(2, 2, 1, 'ObligationSquareEntity'),
(3, 3, 1, 'DrinkSquareEntity'),
(4, 4, 1, 'StartOverSquareEntity'),
(6, 6, 1, 'BivioSquareEntity'),
(10, 10, 1, 'ObligationSquareEntity'),
(11, 11, 1, 'ObligationSquareEntity'),
(12, 12, 1, 'BivioSquareEntity'),
(16, 16, 1, 'StartOverSquareEntity'),
(17, 17, 1, 'BivioSquareEntity'),
(22, 22, 1, 'DrinkSquareEntity'),
(23, 23, 1, 'PortalSquareEntity'),
(25, 25, 1, 'ObligationSquareEntity'),
(26, 26, 1, 'ObligationSquareEntity'),
(27, 27, 1, 'PortalSquareEntity'),
(28, 28, 1, 'SnackSquareEntity'),
(29, 29, 1, 'ObligationSquareEntity'),
(30, 30, 1, 'ObligationSquareEntity'),
(31, 31, 1, 'DrinkSquareEntity'),
(32, 32, 1, 'PortalSquareEntity'),
(33, 33, 1, 'BivioSquareEntity'),
(40, 40, 1, 'ObligationSquareEntity'),
(41, 41, 1, 'ObligationSquareEntity'),
(42, 42, 1, 'BivioSquareEntity'),
(46, 46, 1, 'StartOverSquareEntity'),
(47, 47, 1, 'BivioSquareEntity'),
(52, 52, 1, 'DrinkSquareEntity'),
(53, 53, 1, 'PortalSquareEntity'),
(55, 55, 1, 'ObligationSquareEntity'),
(56, 56, 1, 'SnackSquareEntity'),
(57, 57, 1, 'ObligationSquareEntity'),
(58, 58, 1, 'PortalSquareEntity'),
(59, 59, 1, 'ObligationSquareEntity'),
(60, 60, 1, 'DrinkSquareEntity'),
(61, 61, 1, 'ObligationSquareEntity'),
(62, 62, 1, 'PortalSquareEntity'),
(63, 63, 1, 'BivioSquareEntity'),
(70, 70, 1, 'BivioSquareEntity'),
(71, 71, 1, 'SnackSquareEntity'),
(72, 72, 1, 'StartOverSquareEntity'),
(73, 73, 1, 'BivioSquareEntity'),
(80, 80, 1, 'BivioSquareEntity'),
(81, 81, 1, 'CheckpointSquareEntity'),
(82, 82, 1, 'ObligationSquareEntity'),
(83, 83, 1, 'DrinkSquareEntity'),
(84, 84, 1, 'SnackSquareEntity'),
(86, 86, 1, 'ObligationSquareEntity'),
(87, 87, 1, 'DrinkSquareEntity'),
(88, 88, 1, 'StartOverSquareEntity'),
(89, 89, 1, 'ObligationSquareEntity'),
(90, 90, 1, 'ObligationSquareEntity'),
(91, 91, 1, 'ObligationSquareEntity'),
(92, 92, 1, 'ObligationSquareEntity'),
(93, 93, 1, 'StartOverSquareEntity'),
(94, 94, 1, 'DrinkSquareEntity'),
(95, 95, 1, 'SnackSquareEntity'),
(96, 96, 1, 'StartOverSquareEntity'),
(98, 98, 1, 'ObligationSquareEntity'),
(99, 99, 1, 'ObligationSquareEntity'),
(100, 100, 1, 'ObligationSquareEntity'),
(102, 102, 1, 'SnackSquareEntity'),
(103, 103, 1, 'StartOverSquareEntity'),
(104, 104, 1, 'DrinkSquareEntity'),
(105, 105, 1, 'ObligationSquareEntity'),
(106, 106, 1, 'StartOverSquareEntity'),
(107, 107, 1, 'ObligationSquareEntity'),
(108, 108, 1, 'ObligationSquareEntity'),
(109, 109, 1, 'StartOverSquareEntity'),
-- righe "effetto" dei bivi: nessuna board (non sono posizionate), position=0 = placeholder inerte
(1006, 0, NULL, 'SnackSquareEntity'),
(1012, 0, NULL, 'ObligationSquareEntity'),
(1042, 0, NULL, 'ObligationSquareEntity'),
(1070, 0, NULL, 'StartOverSquareEntity'),
(1033, 0, NULL, 'ObligationSquareEntity'),
(1047, 0, NULL, 'DrinkSquareEntity'),
(1063, 0, NULL, 'ObligationSquareEntity'),
(1073, 0, NULL, 'ObligationSquareEntity');

-- OBLIGATION: messaggi (positioned + effetto)
INSERT INTO obligation_square_entity (id, message) VALUES
(1, '{player} deve dare un obbligo a qualcuno'),
(2, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(10, '{player} deve fare 2 tiri'),
(11, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(25, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(26, '{player} deve dare un obbligo a qualcuno'),
(29, '{player} deve fare 2 tiri'),
(30, '{player} deve dare un obbligo a qualcuno'),
(40, '{player} deve fare 2 tiri'),
(41, '{player} deve dare un obbligo a qualcuno'),
(55, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(57, '{player} deve fare 2 tiri'),
(59, '{player} deve dare un obbligo a qualcuno'),
(61, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(82, '{player} deve fare 3 tiri di creazione'),
(86, '{player} deve dare un obbligo a qualcuno'),
(89, '{player} deve dare un obbligo a qualcuno'),
(90, '{player} deve fare 3 tiri di creazione'),
(91, '{player} deve fare un hit di bong o pipa'),
(92, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(98, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(99, '{player} deve fare 2 tiri'),
(100, '{player} deve dare un obbligo a qualcuno'),
(105, 'tutti devono fare un hit di bong o pipa'),
(107, '{player} deve fare un hit di bong o pipa'),
(108, '{player} deve fare 3 tiri di creazione'),
(1012, '{player} deve dare un obbligo a qualcuno'),
(1042, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(1033, '{player} deve fare 3 tiri di creazione'),
(1063, '{player} deve fare 3 tiri di creazione'),
(1073, '{player} deve fare 3 tiri di creazione');

-- DRINK (amount 1 di default, 2 dove segnato "x2")
INSERT INTO drink_square_entity (id, amount) VALUES
(3, 1), (22, 1), (31, 1), (52, 1), (60, 1),
(83, 2), (87, 1), (94, 2), (104, 2),
(1047, 1);

-- SNACK
INSERT INTO snack_square_entity (id, amount) VALUES
(28, 1), (56, 1), (71, 1), (84, 1), (95, 1), (102, 1),
(1006, 1);

-- STARTOVER
INSERT INTO start_over_square_entity (id) VALUES
(4), (16), (46), (72), (88), (93), (96), (103), (106), (109),
(1070);

-- CHECKPOINT
INSERT INTO checkpoint_square_entity (id) VALUES (81);

-- PORTAL
INSERT INTO portal_square_entity (id) VALUES (23), (27), (32), (53), (58), (62);

INSERT INTO portal_square_link (square_id, linked_position) VALUES
(53, 23), (53, 32), (53, 62), (53, 27), (53, 58),
(23, 53), (23, 32), (23, 62), (23, 27), (23, 58),
(32, 53), (32, 23), (32, 62), (32, 27), (32, 58),
(62, 53), (62, 23), (62, 32), (62, 27), (62, 58),
(27, 53), (27, 23), (27, 32), (27, 62), (27, 58),
(58, 53), (58, 23), (58, 32), (58, 62), (58, 27);

-- BIVIO (id, effect_square_id nullable)
INSERT INTO bivio_square_entity (id, effect_square_id) VALUES
(6, 1006),
(12, 1012),
(17, NULL),
(33, 1033),
(42, 1042),
(47, 1047),
(63, 1063),
(70, 1070),
(73, 1073),
(80, NULL);

-- NEXT / PREVIOUS POSITIONS DEI BIVI
INSERT INTO bivio_square_next (square_id, next_position) VALUES
(6, 10), (6, 40),
(12, 16), (12, 22),
(17, 70),
(33, 80),
(42, 46), (42, 52),
(47, 70),
(63, 80),
(70, 71),
(73, 80),
(80, 81);

INSERT INTO bivio_square_previous (square_id, previous_position) VALUES
(6, 5),
(12, 11),
(17, 16),
(33, 32),
(42, 41),
(47, 46),
(63, 62),
(70, 17), (70, 47),
(73, 72),
(80, 33), (80, 63), (80, 73);