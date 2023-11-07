package com.example.services.entrypoints.secondaryFiltering;

import com.example.dao.PivotSetDao;
import com.example.dao.ProteinChainMetadataDao;
import com.example.service.PivotSetService;
import com.example.service.ProteinChainMetadataService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import static com.example.CliApp.getSessionFactory;

//i hate singletons i hate singletons i hate singletons
public class SketchDataSingleton {
    private static SketchDataSingleton instance = null;
    private final ProteinChainMetadataService proteinChainMetadataService;
    private final AllSketchesResult allSketchesResult;

    private final Session session;
    private SketchDataSingleton() {
        //get session from factory
        SessionFactory sessionFactory = getSessionFactory();
        this.session = sessionFactory.openSession();
        this.proteinChainMetadataService = new ProteinChainMetadataService(new ProteinChainMetadataDao(session), new PivotSetService(new PivotSetDao(session)));
        this.allSketchesResult = proteinChainMetadataService.getAllSketches();
        session.close();
    }

    public static SketchDataSingleton getInstance() {
        if (instance == null) {
            instance = new SketchDataSingleton();
        }
        return instance;
    }

    public long[] getSketch(int sketchId) {
        var arrId = allSketchesResult.mapping.get(sketchId);
        if (arrId == null) {
            System.out.println("Sketch with id " + sketchId + " not found");
            return null;
        }
        return allSketchesResult.sketches.get(arrId);
    }
}
