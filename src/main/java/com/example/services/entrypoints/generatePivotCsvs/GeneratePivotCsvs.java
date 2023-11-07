package com.example.services.entrypoints.generatePivotCsvs;

import com.example.dao.*;
import com.example.model.PivotSet;
import com.example.service.PivotSetService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.CliApp.getSessionFactory;

public class GeneratePivotCsvs {
    public static void run(String filename) throws IOException {
        try (SessionFactory sessionFactory = getSessionFactory(); Session session = sessionFactory.openSession()) {
            var pivotSetService = new PivotSetService(new PivotSetDao(session));
            var pivotsDao = new PivotPairsForXpSketchesDao(session);
            PivotSet pivotSet = pivotSetService.GetCurrentPivotSet();
            var pivots = pivotsDao.loadPairs(pivotSet, "pivotPairsFor512pSketches");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                for (String[] pivotPair : pivots) {
                    writer.write("\"" + pivotPair[0] + "\";\"" + pivotPair[1] + "\"");
                    writer.newLine();
                }
            }
        }
    }
}
