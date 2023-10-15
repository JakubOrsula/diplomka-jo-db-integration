create table proteinChainMetadataTransfer
(
    pivotSetId     int                                   not null comment 'foreign key',
    chainIntId     int(11) unsigned                      not null comment 'foreign key',
    sketch512p     varchar(400)                          comment 'long sketch',
    sketch64p      varchar(96)                           comment 'short sketch',
    primary key (pivotSetId, chainIntId),
    constraint `proteinChainMetadataTransfer.chainIndId`
        foreign key (chainIntId) references proteinChain (intId),
    constraint `proteinChainMetadataTransfer.pivotSetId`
        foreign key (pivotSetId) references pivotSet (id)
);