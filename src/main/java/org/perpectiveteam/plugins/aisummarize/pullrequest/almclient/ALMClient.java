package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient;

import java.util.List;

import org.perpectiveteam.plugins.aisummarize.pullrequest.dtobuilder.FileDiff;
import org.sonar.db.alm.setting.ALM;

public interface ALMClient {
    

    List<FileDiff> fetchPullRequestFiles(String owner, String repo, String pullRequestNumber);

    String getProviderName();

    String getDefaultTargetBranch();

    List<ALM> alm();
}
