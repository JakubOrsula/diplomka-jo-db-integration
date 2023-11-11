package com.example.services.entrypoints.generatePivotCsvs;

import com.example.dao.*;
import com.example.model.PivotSet;
import com.example.service.PivotSetService;
import com.example.utils.UnrecoverableError;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.CliApp.getSessionFactory;

public class GeneratePivotCsvs {
    public static void run(SessionFactory sessionFactory, String filename, String storageTableName) {
        try (Session session = sessionFactory.openSession() ) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotsDao = new PivotPairsForXpSketchesDao(session);
            PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
            var pivots = pivotsDao.loadPairs(pivotSet, storageTableName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                for (String[] pivotPair : pivots) {
                    writer.write("\"" + pivotPair[0] + "\";\"" + pivotPair[1] + "\"");
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new UnrecoverableError("Failed to write the csv.", e);
            }
            System.out.println("Pivot csv " + filename + " generated.");
        }
    }
}
