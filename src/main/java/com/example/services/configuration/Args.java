package com.example.services.configuration;

import com.beust.jcommander.Parameter;

public class Args {

    @Parameter(names = "--run", description = """
            Function to run:
            checkGesamtLibPresence
            checkComputedDistances
            checkGesamtBinaryFilesAreInDB
            removeProteinChainsWithoutFile
            computeDistances
            learnSketches
            applySketches
            secondaryFiltering""", required = true)
    public String runFunction;

    @Parameter(names = "--dry-run", description = "If set, the program will not perform write operations")
    public boolean dryRun = false;
}