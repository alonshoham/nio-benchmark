package jmh.utils;

import com.gigaspaces.internal.utils.GsEnv;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.CannotFindSpaceException;
import org.openspaces.core.space.EmbeddedSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

public class GigaSpaceFactory {
    public static GigaSpace getOrCreateSpace(String spaceName, boolean embedded) {
        if (embedded) {
            spaceName = spaceName+System.currentTimeMillis();
            return new GigaSpaceConfigurer(new EmbeddedSpaceConfigurer(spaceName)).create();
        } else {
            CannotFindSpaceException cfse=null;
            for (int i=1; i<=5; i++) {
                try {
                    return new GigaSpaceConfigurer(new SpaceProxyConfigurer(spaceName)
                            .lookupLocators(GsEnv.get("LOOKUP_LOCATORS", "127.0.0.1"))
                            .lookupTimeout(i*15000))
                            .create();
                } catch (CannotFindSpaceException e) {
                    System.err.println("Failed to find space: " + e.getMessage());
                    cfse = e;
                }
            }
            throw cfse;
        }
    }
}