-- phpMyAdmin SQL Dump
-- version 5.1.1deb5ubuntu1
-- https://www.phpmyadmin.net/
--
-- Počítač: localhost:3306
-- Vytvořeno: Sob 11. úno 2023, 13:08
-- Verze serveru: 10.6.11-MariaDB-0ubuntu0.22.04.1
-- Verze PHP: 8.1.2-1ubuntu2.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

SET GLOBAL connect_timeout=28800;
SET GLOBAL interactive_timeout=28800;
SET GLOBAL wait_timeout=28800;
SET GLOBAL max_allowed_packet=300000000;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Databáze: `protein_chain_db`
--
CREATE DATABASE IF NOT EXISTS `protein_chain_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `protein_chain_db`;

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `numberOfChainsWithoutMetadata`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `numberOfChainsWithoutMetadata` (
    `number_of_chains_without_metadata` bigint(22)
);

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `pivot64ForSketches`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `pivot64ForSketches` (
                                      `chainIntId` int(11) unsigned
    ,`pivotSetId` int(11)
);

-- --------------------------------------------------------

--
-- Struktura tabulky `pivot512`
--

CREATE TABLE `pivot512` (
                            `chainIntId` int(11) UNSIGNED NOT NULL COMMENT 'foreign key',
                            `pivotSetId` int(11) NOT NULL COMMENT 'foreign key'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `pivot512ForSketches`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `pivot512ForSketches` (
                                       `chainIntId` int(11) unsigned
    ,`pivotSetId` int(11)
);

-- --------------------------------------------------------

--
-- Struktura tabulky `pivotPairsFor64pSketches`
--

CREATE TABLE `pivotPairsFor64pSketches` (
                                            `pivotSetId` int(11) NOT NULL COMMENT 'foreign key',
                                            `sketchBitOrder` smallint(6) NOT NULL COMMENT '0-191 (or 193, first)',
                                            `pivot1` int(11) UNSIGNED NOT NULL COMMENT 'foreign key (int)',
                                            `pivot2` int(11) UNSIGNED NOT NULL COMMENT 'foreign key (int)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabulky `pivotPairsFor512pSketches`
--

CREATE TABLE `pivotPairsFor512pSketches` (
                                             `pivotSetId` int(11) NOT NULL COMMENT 'foreign key',
                                             `sketchBitOrder` smallint(6) NOT NULL COMMENT '0-1023',
                                             `pivot1` int(11) UNSIGNED NOT NULL COMMENT 'foreign key (int)',
                                             `pivot2` int(11) UNSIGNED NOT NULL COMMENT 'foreign key (int)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabulky `pivotSet`
--

CREATE TABLE `pivotSet` (
                            `id` int(11) NOT NULL COMMENT 'artificial id',
                            `currentlyUsed` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'denotes the only pivot set used in the app right now',
                            `added` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'timestamp of the pivot set selection'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabulky `protein`
--

CREATE TABLE `protein` (
                           `pdbId` varchar(4) NOT NULL COMMENT 'Identifier of the protein (e.g., 12AS)',
                           `name` longtext NOT NULL COMMENT 'Structure title from mmCIF file.'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Struktura tabulky `proteinChain`
--

CREATE TABLE `proteinChain` (
                                `gesamtId` varchar(15) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'id from PDBe database, e.g. 7BZ5:A',
                                `intId` int(11) UNSIGNED NOT NULL COMMENT 'int id for PPP codes',
                                `chainLength` int(11) NOT NULL COMMENT 'protein chain size',
                                `indexedAsDataObject` tinyint(4) NOT NULL DEFAULT 1 COMMENT 'If 1, then object is indexed by PPP codes and others. If 0, it is skipped from the indexing, but can be used as a pivot.',
                                `added` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggery `proteinChain`
--
DELIMITER $$
CREATE TRIGGER `proteinChainDeleteForbidden` BEFORE DELETE ON `proteinChain` FOR EACH ROW BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Do NOT delete chains from this table - they could serve as pivots. Change the corresponding attribut to discard them from the search, and delete them from table pivotChainMetadata for the currently used pivotSet, instead.';
END
    $$
DELIMITER ;

-- --------------------------------------------------------

--
-- Struktura tabulky `proteinChainMetadata`
--

CREATE TABLE `proteinChainMetadata` (
                                        `pivotSetId` int(11) NOT NULL COMMENT 'foreign key',
                                        `chainIntId` int(11) UNSIGNED NOT NULL COMMENT 'foreign key',
                                        `pivotDistances` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'map of distance to pivot in a format for JSON',
                                        `sketch512p` varchar(400) NOT NULL COMMENT 'long sketch',
                                        `sketch64p` varchar(96) NOT NULL COMMENT 'short sketch',
                                        `lastUpdate` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Last update'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `proteinChainMetadata_WithTrivialLongSketchForCurrentPivotSet`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `proteinChainMetadata_WithTrivialLongSketchForCurrentPivotSet` (
                                                                                `pivotSetId` int(11)
    ,`chainIntId` int(11) unsigned
    ,`pivotDistances` longtext
    ,`sketch512p` varchar(400)
    ,`sketch64p` varchar(96)
    ,`lastUpdate` timestamp
);

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `proteinId`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `proteinId` (
    `id` varchar(4)
);

-- --------------------------------------------------------

--
-- Struktura tabulky `queriesNearestNeighboursStats`
--

CREATE TABLE `queriesNearestNeighboursStats` (
                                                 `evaluationTime` int(11) NOT NULL DEFAULT 0,
                                                 `added` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Time when the row added into the table',
                                                 `queryGesamtId` varchar(15) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'gesamt key of the query, NOT a foreign key',
                                                 `nnGesamtId` varchar(15) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'gesamt key of nearest neighbour, foreign key',
                                                 `qscore` float NOT NULL COMMENT 'q-score (not distance)',
                                                 `rmsd` float NOT NULL COMMENT 'RMSD between aligned structures',
                                                 `alignedResidues` int(11) NOT NULL COMMENT 'Number of aligned residues',
                                                 `seqIdentity` float NOT NULL COMMENT 'Sequence identity',
                                                 `rotationStats` varchar(192) DEFAULT NULL COMMENT 'Stats to draw the overleap'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `randomQuery`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `randomQuery` (
                               `gesamtId` varchar(15)
    ,`chainLength` int(11)
);

-- --------------------------------------------------------

--
-- Struktura tabulky `savedQueries`
--

CREATE TABLE `savedQueries` (
                                `job_id` varchar(8) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'Unique ID of the job.',
                                `disable_search_stats` tinyint(1) DEFAULT 0,
                                `disable_visualizations` tinyint(1) DEFAULT 0,
                                `added` datetime NOT NULL DEFAULT current_timestamp() COMMENT 'Time when the query results were saved.',
                                `name` varchar(256) CHARACTER SET latin1 COLLATE latin1_general_cs NOT NULL COMMENT 'Name of the query structure',
                                `chain` varchar(5) NOT NULL COMMENT 'ID of the chain from input',
                                `radius` double NOT NULL COMMENT 'Radius of the search',
                                `k` int(11) NOT NULL COMMENT 'Maximum number of results',
                                `statistics` longtext CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'JSON with statistics to display'
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Zástupná struktura pro pohled `TMPCachedDistsToPivots`
-- (Vlastní pohled viz níže)
--
CREATE TABLE `TMPCachedDistsToPivots` (
                                          `evaluationTime` int(11)
    ,`added` timestamp
    ,`queryGesamtId` varchar(15)
    ,`nnGesamtId` varchar(15)
    ,`qscore` float
    ,`rmsd` float
    ,`alignedResidues` int(11)
    ,`seqIdentity` float
);

-- --------------------------------------------------------

--
-- Struktura pro pohled `numberOfChainsWithoutMetadata`
--
DROP TABLE IF EXISTS `numberOfChainsWithoutMetadata`;

CREATE ALGORITHM=UNDEFINED DEFINER=`chain`@`%` SQL SECURITY DEFINER VIEW `numberOfChainsWithoutMetadata`  AS SELECT `y`.`p2`- `x`.`p1` AS `number_of_chains_without_metadata` FROM ((select count(0) AS `p1` from (`proteinChainMetadata` `m` join `pivotSet` `p` on(`m`.`pivotSetId` = `p`.`id`)) where `p`.`currentlyUsed` = 1) `x` join (select count(0) AS `p2` from `proteinChain`) `y`) ;

-- --------------------------------------------------------

--
-- Struktura pro pohled `pivot64ForSketches`
--
DROP TABLE IF EXISTS `pivot64ForSketches`;

CREATE ALGORITHM=UNDEFINED DEFINER=`chain`@`%` SQL SECURITY DEFINER VIEW `pivot64ForSketches`  AS SELECT `x`.`pivot1` AS `chainIntId`, `x`.`pivotSetId` AS `pivotSetId` FROM (select `pivotPairsFor64pSketches`.`pivotSetId` AS `pivotSetId`,`pivotPairsFor64pSketches`.`sketchBitOrder` AS `sketchBitOrder`,`pivotPairsFor64pSketches`.`pivot1` AS `pivot1`,`pivotPairsFor64pSketches`.`pivot2` AS `pivot2`,`pivotSet`.`id` AS `id`,`pivotSet`.`currentlyUsed` AS `currentlyUsed`,`pivotSet`.`added` AS `added` from (`pivotPairsFor64pSketches` join `pivotSet`) where `pivotSet`.`currentlyUsed` = 1) AS `x` ;

-- --------------------------------------------------------

--
-- Struktura pro pohled `pivot512ForSketches`
--
DROP TABLE IF EXISTS `pivot512ForSketches`;

CREATE ALGORITHM=UNDEFINED DEFINER=`chain`@`%` SQL SECURITY DEFINER VIEW `pivot512ForSketches`  AS SELECT `x`.`pivot1` AS `chainIntId`, `x`.`pivotSetId` AS `pivotSetId` FROM (select `pivotPairsFor512pSketches`.`pivotSetId` AS `pivotSetId`,`pivotPairsFor512pSketches`.`sketchBitOrder` AS `sketchBitOrder`,`pivotPairsFor512pSketches`.`pivot1` AS `pivot1`,`pivotPairsFor512pSketches`.`pivot2` AS `pivot2`,`pivotSet`.`id` AS `id`,`pivotSet`.`currentlyUsed` AS `currentlyUsed`,`pivotSet`.`added` AS `added` from (`pivotPairsFor512pSketches` join `pivotSet`) where `pivotSet`.`currentlyUsed` = 1) AS `x` ;

-- --------------------------------------------------------

--
-- Struktura pro pohled `proteinChainMetadata_WithTrivialLongSketchForCurrentPivotSet`
--
DROP TABLE IF EXISTS `proteinChainMetadata_WithTrivialLongSketchForCurrentPivotSet`;

CREATE ALGORITHM=UNDEFINED DEFINER=`chain`@`%` SQL SECURITY DEFINER VIEW `proteinChainMetadata_WithTrivialLongSketchForCurrentPivotSet`  AS SELECT `m`.`pivotSetId` AS `pivotSetId`, `m`.`chainIntId` AS `chainIntId`, `m`.`pivotDistances` AS `pivotDistances`, `m`.`sketch512p` AS `sketch512p`, `m`.`sketch64p` AS `sketch64p`, `m`.`lastUpdate` AS `lastUpdate` FROM (`proteinChainMetadata` `m` join `pivotSet` `p` on(`m`.`pivotSetId` = `p`.`id`)) WHERE `p`.`currentlyUsed` = 1 AND `m`.`sketch512p` = '{"sk1024_long":[0]}' ;

-- --------------------------------------------------------

--
-- Struktura pro pohled `proteinId`
--
DROP TABLE IF EXISTS `proteinId`;

CREATE ALGORITHM=UNDEFINED DEFINER=`chain`@`%` SQL SECURITY DEFINER VIEW `proteinId`  AS SELECT DISTINCT substr(`p`.`gesamtId`,1,4) AS `id` FROM `proteinChain` AS `p` WHERE `p`.`indexedAsDataObject` = 1 ;

-- --------------------------------------------------------

--
-- Struktura pro pohled `randomQuery`
--
DROP TABLE IF EXISTS `randomQuery`;

CREATE ALGORITHM=UNDEFINED DEFINER=`admin`@`localhost` SQL SECURITY DEFINER VIEW `randomQuery`  AS SELECT `x`.`gesamtId` AS `gesamtId`, `x`.`chainLength` AS `chainLength` FROM (select `proteinChain`.`gesamtId` AS `gesamtId`,`proteinChain`.`chainLength` AS `chainLength` from `proteinChain` order by rand() limit 10) AS `x` ORDER BY `x`.`chainLength` ASC ;

-- --------------------------------------------------------

--
-- Struktura pro pohled `TMPCachedDistsToPivots`
--
DROP TABLE IF EXISTS `TMPCachedDistsToPivots`;

CREATE ALGORITHM=UNDEFINED DEFINER=`chain`@`%` SQL SECURITY DEFINER VIEW `TMPCachedDistsToPivots`  AS   (select `queriesNearestNeighboursStats`.`evaluationTime` AS `evaluationTime`,`queriesNearestNeighboursStats`.`added` AS `added`,`queriesNearestNeighboursStats`.`queryGesamtId` AS `queryGesamtId`,`queriesNearestNeighboursStats`.`nnGesamtId` AS `nnGesamtId`,`queriesNearestNeighboursStats`.`qscore` AS `qscore`,`queriesNearestNeighboursStats`.`rmsd` AS `rmsd`,`queriesNearestNeighboursStats`.`alignedResidues` AS `alignedResidues`,`queriesNearestNeighboursStats`.`seqIdentity` AS `seqIdentity` from `queriesNearestNeighboursStats` where `queriesNearestNeighboursStats`.`nnGesamtId` in (select `c`.`gesamtId` from ((`pivot512` join `pivotSet` `s`) join `proteinChain` `c`) where `s`.`currentlyUsed` = 1))  ;

--
-- Indexy pro exportované tabulky
--

--
-- Indexy pro tabulku `pivot512`
--
ALTER TABLE `pivot512`
    ADD PRIMARY KEY (`chainIntId`,`pivotSetId`),
  ADD KEY `pivotSetId` (`pivotSetId`),
  ADD KEY `chainIntId` (`chainIntId`);

--
-- Indexy pro tabulku `pivotPairsFor64pSketches`
--
ALTER TABLE `pivotPairsFor64pSketches`
    ADD PRIMARY KEY (`pivotSetId`,`sketchBitOrder`) USING BTREE,
  ADD KEY `pivotPairsFor64pSketches.pivot1` (`pivot1`),
  ADD KEY `pivotPairsFor64pSketches.pivot2` (`pivot2`);

--
-- Indexy pro tabulku `pivotPairsFor512pSketches`
--
ALTER TABLE `pivotPairsFor512pSketches`
    ADD PRIMARY KEY (`pivotSetId`,`sketchBitOrder`) USING BTREE,
  ADD KEY `pivotPairsFor512pSketches.pivot1` (`pivot1`),
  ADD KEY `pivotPairsFor512pSketches.pivot2` (`pivot2`);

--
-- Indexy pro tabulku `pivotSet`
--
ALTER TABLE `pivotSet`
    ADD PRIMARY KEY (`id`),
  ADD KEY `currentlyUsed` (`currentlyUsed`);

--
-- Indexy pro tabulku `protein`
--
ALTER TABLE `protein`
    ADD PRIMARY KEY (`pdbId`);
ALTER TABLE `protein` ADD FULLTEXT KEY `name` (`name`);

--
-- Indexy pro tabulku `proteinChain`
--
ALTER TABLE `proteinChain`
    ADD PRIMARY KEY (`intId`),
  ADD UNIQUE KEY `gesamtIdUnique` (`gesamtId`),
  ADD KEY `gesamtId` (`gesamtId`);

--
-- Indexy pro tabulku `proteinChainMetadata`
--
ALTER TABLE `proteinChainMetadata`
    ADD PRIMARY KEY (`pivotSetId`,`chainIntId`),
  ADD KEY `proteinChainMetadata.pivotSetId` (`pivotSetId`) USING BTREE,
  ADD KEY `lastUpdate` (`lastUpdate`),
  ADD KEY `proteinChainMetadata.sketch512p` (`sketch512p`),
  ADD KEY `proteinChainMetadata.chainIntId,sketch512p` (`chainIntId`,`sketch512p`),
  ADD KEY `proteinChainMetadata.pivotSetId,chainIntId,sketch512p` (`pivotSetId`,`chainIntId`,`sketch512p`) USING BTREE;

--
-- Indexy pro tabulku `queriesNearestNeighboursStats`
--
ALTER TABLE `queriesNearestNeighboursStats`
    ADD PRIMARY KEY (`queryGesamtId`,`nnGesamtId`),
  ADD KEY `queriesNearestNeighboursStats.added` (`added`) USING BTREE,
  ADD KEY `nnGesamtId` (`nnGesamtId`),
  ADD KEY `queryGesamtId` (`queryGesamtId`),
  ADD KEY `evaluationTime` (`evaluationTime`);

--
-- Indexy pro tabulku `savedQueries`
--
ALTER TABLE `savedQueries`
    ADD PRIMARY KEY (`job_id`);

--
-- AUTO_INCREMENT pro tabulky
--

--
-- AUTO_INCREMENT pro tabulku `pivotSet`
--
ALTER TABLE `pivotSet`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'artificial id';

--
-- AUTO_INCREMENT pro tabulku `proteinChain`
--
ALTER TABLE `proteinChain`
    MODIFY `intId` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'int id for PPP codes';

--
-- Omezení pro exportované tabulky
--

--
-- Omezení pro tabulku `pivot512`
--
ALTER TABLE `pivot512`
    ADD CONSTRAINT `pivot.intId` FOREIGN KEY (`chainIntId`) REFERENCES `proteinChain` (`intId`),
  ADD CONSTRAINT `pivot.pivotSetId` FOREIGN KEY (`pivotSetId`) REFERENCES `pivotSet` (`id`);

--
-- Omezení pro tabulku `pivotPairsFor64pSketches`
--
ALTER TABLE `pivotPairsFor64pSketches`
    ADD CONSTRAINT `pivotPairsFor64pSketches.pivot1` FOREIGN KEY (`pivot1`) REFERENCES `pivot512` (`chainIntId`),
  ADD CONSTRAINT `pivotPairsFor64pSketches.pivot2` FOREIGN KEY (`pivot2`) REFERENCES `pivot512` (`chainIntId`),
  ADD CONSTRAINT `pivotPairsFor64pSketches.pivotSetId` FOREIGN KEY (`pivotSetId`) REFERENCES `pivotSet` (`id`);

--
-- Omezení pro tabulku `pivotPairsFor512pSketches`
--
ALTER TABLE `pivotPairsFor512pSketches`
    ADD CONSTRAINT `pivotPairsFor512pSketches.pivot1` FOREIGN KEY (`pivot1`) REFERENCES `pivot512` (`chainIntId`),
  ADD CONSTRAINT `pivotPairsFor512pSketches.pivot2` FOREIGN KEY (`pivot2`) REFERENCES `pivot512` (`chainIntId`),
  ADD CONSTRAINT `pivotPairsFor512pSketches.pivotSetId` FOREIGN KEY (`pivotSetId`) REFERENCES `pivotSet` (`id`);

--
-- Omezení pro tabulku `proteinChainMetadata`
--
ALTER TABLE `proteinChainMetadata`
    ADD CONSTRAINT `proteinChainMetadata.chainIndId` FOREIGN KEY (`chainIntId`) REFERENCES `proteinChain` (`intId`),
  ADD CONSTRAINT `proteinChainMetadata.pivotSetId` FOREIGN KEY (`pivotSetId`) REFERENCES `pivotSet` (`id`);

--
-- Omezení pro tabulku `queriesNearestNeighboursStats`
--
ALTER TABLE `queriesNearestNeighboursStats`
    ADD CONSTRAINT `queriesNearestNeighboursStats_ibfk_1` FOREIGN KEY (`nnGesamtId`) REFERENCES `proteinChain` (`gesamtId`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
