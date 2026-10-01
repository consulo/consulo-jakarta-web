package consulo.javaee.run.configuration.editor;

import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactManager;
import consulo.compiler.artifact.execution.BuildArtifactsBeforeRunTaskHelper;
import consulo.configurable.ConfigurationException;
import consulo.dataContext.DataContext;
import consulo.disposer.Disposer;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.jakarta.localize.JakartaLocalize;
import consulo.jakartaee.webServer.impl.deployment.DeploymentModel;
import consulo.jakartaee.webServer.impl.run.configuration.CommonModel;
import consulo.javaee.artifact.ExplodedWarArtifactType;
import consulo.javaee.bundle.JavaEEServerBundleType;
import consulo.javaee.deployment.impl.JavaEEDeploymentSettingsImpl;
import consulo.javaee.run.configuration.JavaEEConfigurationImpl;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.remoteServer.configuration.deployment.DeploymentSourceFactory;
import consulo.ui.Component;
import consulo.ui.ListBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionToolbarPosition;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.popup.BaseListPopupStep;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.PopupStep;
import consulo.ui.ex.toolbar.AddAction;
import consulo.ui.ex.toolbar.EditAction;
import consulo.ui.ex.toolbar.RemoveAction;
import consulo.ui.ex.toolbar.ToolbarDecoratorBuilderFactory;
import consulo.ui.image.Image;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.SplitLayoutPosition;
import consulo.ui.layout.TwoComponentSplitLayout;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2017-07-09
 */
public class JavaEEDeploymentConfigurationEditor extends SettingsEditor<JavaEEConfigurationImpl> {
    private final Project myProject;
    private final JavaEEServerBundleType myBundleType;
    private final CommonModel myCommonModel;
    private final BuildArtifactsBeforeRunTaskHelper myBuildArtifactsBeforeRunTaskHelper;

    private final MutableFlatDataModel<DeployItem> myItems = FlatDataModel.of(List.of());

    @Nullable
    private ListBox<DeployItem> myItemList;
    @Nullable
    private DockLayout myItemSettingsPanel;
    @Nullable
    private DeployItem myShownItem;

    public JavaEEDeploymentConfigurationEditor(Project project, JavaEEServerBundleType bundleType, CommonModel commonModel) {
        myProject = project;
        myBundleType = bundleType;
        myCommonModel = commonModel;
        myBuildArtifactsBeforeRunTaskHelper = project.getInstance(BuildArtifactsBeforeRunTaskHelper.class);
    }

    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        ListBox<DeployItem> itemList = ListBox.create(myItems);
        itemList.setRender((presentation, item) -> {
            DeployItem deployItem = item.getValue();
            if (deployItem != null) {
                deployItem.render(presentation);
            }
        });
        itemList.addValueListener(event -> showItemSettings(event.getValue()));
        myItemList = itemList;

        Component itemListPanel = ToolbarDecoratorBuilderFactory.getInstance()
            .create(itemList)
            .addOrReplaceAction(new DeployItemAddAction())
            .addOrReplaceAction(new DeployItemRemoveAction())
            .disableAction(EditAction.class)
            .withToolbarPosition(ActionToolbarPosition.RIGHT)
            .build();

        DockLayout itemSettingsPanel = DockLayout.create();
        myItemSettingsPanel = itemSettingsPanel;

        TwoComponentSplitLayout splitLayout = TwoComponentSplitLayout.create(SplitLayoutPosition.HORIZONTAL)
            .withFirstComponent(itemListPanel)
            .withSecondComponent(itemSettingsPanel)
            .withProportion(40);

