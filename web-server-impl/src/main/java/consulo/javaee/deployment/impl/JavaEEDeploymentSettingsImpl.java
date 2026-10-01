package consulo.javaee.deployment.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.compiler.artifact.Artifact;
import consulo.jakartaee.webServer.impl.deployment.DeploymentModel;
import consulo.jakartaee.webServer.impl.deployment.DeploymentSettings;
import consulo.jakartaee.webServer.impl.run.configuration.CommonModel;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.project.Project;
import consulo.remoteServer.configuration.deployment.DeploymentSource;
import consulo.remoteServer.configuration.deployment.DeploymentSourceFactory;
import consulo.remoteServer.configuration.deployment.DeploymentSourceType;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2017-07-15
 */
public class JavaEEDeploymentSettingsImpl implements DeploymentSettings {
    private final List<DeploymentModel> myItems = new ArrayList<>();
    private final Project myProject;
    private final JavaEEServerBundleType myBundleType;
    private final CommonModel myCommonModel;
    private final DeploymentSourceFactory myDeploymentSourceFactory;

    public JavaEEDeploymentSettingsImpl(Project project, JavaEEServerBundleType bundleType, CommonModel commonModel) {
        myProject = project;
        myBundleType = bundleType;
        myCommonModel = commonModel;
        myDeploymentSourceFactory = project.getInstance(DeploymentSourceFactory.class);
    }

    public JavaEEDeploymentSettingsImpl copy(CommonModel commonModel) {
        JavaEEDeploymentSettingsImpl copy = new JavaEEDeploymentSettingsImpl(myProject, myBundleType, commonModel);
        Element element = new Element("copy");
        writeExternal(element);
        copy.readExternal(element);
        return copy;
    }

    public void addModel(@Nonnull DeploymentModel model) {
        myItems.add(model);
    }

    public void removeAll() {
        myItems.clear();
    }

    @Nonnull
    @Override
    public List<DeploymentModel> getDeploymentModels() {
        return myItems;
    }

    @Override
    public void removeModel(@Nonnull DeploymentModel model) {
        myItems.remove(model);
    }

    @Nullable
    @Override
    public DeploymentModel getModel(@Nonnull Artifact artifact) {
        for (DeploymentModel item : myItems) {
            Artifact other = item.getArtifact();
            if (artifact.equals(other)) {
                return item;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public List<Artifact> getDeployedArtifacts() {
        List<Artifact> artifacts = new ArrayList<>();
        for (DeploymentModel item : myItems) {
            Artifact artifact = item.getArtifact();
            if (artifact != null) {
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

    @Nonnull
    @Override
    public List<Artifact> getArtifacts2Build() {
        return getDeployedArtifacts();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public DeploymentModel getOrCreateModel(@Nonnull Artifact artifact) {
        return getOrCreateModel(myDeploymentSourceFactory.createArtifactDeploymentSource(artifact));
    }

    @Nonnull
    @Override
    public DeploymentModel getOrCreateModel(@Nonnull DeploymentSource source) {
        DeploymentModel newDeploymentModel = myBundleType.createNewDeploymentModel(myCommonModel, source);
        myItems.add(newDeploymentModel);
        return newDeploymentModel;
    }

    public void readExternal(Element element) {
        myItems.clear();

        Element deploymentElement = element.getChild("deployment");
        if (deploymentElement == null) {
            return;
        }

        for (Element childElement : deploymentElement.getChildren()) {
            DeploymentSourceType<?> deploymentSourceType = findDeploymentSourceType(childElement.getName());
            if (deploymentSourceType == null) {
                continue;
            }

            DeploymentSource deploymentSource = deploymentSourceType.load(childElement, myProject);

            DeploymentModel model = myBundleType.createNewDeploymentModel(myCommonModel, deploymentSource);

            Element settingsElement = childElement.getChild("settings");
            if (settingsElement != null) {
                model.readExternal(settingsElement);
            }

            myItems.add(model);
        }
    }

    @Nullable
    private static DeploymentSourceType<?> findDeploymentSourceType(@Nullable String id) {
        for (DeploymentSourceType<?> type : DeploymentSourceType.EP_NAME.getExtensions()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void writeExternal(Element rootElement) {
        if (myItems.isEmpty()) {
            return;
        }

        Element deploymentElement = new Element("deployment");
        rootElement.addContent(deploymentElement);

        for (DeploymentModel item : myItems) {
            DeploymentSource deploymentSource = item.getDeploymentSource();
            DeploymentSourceType type = deploymentSource.getType();

            Element childElement = new Element(type.getId());
            deploymentElement.addContent(childElement);

            type.save(deploymentSource, childElement);

            Element settingsElement = new Element("settings");
            item.writeExternal(settingsElement);

            childElement.addContent(settingsElement);
        }
    }
}
