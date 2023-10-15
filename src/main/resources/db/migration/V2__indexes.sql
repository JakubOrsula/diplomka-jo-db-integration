ALTER TABLE proteinChainMetadata ADD INDEX pivot_chain_idx (pivotSetId, chainIntId);
ALTER TABLE proteinChain ADD INDEX indexed_intid_idx (indexedAsDataObject, intId);