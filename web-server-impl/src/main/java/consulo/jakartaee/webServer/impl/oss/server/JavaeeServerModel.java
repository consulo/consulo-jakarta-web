/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package consulo.jakartaee.webServer.impl.oss.server;

import com.intellij.java.debugger.PositionManager;
import com.intellij.java.debugger.engine.DebugProcess;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import consulo.compiler.artifact.ArtifactUtil;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.log.LogFileOptions;
import consulo.execution.configuration.log.PredefinedLogFile;
import consulo.jakartaee.web.module.extension.JavaWebModuleExtension;
import consulo.jakartaee.webServer.impl.deployment.DeploymentManager;
import consulo.jakartaee.webServer.impl.deployment.DeploymentModel;
import consulo.jakartaee.webServer.impl.deployment.DeploymentProvider;
import consulo.jakartaee.webServer.impl.oss.JavaeeBundle;
import consulo.jakartaee.webServer.impl.oss.admin.JavaeeAdmin;
import consulo.jakartaee.webServer.impl.oss.descriptor.JavaeeDescriptor;
import consulo.jakartaee.webServer.impl.run.configuration.CommonModel;
import consulo.jakartaee.webServer.impl.run.configuration.CommonStrategy;
import consulo.jakartaee.webServer.impl.run.configuration.PredefinedLogFilesProvider;
import consulo.jakartaee.webServer.impl.run.configuration.ServerModel;
import consulo.jakartaee.webServer.impl.run.execution.DefaultOutputProcessor;
import consulo.jakartaee.webServer.impl.run.execution.OutputProcessor;
import consulo.jakartaee.webServer.impl.serverInstances.J2EEServerInstance;
import consulo.javaee.module.extension.JavaEEApplicationModuleExtension;
import consulo.javaee.module.extension.JavaEEModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.remoteServer.configuration.deployment.DeploymentSource;
import consulo.util.io.ClassPathUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.DefaultJDOMExternalizer;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.*;

public abstract class JavaeeServerModel implements ServerModel, PredefinedLogFilesProvider {
    protected static class LogFileFactory {
        private boolean myEnabled;

        public LogFileFactory(boolean enabled) {
            myEnabled = enabled;
        }

        protected String getId() {
            return getClass().getSimpleName();
        }

        protected String getName() {
            return getId();
        }

        protected LogFileOptions createOptions(PredefinedLogFile file) {
            return null;
        }

        protected String getPath() {
            return null;
        }
    }

    protected static class DefaultLogFileFactory extends LogFileFactory {
        public DefaultLogFileFactory() {
            super(true);
        }
    }

    public String USERNAME = getDefaultUsername();

    public String PASSWORD = getDefaultPassword();

    private CommonModel config;

    public PositionManager createPositionManager(DebugProcess process, JavaEEModuleExtension[] scopeFacetsWithIncluded) {
        //TODO [VISTALL] STUB
        return null;
    }

    protected String getJdkRequiredMessage() {
        //TODO [VISTALL] STUB
        return null;
    }

    public int getPingPort() {
        //TODO [VISTALL] STUB
        return -1;
    }

    protected JavaeeAdmin createServerAdmin(JavaeeServerInstance serverInstance) throws Exception {
        //TODO [VISTALL] STUB
        return null;
    }

    public String getVmArgument(String key) {
        return null;
    }

    @Nonnull
    protected List<LogFileFactory> getLogFileFactories() {
        return Collections.emptyList();
    }

    protected String getLogFileId() {
        //TODO [VISTALL] STUB
        return null;
    }

    public boolean isDetectableLocalPort() {
        //TODO [VISTALL] STUB
        return true;
    }

    public boolean isDetectableServerPort() {
        //TODO [VISTALL] STUB
        return true;
    }

    public void onNewConfigurationCreated() {
    }

    public void onConfigurationCopied() {
    }

    public List<Pair<String, String>> getExternalizationMacros() {
        //TODO [VISTALL] STUB
        return Collections.emptyList();
    }

    protected void checkJdkAtLeast7() {
        //TODO [VISTALL] STUB
    }

    protected void checkHasJdk() {
        //TODO [VISTALL] STUB
    }