        return LabeledLayout.create(
            JakartaLocalize.borderRunConfigurationEditorDeployAtServerStartup(),
            DockLayout.create().center(splitLayout)
        );
    }

    @RequiredUIAccess
    private void showItemSettings(@Nullable DeployItem item) {
        DeployItem shownItem = myShownItem;
        if (shownItem == item) {
            return;
        }

        if (shownItem != null) {
            try {
                shownItem.saveEditorState();
            }
            catch (ConfigurationException ignored) {
            }
        }

        clearItemSettings();

        DockLayout itemSettingsPanel = myItemSettingsPanel;
        if (item == null || itemSettingsPanel == null) {
            return;
        }

        Component settingsComponent = item.getSettingsComponent();
        if (settingsComponent != null) {
            itemSettingsPanel.center(settingsComponent);
        }
        myShownItem = item;
    }

    @RequiredUIAccess
    private void clearItemSettings() {
        myShownItem = null;

        DockLayout itemSettingsPanel = myItemSettingsPanel;
        if (itemSettingsPanel != null) {
            itemSettingsPanel.removeAll();
        }
    }

    private List<Artifact> collectDeployableArtifacts() {
        Set<Artifact> deployed = new HashSet<>();
        for (DeployItem item : myItems) {
            Artifact artifact = item.getArtifact();
            if (artifact != null) {
                deployed.add(artifact);
            }
        }

        List<Artifact> artifacts = new ArrayList<>();
        for (Artifact artifact : ArtifactManager.getInstance(myProject).getArtifacts()) {
            if (artifact.getArtifactType() == ExplodedWarArtifactType.getInstance() && !deployed.contains(artifact)) {
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

    @RequiredUIAccess
    private void chooseArtifact(AnActionEvent e, List<Artifact> artifacts) {
        DataContext dataContext = e.getDataContext();

        BaseListPopupStep<Artifact> step = new BaseListPopupStep<>(JakartaLocalize.titleRunConfigurationEditorChooseArtifact().get(), artifacts) {
            @Override
            public String getTextFor(Artifact value) {
                return value.getName();
            }

            @Override
            public Image getIconFor(Artifact value) {
                return value.getArtifactType().getIcon();
            }

            @Override
            public PopupStep onChosen(Artifact selectedValue, boolean finalChoice) {
                return doFinalStep(() -> addArtifact(selectedValue, dataContext));
            }
        };

        JBPopupFactory.getInstance().createListPopup(myProject, step).showUnderneathOf(e);
    }

    @RequiredUIAccess
    private void addArtifact(Artifact artifact, DataContext dataContext) {
        DeploymentSourceFactory factory = myProject.getInstance(DeploymentSourceFactory.class);

        DeployItem item = new DeployItem(myCommonModel, factory.createArtifactDeploymentSource(artifact), myBundleType);
        myItems.add(item);

        ListBox<DeployItem> itemList = myItemList;
        if (itemList != null) {
            itemList.setValue(item);
        }

        myBuildArtifactsBeforeRunTaskHelper.setBuildArtifactBeforeRunOption(dataContext, artifact, true);
    }

    @Override
    protected void disposeEditor() {
        super.disposeEditor();

        for (DeployItem item : myItems) {
            Disposer.dispose(item);
        }
    }

    @Override
    @RequiredUIAccess
    protected void resetEditorFrom(JavaEEConfigurationImpl configuration) {
        clearItemSettings();

        List<DeployItem> items = new ArrayList<>();
        for (DeploymentModel deploymentModel : configuration.getDeploymentSettings().getDeploymentModels()) {
            DeployItem item = new DeployItem(myCommonModel, deploymentModel.getDeploymentSource(), myBundleType);
            item.resetFrom(deploymentModel);
            items.add(item);
        }

        for (DeployItem oldItem : myItems.replaceAll(items)) {
            Disposer.dispose(oldItem);
        }
    }

    @Override
    protected void applyEditorTo(JavaEEConfigurationImpl configuration) throws ConfigurationException {
        JavaEEDeploymentSettingsImpl deploymentSettings = (JavaEEDeploymentSettingsImpl)configuration.getDeploymentSettings();

        deploymentSettings.removeAll();

        for (DeployItem item : myItems) {
            DeploymentModel deploymentModel = myBundleType.createNewDeploymentModel(myCommonModel, item.getDeploymentSource());
            if (deploymentModel == null) {
                continue;
            }

            item.applyTo(deploymentModel);

            deploymentSettings.addModel(deploymentModel);
        }
    }

    private class DeployItemAddAction extends AddAction<DeployItem> {
        @Override
        @RequiredUIAccess
        protected void doAdd(AnActionEvent e) {
            List<Artifact> artifacts = collectDeployableArtifacts();

            LocalizeValue artifactSource = JakartaLocalize.labelRunConfigurationEditorDeploymentSourceArtifact();
            BaseListPopupStep<LocalizeValue> step =
                new BaseListPopupStep<>(JakartaLocalize.titleRunConfigurationEditorSelectDeploymentSource().get(), artifactSource) {
                    @Override
                    public String getTextFor(LocalizeValue value) {
                        return value.get();
                    }

                    @Override
                    public Image getIconFor(LocalizeValue value) {
                        return PlatformIconGroup.nodesArtifact();
                    }

                    @Override
                    public boolean isSelectable(LocalizeValue value) {
                        return !artifacts.isEmpty();
                    }

                    @Override
                    public PopupStep onChosen(LocalizeValue selectedValue, boolean finalChoice) {
                        return doFinalStep(() -> chooseArtifact(e, artifacts));
                    }
                };

            JBPopupFactory.getInstance().createListPopup(myProject, step).showUnderneathOf(e);
        }
    }

    private class DeployItemRemoveAction extends RemoveAction<DeployItem> {
        @Override
        @RequiredUIAccess
        protected void doRemove(DeployItem item, AnActionEvent e) {
            Artifact artifact = item.getArtifact();
            if (artifact != null) {
                myBuildArtifactsBeforeRunTaskHelper.setBuildArtifactBeforeRunOption(e.getDataContext(), artifact, false);
            }

            if (myShownItem == item) {
                clearItemSettings();
            }

            myItems.remove(item);
            Disposer.dispose(item);
        }
    }
}
