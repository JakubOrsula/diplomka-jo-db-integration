package com.example.services.distance;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vlada
 */
public class ProteinNativeQScoreDistance implements Serializable {

    /**
     * Class id for Java serialization.
     */
    private static final long serialVersionUID = 124687651462L;

    public static final Float IMPLICIT_INNER_PARAMETER_ON_SIZE_CHECK = 0.6f;
    private static final Logger LOG = Logger.getLogger(ProteinNativeQScoreDistance.class.getName());

    private static native void init(String archiveDirectory);

    private static native float[] getStats(String id1, String id2, float inherentApprox);

    /**
     * Must be called before first evaluation. Objects from specified file are
     * read into the main memorys for more efficient distance evaluations
     *
     * @param gesamtLibraryPath
     * according to gesamt
     */
    public static void initDistance(String gesamtLibraryPath) {
        try {
            // make sure that jre can find it by placing e.g
            // env var LD_LIBRARY_PATH=/home/jakub/Documents/src/gesamt_distance/build/distance
            System.loadLibrary("ProteinDistance");
            init(gesamtLibraryPath);
        } catch (java.lang.UnsatisfiedLinkError | Exception ex) {
            LOG.log(Level.WARNING, "Initialization of the distance function not successfull.", ex);
        }

    }

    /**
     *
     * @param o1
     * @param o2
     * @param innerParameterOnSizeDiff 0 for no check, 0.6 according tu us, 0.7
     * @return
     */
    public static float[] getStatsFloats(String o1, String o2, float innerParameterOnSizeDiff) {
        if (o1 == null || o2 == null) {
            LOG.log(Level.SEVERE, "Attempt to evaluate distance between null proteins {0}, {1}.", new Object[]{o1, o2});
        }
        try {
            float[] ret = getStats(o1, o2, innerParameterOnSizeDiff);
            if (o1.equals(o2)) {
                ret[0] = 1; // identity fix
            }
            return ret;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unsuccessful attempt to evaluate distance between protein structures with IDs {0}, {1}. Original exception: {2}", new Object[]{o1, o2, e.getMessage()});
            throw new IllegalArgumentException("Unsuccessful attempt to evaluate distance between protein structures with IDs " + o1 + ", " + o2);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public float getMaxDistance() {
        return 1f;
    }

}
