package consulo.javaee.run.configuration;

import com.intellij.java.execution.impl.JavaRunConfigurationExtensionManager;
import consulo.annotation.access.RequiredReadAction;
import consulo.compiler.artifact.Artifact;
import consulo.compiler.scope.CompileScope;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.*;
import consulo.execution.configuration.log.LogFileOptions;
import consulo.execution.configuration.log.PredefinedLogFile;
import consulo.execution.configuration.log.ui.LogConfigurationPanel;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.configuration.ui.SettingsEditorGroup;
import consulo.execution.executor.Executor;
import consulo.execution.localize.ExecutionLocalize;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.ProgramRunner;
import consulo.jakarta.localize.JakartaLocalize;
import consulo.jakartaee.webServer.impl.appServerIntegrations.AppServerIntegration;
import consulo.jakartaee.webServer.impl.deployment.DeploymentModel;
import consulo.jakartaee.webServer.impl.deployment.DeploymentSettings;
import consulo.jakartaee.webServer.impl.oss.server.JavaeeServerModel;
import consulo.jakartaee.webServer.impl.run.configuration.CommonStrategy;
import consulo.jakartaee.webServer.impl.run.configuration.ServerModel;
import consulo.jakartaee.webServer.impl.run.localRun.ExecutableObjectStartupPolicy;
import consulo.java.debugger.impl.GenericDebugRunnerConfiguration;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.javaee.deployment.impl.JavaEEDeploymentSettingsImpl;
import consulo.javaee.run.configuration.editor.JavaEEDeploymentConfigurationEditor;
import consulo.javaee.run.configuration.editor.JavaEEServerConfigurationEditor;
import consulo.javaee.run.configuration.editor.JavaEEStartupConfigurationEditor;
import consulo.javaee.run.configuration.state.JavaEECommandLineState;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jdom.Element;

import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 2017-07-09
 */
public class JavaEEConfigurationImpl extends LocatableConfigurationBase implements CommonStrategy, GenericDebugRunnerConfiguration {
    private JavaEEServerBundleType myBundleType;
    private final boolean myIsLocal;
    private final ExecutableObjectStartupPolicy myStartupPolicy;
    private JavaeeServerModel myServerModel;

    private SettingsBean mySettingsBean = new SettingsBean();

    private JavaEEDeploymentSettingsImpl myDeploymentSettings;

    public String APPLICATION_SERVER_NAME;

    public JavaEEConfigurationImpl(
        Project project,
        ConfigurationFactory factory,
        String name,
        JavaEEServerBundleType bundleType,
        ServerModel serverModel,
        boolean isLocal,
        ExecutableObjectStartupPolicy startupPolicy
    ) {
        super(project, factory, name);
        myBundleType = bundleType;
        myIsLocal = isLocal;
        myStartupPolicy = startupPolicy;
        myServerModel = (JavaeeServerModel)serverModel;
        myDeploymentSettings = new JavaEEDeploymentSettingsImpl(project, bundleType, this);

        myServerModel.setCommonModel(this);
    }

    @Nonnull
    public ExecutableObjectStartupPolicy getStartupPolicy() {
        return myStartupPolicy;
    }

    @Override
    public SettingsBean getSettingsBean() {
        return mySettingsBean;
    }

    @Override
    public AppServerIntegration getIntegration() {
        return myBundleType.getIntegration();
    }

    @Override
    public boolean isLocal() {
        return myIsLocal;
    }

    @Nonnull
    @Override
    public JavaEEServerBundleType getServerBundleType() {
        return myBundleType;
    }

