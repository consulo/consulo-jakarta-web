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

import consulo.compiler.artifact.ArtifactType;
import consulo.compiler.artifact.element.*;
import consulo.component.util.pointer.NamedPointer;
import consulo.content.ContentFolderTypeProvider;
import consulo.language.content.LanguageContentFolderScopes;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;

/**
 * @author VISTALL
 * @since 2013-11-07
 */
public class WebResourceModuleOutputPackagingElement extends ModuleOutputPackagingElementImpl {
    public WebResourceModuleOutputPackagingElement(
        PackagingElementType type,
        Project project,
        NamedPointer<Module> modulePointer,
        ContentFolderTypeProvider contentFolderType
    ) {
        super(type, project, modulePointer, contentFolderType);
    }

    public WebResourceModuleOutputPackagingElement(
        PackagingElementType type,
        Project project,
        ContentFolderTypeProvider contentFolderType
    ) {
        super(type, project, contentFolderType);
    }

    @Override
    public void computeIncrementalCompilerInstructions(
        IncrementalCompilerInstructionCreator creator,
        PackagingElementResolvingContext resolvingContext,
        ArtifactIncrementalCompilerContext compilerContext,
        ArtifactType artifactType
    ) {
        Module module = findModule(resolvingContext);
        if (module != null) {
            VirtualFile[] virtualFiles =
                ModuleRootManager.getInstance(module).getContentFolderFiles(LanguageContentFolderScopes.of(myContentFolderType));

            for (VirtualFile virtualFile : virtualFiles) {
                creator.addDirectoryCopyInstructions(virtualFile.toNioPath(), null);
            }
        }
    }
}
