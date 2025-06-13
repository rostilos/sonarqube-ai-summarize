package org.perpectiveteam.plugins.aisummarize;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.PluginContextImpl;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarEdition;

import static org.junit.jupiter.api.Assertions.*;

class AiSummarizePluginTest {

    @Test
    void testDefine() {
        SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.create(9, 9), SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
        Plugin.Context context = new PluginContextImpl.Builder()
            .setSonarRuntime(runtime)
            .build();

        AiSummarizePlugin plugin = new AiSummarizePlugin();
        plugin.define(context);

        assertTrue(context.getExtensions().size() > 0, 
            "Plugin should register extensions");
    }
}