    protected String getJdkTooOldMessage(String description) {
        //TODO [VISTALL] STUB
        return null;
    }

    public Sdk getJre() {
        //TODO [VISTALL] STUB
        return null;
    }

    public String getJrePath() {
        //TODO [VISTALL] STUB
        return null;
    }

    public boolean undeployBeforeDeploy(DeploymentModel deploymentModel) {
        //TODO [VISTALL] STUB
        return false;
    }

    @Override
    public void setCommonModel(CommonModel config) {
        this.config = config;
    }

    @Override
    public J2EEServerInstance createServerInstance() throws ExecutionException {
        /*try
		{
			((JavaeeIntegration) config.getIntegration()).checkValidServerHome(getHome(), getVersion());
			ClassLoader loader = new JavaeeClassLoader(getLibraries(), getExcludes(), getClass().getClassLoader());
			Constructor<?> constructor = loader.loadClass(getServerName()).getDeclaredConstructor();
			constructor.setAccessible(true);
			JavaeeServer server = JavaeeServer.class.cast(constructor.newInstance());
			server.setHost(getServerHost());
			server.setPort(getServerPort());
			server.setUsername(USERNAME);
			server.setPassword(PASSWORD);
			return new JavaeeServerInstance(config, server);
		}
		catch(Exception e)
		{
			throw new ExecutionException(e.getMessage(), e);
		} */
        throw new UnsupportedOperationException();
    }

