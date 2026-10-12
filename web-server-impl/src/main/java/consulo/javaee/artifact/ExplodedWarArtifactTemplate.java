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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.compiler.artifact.ArtifactTemplate;
import consulo.compiler.artifact.element.*;
import consulo.component.util.pointer.NamedPointer;
import consulo.content.base.WebResourcesFolderTypeProvider;
import consulo.content.library.Library;
import consulo.content.library.LibraryTable;
import consulo.jakartaee.web.JavaWebConstants;
import consulo.jakartaee.web.module.extension.JavaWebModuleExtension;
import consulo.jakartaee.webServer.impl.localize.JakartaWebServerLocalize;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.content.ProductionResourceContentFolderTypeProvider;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.layer.ModuleRootModel;
import consulo.module.content.layer.ModulesProvider;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.ModuleOrderEntry;
import consulo.module.content.layer.orderEntry.RootPolicy;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.ChooseItemsDialogDescriptor;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * @author VISTALL
 * @since 2013-11-07
 */
public class ExplodedWarArtifactTemplate extends ArtifactTemplate {
    private final PackagingElementResolvingContext myContext;

    public ExplodedWarArtifactTemplate(PackagingElementResolvingContext context) {
        myContext = context;
    }

    @Nonnull
    @RequiredReadAction
    public static NewArtifactConfiguration doCreateArtifactTemplate(
        Module module,
        PackagingElementResolvingContext packagingElementResolvingContext
    ) {
        ModulesProvider modulesProvider = packagingElementResolvingContext.getModulesProvider();

        Project project = module.getProject();
        PackagingElementFactory elementFactory = PackagingElementFactory.getInstance(project);
        CompositePackagingElement<?> root = ExplodedWarArtifactType.getInstance().createRootElement(elementFactory, module.getName());

        DirectoryPackagingElement webInfDir = DirectoryElementType.getInstance().createEmpty(project);
        webInfDir.setDirectoryName(JavaWebConstants.WEB_INF);
        root.addFirstChild(webInfDir);

        DirectoryPackagingElement libDir = DirectoryElementType.getInstance().createEmpty(project);
        libDir.setDirectoryName("lib");
        webInfDir.addFirstChild(libDir);

        Set<Pair<Library, Module>> libraries = new LinkedHashSet<>();
        Set<Module> modules = new LinkedHashSet<>();

        collectInfo(modules, libraries, modulesProvider, module);

        for (Module toAddModule : modules) {
            NamedPointer<Module> pointer = ModuleUtilCore.createPointer(toAddModule);
            ModuleRootModel rootModel = modulesProvider.getRootModel(toAddModule);

            ZipArchivePackagingElement zipArchivePackagingElement = ZipArchiveElementType.getInstance().createEmpty(project);
            zipArchivePackagingElement.setArchiveFileName(toAddModule.getName() + ".jar");

            if (rootModel.getContentFolders(LanguageContentFolderScopes.of(ProductionContentFolderTypeProvider.getInstance())).length > 0) {
                zipArchivePackagingElement.addFirstChild(ProductionModuleOutputElementType.getInstance().createElement(project, pointer));
            }

            if (rootModel.getContentFolders(LanguageContentFolderScopes.of(ProductionResourceContentFolderTypeProvider.getInstance())).length > 0) {
                zipArchivePackagingElement.addFirstChild(ProductionResourceModuleOutputElementType.getInstance()
                    .createElement(project, pointer));
            }

            libDir.addFirstChild(zipArchivePackagingElement);

            if (rootModel.getContentFolders(LanguageContentFolderScopes.of(WebResourcesFolderTypeProvider.getInstance())).length > 0) {
                root.addFirstChild(WebResourceModuleOutputElementType.getInstance().createElement(project, pointer));
            }
        }

        for (Pair<Library, Module> pair : libraries) {
            Library library = pair.getKey();
            Module librayModule = pair.getValue();

            LibraryPackagingElement libraryPackagingElement = LibraryElementType.getInstance().createEmpty(project);
            libraryPackagingElement.setLibraryName(library.getName());

            LibraryTable table = library.getTable();

            String tableLevel = table == null ? null : table.getTableLevel();
            libraryPackagingElement.setLevel(tableLevel);

            if (librayModule != null) {
                libraryPackagingElement.setModuleName(librayModule.getName());
            }

            libDir.addFirstChild(libraryPackagingElement);
        }

        return new NewArtifactConfiguration(
            root,
            ExplodedWarArtifactType.getInstance().getPresentableName() + ": " + module.getName(),
            ExplodedWarArtifactType.getInstance()
        );
    }

    private static void collectInfo(
        Set<Module> modules,
        Set<Pair<Library, Module>> libraries,
        ModulesProvider modulesProvider,
        Module module
    ) {
        modules.add(module);

        ModuleRootModel rootModel = modulesProvider.getRootModel(module);

        rootModel.orderEntries().withoutSdk().runtimeOnly().process(
            new RootPolicy<>() {
                @Override
                public Object visitLibraryOrderEntry(LibraryOrderEntry libraryOrderEntry, Object value) {
                    Library library = libraryOrderEntry.getLibrary();
                    if (library == null) {
                        return null;
                    }
                    if (libraryOrderEntry.isModuleLevel()) {
                        libraries.add(Pair.create(library, libraryOrderEntry.getOwnerModule()));
                    }
                    else {
                        libraries.add(Pair.create(library, null));
                    }
                    return null;
                }

                @Override
                public Object visitModuleOrderEntry(ModuleOrderEntry moduleOrderEntry, Object value) {
                    Module moduleDependency = moduleOrderEntry.getModule();
                    if (moduleDependency == null) {
                        return null;
                    }

                    if (!modules.contains(moduleDependency)) {
                        collectInfo(modules, libraries, modulesProvider, moduleDependency);
                    }
                    return null;
                }
            },
            null
        );
    }

    @Override
    @RequiredUIAccess
    public CompletableFuture<NewArtifactConfiguration> createArtifact() {
        List<Module> modules = new ArrayList<>();
        for (Module module : myContext.getModulesProvider().getModules()) {
            if (ModuleUtilCore.getExtension(module, JavaWebModuleExtension.class) != null) {
                modules.add(module);
            }
        }

        return ChooseItemsDialogDescriptor.showAsync(
            null,
            JakartaWebServerLocalize.artifactTemplateFromModuleChooseTitle(),
            JakartaWebServerLocalize.artifactTemplateFromModuleChooseDescription(),
            modules,
            (module, presentation) -> {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(module.getName());
            },
            false,
            List.of()
        ).thenApply(selected -> {
            if (selected.size() != 1) {
                throw new CancellationException();
            }
            return ReadAction.compute(() -> doCreateArtifactTemplate(selected.get(0), myContext));
        });
    }

    @Override
    public LocalizeValue getPresentableName() {
        return JakartaWebServerLocalize.artifactTemplateFromModuleName();
    }
}
