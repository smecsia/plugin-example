package com.my.plugin.app;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.eclipse.aether.connector.wagon.WagonProvider;

public class ManualWagonProvider implements WagonProvider {
    public Wagon lookup(String roleHint)
            throws Exception {
        if ("file".equals(roleHint)) {
            return new FileWagon();
        } else if (roleHint != null && roleHint.startsWith("http")) { // http and https
            return new HttpWagon();
        }
        return null;
    }

    public void release(Wagon wagon) {
        // no-op
    }
}