    @Override
    public DeploymentProvider getDeploymentProvider() {
        return new JavaeeDeploymentProvider(false);
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    @Override
    public int getLocalPort() {
        return getDefaultPort();
    }

    @Override
    public String getDefaultUrlForBrowser() {
        return getDefaultUrlForBrowser(true);
    }

    @Override
    public OutputProcessor createOutputProcessor(ProcessHandler handler, J2EEServerInstance instance) {
        return new DefaultOutputProcessor(handler);
    }

    @Override
    @Nullable
    public List<Pair<String, Integer>> getAddressesToCheck() {
        return config.isLocal() ? Collections.singletonList(new Pair<>(getServerHost(), getServerPort())) : null;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isNotEmpty(USERNAME) && StringUtil.isEmpty(PASSWORD)) {
            throw new RuntimeConfigurationError(JavaeeBundle.message("ServerModel.password"));
        }
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Nullable
    public String getHome() {
        Sdk server = config.getServerBundle();
        return server != null ? server.getHomePath() : null;
    }

    @Nullable
    public String getVersion() {
        Sdk server = config.getServerBundle();
        return server != null ? server.getVersionString() : null;
    }

    public String getVmArguments() {
        return (config instanceof CommonStrategy) ? ((CommonStrategy) config).getSettingsBean().COMMON_VM_ARGUMENTS : "";
    }

    protected boolean isDeploymentSourceSupported(DeploymentSource source) {
        return true;
    }

    @NonNls
    protected abstract String getDefaultUsername();

    @NonNls
    protected abstract String getDefaultPassword();

    protected abstract int getServerPort();

    protected String getServerHost() {
        return config.getHost();
    }

    public CommonModel getCommonModel() {
        return config;
    }

    protected List<File> getLibraries() throws ExecutionException {
        List<File> libraries = new ArrayList<>();
        libraries.add(new File(ClassPathUtil.getJarPathForClass(getClass())));

        Sdk serverBundle = config.getServerBundle();
        if (serverBundle != null) {
            for (VirtualFile file : serverBundle.getRootProvider().getFiles(BinariesOrderRootType.ID)) {
                libraries.add(new File(file.getPresentableUrl()));
            }
        }
        return libraries;
    }

    protected File getPathUnderHome(String name) {
        return null;
    }

    protected Set<Class<?>> getExcludes() {
        Set<Class<?>> excludes = new HashSet<>();
        excludes.add(JavaeeServer.class);
        excludes.add(JavaeeDescriptor.class);
        return excludes;
    }

    String getDefaultUrlForBrowser(boolean addContextPath) {
        @NonNls String url = "http://" + config.getHost() + ':' + config.getPort();
        if (addContextPath) {
            String path = getContextPathFromAppFacets();
            if (StringUtil.isEmpty(path)) {
                path = getContextPathFromWebFacets();
            }
            if (StringUtil.isNotEmpty(path)) {
                url = ArtifactUtil.concatPaths(url, path);
            }
            url = ArtifactUtil.concatPaths(url, "/");
        }
        return url;
    }

    @Nullable
    @NonNls
    private String getContextPathFromAppFacets() {
		/*List<JavaEEApplicationModuleExtension> facets = new ArrayList<JavaEEApplicationModuleExtension>();
		for(DeploymentModel deployment : config.getDeploymentModels())
		{
			Artifact artifact = deployment.getArtifact();
			JavaEEModuleExtension facet = deployment.getFacet();
			if(artifact != null)
			{
				facets.addAll(JavaeeArtifactUtil.getInstance().getFacetsIncludedInArtifact(config.getProject(), artifact, JavaEEApplicationModuleExtension.class));
			}
			else if(facet instanceof JavaEEApplicationModuleExtension)
			{
				facets.add((JavaEEApplicationModuleExtension) facet);
			}
		}
		for(JavaEEApplicationModuleExtension facet : facets)
		{
			String path = getContextPathFromAppFacet(facet);
			if(path != null)
			{
				return path;
			}
		}*/
        return null;
    }

    @Nullable
    @NonNls
    private String getContextPathFromWebFacets() {
	/*	for(DeploymentModel deployment : config.getDeploymentModels())
		{
			Artifact artifact = deployment.getArtifact();
			JavaEEModuleExtension facet = deployment.getFacet();
			if(artifact != null)
			{
				for(JavaWebModuleExtension webFacet : JavaeeArtifactUtil.getInstance().getFacetsIncludedInArtifact(config.getProject(), artifact, JavaWebModuleExtension.class))
				{
					String path = getContextPathFromWebFacet(webFacet, deployment);
					if(path != null)
					{
						return path;
					}
				}
			}
			else if(facet instanceof WebFacet)
			{
				String path = getContextPathFromWebFacet((WebFacet) facet, deployment);
				if(path != null)
				{
					return path;
				}
			}
		}
		for(JavaWebModuleExtension facet : JavaeeFacetUtil.getInstance().getJavaeeFacets(WebFacet.ID, config.getProject()))
		{
			DeploymentModel model = config.getDeploymentModel(facet);
			if((model instanceof JavaeeDeploymentModel) && ((JavaeeDeploymentModel) model).isDeploymentSourceSupported(model.getDeploymentSource()))
			{
				String path = getContextPathFromWebFacet(facet, model);
				if(path != null)
				{
					return path;
				}
			}
		} */
        return null;
    }

    @Nullable
    @NonNls
    protected String getContextPathFromAppFacet(JavaEEApplicationModuleExtension facet) {
        JavaeeApplication application = facet.getRoot();
        if (application != null) {
            for (JavaeeModule module : application.getModules()) {
                if (module.getWeb().getXmlTag() != null) {
                    return module.getWeb().getContextRoot().getValue();
                }
            }
        }
        return null;
    }

    @Nullable
    @NonNls
    protected String getContextPathFromWebFacet(JavaWebModuleExtension facet, DeploymentModel deployment) {
        DeploymentManager manager = DeploymentManager.getInstance(config.getProject());
        File source = manager.getDeploymentSource(deployment);
        return (source != null) ? StringUtil.trimEnd(source.getName(), ".war") : null;
    }

    @Override
    @Nonnull
    public PredefinedLogFile[] getPredefinedLogFiles() {
        return new PredefinedLogFile[]{new PredefinedLogFile(getLogFileId(), true)};
    }

    @Override
    @Nullable
    public LogFileOptions getOptionsForPredefinedLogFile(PredefinedLogFile file) {
        if (getLogFileId().equals(file.getId())) {
            String path = getLogFilePath(getHome());
            if (path != null) {
                String name = JavaeeBundle.message("ServerModel.logfile", getLogFileId());
                return new LogFileOptions(name, path, file.isEnabled(), true, false);
            }
        }
        return null;
    }

    @Nullable
    protected String getLogFilePath(String home) {
        return null;
    }

    protected boolean isTruncateLogFile() {
        return false;
    }
}
