CREATE TABLE queryProtein
(
    queryProteinId      int(11) unsigned auto_increment primary key,
    proteinChainId      int(11) unsigned not null,
    FOREIGN KEY (proteinChainId) REFERENCES proteinChain(intId)
);
