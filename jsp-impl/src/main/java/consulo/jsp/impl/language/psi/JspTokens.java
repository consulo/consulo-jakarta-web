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

package consulo.jsp.impl.language.psi;

import consulo.jsp.language.JspLanguage;
import consulo.jsp.language.ast.JspTokenType;
import consulo.language.ast.IElementType;
import consulo.language.ast.StandardTokenTypes;

/**
 * @author VISTALL
 * @since 08.11.13.
 */
public interface JspTokens extends JspTokenType, StandardTokenTypes {
    IElementType HTML_FRAGMENT = new IElementType("HTML_FRAGMENT", JspLanguage.INSTANCE);
}
