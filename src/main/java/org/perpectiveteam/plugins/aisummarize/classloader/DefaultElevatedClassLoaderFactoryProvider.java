package org.perpectiveteam.plugins.aisummarize.classloader;

import org.sonar.api.Plugin;

/**
 * @author Michael Clarke
 */
public final class DefaultElevatedClassLoaderFactoryProvider implements ElevatedClassLoaderFactoryProvider {

    private static final DefaultElevatedClassLoaderFactoryProvider INSTANCE =
            new DefaultElevatedClassLoaderFactoryProvider();

    private DefaultElevatedClassLoaderFactoryProvider() {
        super();
    }

    @Override
    public ElevatedClassLoaderFactory createFactory(Plugin.Context context) {
        return ProviderType.fromName(
                context.getBootConfiguration().get(ElevatedClassLoaderFactoryProvider.class.getName() + ".providerType")
                        .orElse(ProviderType.CLASS_REFERENCE.name())).createFactory(context);
    }

    public static DefaultElevatedClassLoaderFactoryProvider getInstance() {
        return INSTANCE;
    }

}