    @Nullable
    @Override
    public Sdk getServerBundle() {
        return APPLICATION_SERVER_NAME == null ? null : SdkTable.getInstance().findSdk(APPLICATION_SERVER_NAME);
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public ServerModel getServerModel() {
        return myServerModel;
    }

    @Override
    public void initialize() {
        for (PredefinedLogFile file : myServerModel.getPredefinedLogFiles()) {
            addPredefinedLogFile(file);
        }
    }

    @Override
    public RunConfiguration clone() {
        JavaEEConfigurationImpl clone = (JavaEEConfigurationImpl)super.clone();
        try {
            JavaeeServerModel serverModel = (JavaeeServerModel)myServerModel.clone();
            serverModel.setCommonModel(clone);
            clone.myServerModel = serverModel;
        }
        catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException();
        }
        clone.myBundleType = myBundleType;
        clone.myDeploymentSettings = myDeploymentSettings.copy(clone);
        return clone;
    }

    @Nullable
    @Override
    public LogFileOptions getOptionsForPredefinedLogFile(PredefinedLogFile predefinedLogFile) {
        LogFileOptions options = myServerModel.getOptionsForPredefinedLogFile(predefinedLogFile);
        if (options != null) {
            return options;
        }
        return super.getOptionsForPredefinedLogFile(predefinedLogFile);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        myServerModel.checkConfiguration();
    }

    @Override
    public Collection<? extends DeploymentModel> getDeploymentModels() {
        return myDeploymentSettings.getDeploymentModels();
    }

    @Override
    public List<Artifact> getDeployedArtifacts() {
        return myDeploymentSettings.getDeployedArtifacts();
    }

    @Override
    public List<Artifact> getArtifactsToBuild() {
        return myDeploymentSettings.getArtifacts2Build();
    }

    @Nullable
    @Override
    @RequiredReadAction
    public DeploymentModel getDeploymentModel(Artifact artifact) {
        return myDeploymentSettings.getOrCreateModel(artifact);
    }

    @Override
    public void setUrlToOpenInBrowser(@Nullable String newUrl) {

    }

    @Nonnull
    @Override
    public DeploymentSettings getDeploymentSettings() {
        return myDeploymentSettings;
    }

    @Override
    public String getUrlToOpenInBrowser() {
        return null;
    }

    @Override
    public CompileScope getCompileScope() {
        return null;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

        APPLICATION_SERVER_NAME = StringUtil.nullize(element.getAttributeValue("APPLICATION_SERVER_NAME"));

        myDeploymentSettings.readExternal(element);

        Element serverSettingsElement = element.getChild("server-settings");
        if (serverSettingsElement != null) {
            myServerModel.readExternal(serverSettingsElement);
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        if (!StringUtil.isEmptyOrSpaces(APPLICATION_SERVER_NAME)) {
            element.setAttribute("APPLICATION_SERVER_NAME", APPLICATION_SERVER_NAME);
        }

        myDeploymentSettings.writeExternal(element);

        Element serverSettingsElement = new Element("server-settings");
        myServerModel.writeExternal(serverSettingsElement);
        element.addContent(serverSettingsElement);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup group = new SettingsEditorGroup<>();
        group.addEditor(JakartaLocalize.titleRunConfigurationEditorServer().get(), new JavaEEServerConfigurationEditor(myBundleType));
        group.addEditor(
            JakartaLocalize.titleRunConfigurationEditorDeployment().get(),
            new JavaEEDeploymentConfigurationEditor(getProject(), myBundleType, this)
        );
        group.addEditor(ExecutionLocalize.logsTabTitle().get(), new LogConfigurationPanel<>());
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        group.addEditor(ExecutionLocalize.runConfigurationStartupConnectionRabTitle().get(), new JavaEEStartupConfigurationEditor());
        return group;
    }

    @Override
    public ConfigurationPerRunnerSettings createRunnerSettings(ConfigurationInfoProvider provider) {
        return super.createRunnerSettings(provider);
    }

    @Override
    public SettingsEditor<ConfigurationPerRunnerSettings> getRunnerSettingsEditor(ProgramRunner runner) {
        return super.getRunnerSettingsEditor(runner);
    }

    @Nonnull
    @Override
    public Module[] getModules() {
        return new Module[0];
    }

    @Nullable
    @Override
    public RunProfileState getState(@Nonnull Executor executor, @Nonnull ExecutionEnvironment environment) throws ExecutionException {
        return new JavaEECommandLineState(environment);
    }
}
