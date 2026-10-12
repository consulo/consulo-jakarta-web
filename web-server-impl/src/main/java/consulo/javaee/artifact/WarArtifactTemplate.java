/*
 * Copyright 2013 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.javaee.artifact;

import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactManager;
import consulo.compiler.artifact.ArtifactPointerManager;
import consulo.compiler.artifact.ArtifactTemplate;
import consulo.compiler.artifact.ArtifactUtil;
import consulo.compiler.artifact.element.ArtifactElementType;
import consulo.compiler.artifact.element.ArtifactPackagingElement;
import consulo.compiler.artifact.element.CompositePackagingElement;
import consulo.compiler.artifact.element.PackagingElementFactory;
import consulo.compiler.artifact.element.PackagingElementResolvingContext;
import consulo.jakartaee.web.module.extension.JavaWebModuleExtension;
import consulo.jakartaee.webServer.impl.localize.JakartaWebServerLocalize;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.layer.ModuleRootModel;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.ChooseItemsDialogDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 07.11.13.
 */
public class WarArtifactTemplate extends ArtifactTemplate {
    private final PackagingElementResolvingContext myContext;

    public WarArtifactTemplate(PackagingElementResolvingContext context) {
        myContext = context;
    }

    public static NewArtifactConfiguration doCreateArtifactTemplate(Artifact artifact, PackagingElementResolvingContext context) {
        Set<Module> modulesIncludedInArtifacts = ArtifactUtil.getModulesIncludedInArtifacts(List.of(artifact), context.getProject());

        String moduleName = artifact.getName();

        for (Module modulesIncludedInArtifact : modulesIncludedInArtifacts) {
            ModuleRootModel rootModel = context.getModulesProvider().getRootModel(modulesIncludedInArtifact);

            JavaWebModuleExtension extension = rootModel.getExtension(JavaWebModuleExtension.class);
            if (extension != null) {
                moduleName = modulesIncludedInArtifact.getName();
                break;
            }
        }

        PackagingElementFactory elementFactory = PackagingElementFactory.getInstance(context.getProject());

        CompositePackagingElement<?> root = WarArtifactType.getInstance().createRootElement(elementFactory, moduleName);

        ArtifactPackagingElement artifactPackagingElement = ArtifactElementType.getInstance().createEmpty(context.getProject());
        artifactPackagingElement.setArtifactPointer(ArtifactPointerManager.getInstance(context.getProject()).create(artifact));

        root.addFirstChild(artifactPackagingElement);

        return new NewArtifactConfiguration(
            root,
            WarArtifactType.getInstance().getPresentableName() + ": " + moduleName,
            WarArtifactType.getInstance()
        );
    }

    @Override
    @RequiredUIAccess
    public CompletableFuture<NewArtifactConfiguration> createArtifact() {
        List<Artifact> artifacts = new ArrayList<>();
        for (Artifact artifact : ArtifactManager.getInstance(myContext.getProject()).getArtifacts()) {
            if (artifact.getArtifactType() == ExplodedWarArtifactType.getInstance()) {
                artifacts.add(artifact);
            }
        }

        return ChooseItemsDialogDescriptor.showAsync(
            null,
            JakartaWebServerLocalize.artifactTemplateFromExplodedChooseTitle(),
            JakartaWebServerLocalize.artifactTemplateFromExplodedChooseDescription(),
            artifacts,
            (artifact, presentation) -> {
                presentation.withIcon(artifact.getArtifactType().getIcon());
                presentation.append(artifact.getName());
            },
            false,
            List.of()
        ).thenApply(selected -> {
            if (selected.size() != 1) {
                throw new CancellationException();
            }
            return doCreateArtifactTemplate(selected.get(0), myContext);
        });
    }

    @Override
    public LocalizeValue getPresentableName() {
        return JakartaWebServerLocalize.artifactTemplateFromExplodedName();
    }
}
