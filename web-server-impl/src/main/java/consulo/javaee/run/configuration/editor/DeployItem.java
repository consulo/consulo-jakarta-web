package consulo.javaee.run.configuration.editor;

import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactPointer;
import consulo.configurable.ConfigurationException;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.jakartaee.webServer.impl.deployment.DeploymentModel;
import consulo.jakartaee.webServer.impl.run.configuration.CommonModel;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.remoteServer.configuration.deployment.ArtifactDeploymentSource;
import consulo.remoteServer.configuration.deployment.DeploymentSource;
import consulo.ui.Component;
import consulo.ui.TextAttribute;
import consulo.ui.TextItemPresentation;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;
import org.jdom.Element;

/**
 * @author VISTALL
 * @since 2017-07-09
 */
class DeployItem implements Disposable {
    private final DeploymentSource myDeploymentSource;
    @Nullable
    private final DeploymentModel myDeploymentModel;
    @Nullable
    private final SettingsEditor<DeploymentModel> myEditor;

    private final LocalizeValue myPresentableName;
    @Nullable
    private final Image myIcon;
    private final boolean myValid;

    private boolean myEditorShown;

    DeployItem(CommonModel commonModel, DeploymentSource deploymentSource, JavaEEServerBundleType bundleType) {
        myDeploymentSource = deploymentSource;

        myDeploymentModel = bundleType.createNewDeploymentModel(commonModel, deploymentSource);
        myEditor = bundleType.createAdditionalDeploymentSettingsEditor(commonModel, deploymentSource);
        if (myEditor != null) {
            Disposer.register(this, myEditor);
        }

        myPresentableName = deploymentSource.getPresentableName();
        myValid = deploymentSource.isValid();
        myIcon = myValid ? deploymentSource.getIcon() : PlatformIconGroup.toolbarUnknown();
    }

    public void render(TextItemPresentation presentation) {
        if (myIcon != null) {
            presentation.withIcon(myIcon);
        }
        presentation.append(myPresentableName, myValid ? TextAttribute.REGULAR : TextAttribute.ERROR);
    }

    @Nullable
    @RequiredUIAccess
    public Component getSettingsComponent() {
        SettingsEditor<DeploymentModel> editor = myEditor;
        DeploymentModel deploymentModel = myDeploymentModel;
        if (editor == null || deploymentModel == null) {
            return null;
        }

        Component component = editor.getUIComponent();
        if (!myEditorShown) {
            editor.resetFrom(deploymentModel);
            myEditorShown = true;
        }
        return component;
    }

    public void resetFrom(DeploymentModel deploymentModel) {
        DeploymentModel itemModel = myDeploymentModel;
        if (itemModel != null) {
            copy(deploymentModel, itemModel);
        }
    }

    public void saveEditorState() throws ConfigurationException {
        SettingsEditor<DeploymentModel> editor = myEditor;
        DeploymentModel deploymentModel = myDeploymentModel;
        if (myEditorShown && editor != null && deploymentModel != null) {
            editor.applyTo(deploymentModel);
        }
    }

    public void applyTo(DeploymentModel deploymentModel) throws ConfigurationException {
        SettingsEditor<DeploymentModel> editor = myEditor;
        if (myEditorShown && editor != null) {
            editor.applyTo(deploymentModel);
            return;
        }

        DeploymentModel itemModel = myDeploymentModel;
        if (itemModel != null) {
            copy(itemModel, deploymentModel);
        }
    }

    public DeploymentSource getDeploymentSource() {
        return myDeploymentSource;
    }

    @Nullable
    public ArtifactPointer getArtifactPointer() {
        if (myDeploymentSource instanceof ArtifactDeploymentSource artifactDeploymentSource) {
            return artifactDeploymentSource.getArtifactPointer();
        }
        return null;
    }

    @Nullable
    public Artifact getArtifact() {
        ArtifactPointer artifactPointer = getArtifactPointer();
        return artifactPointer == null ? null : artifactPointer.get();
    }

    private static void copy(DeploymentModel from, DeploymentModel to) {
        Element element = new Element("settings");
        from.writeExternal(element);
        to.readExternal(element);
    }

    @Override
    public void dispose() {
    }
}
