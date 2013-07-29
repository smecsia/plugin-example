package com.my.plugin.app;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DependencyResolver {

    public static final String MAVEN_CENTRAL_URL = "http://repo1.maven.org/maven2";
    public static class ResolveResult {
        public String classPath;
        public List<ArtifactResult> artifactResults;

        public ResolveResult(String classPath, List<ArtifactResult> artifactResults) {
            this.classPath = classPath;
            this.artifactResults = artifactResults;
        }
    }

    final RepositorySystemSession session;
    final RepositorySystem repositorySystem;
    final List<String> repositories = new ArrayList<String>();

    public DependencyResolver(File localRepoDir, String... repos) throws IOException {
        repositorySystem = newRepositorySystem();
        session = newSession(repositorySystem, localRepoDir);
        repositories.addAll(Arrays.asList(repos));
    }

    public synchronized ResolveResult resolve(String artifactCoords) throws Exception {
        Dependency dependency = new Dependency(new DefaultArtifact(artifactCoords), "compile");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);

        for (int i = 0; i < repositories.size(); ++i) {
            final String repoUrl = repositories.get(i);
            collectRequest.addRepository(i > 0 ? repo(repoUrl, null, "default") : repo(repoUrl, "central", "default"));
        }

        DependencyNode node = repositorySystem.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(node);

        DependencyResult res = repositorySystem.resolveDependencies(session, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);
        return new ResolveResult(nlg.getClassPath(), res.getArtifactResults());
    }

    private RepositorySystemSession newSession(RepositorySystem system, File localRepoDir) throws IOException {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(localRepoDir.getAbsolutePath());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        return session;
    }

    private RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        return locator.getService(RepositorySystem.class);
    }


    private RemoteRepository repo(String repoUrl) {
        return new RemoteRepository.Builder(null, null, repoUrl).build();
    }

    private RemoteRepository repo(String repoUrl, String repoName, String repoType) {
        return new RemoteRepository.Builder(repoName, repoType, repoUrl).build();
    }
}
