/*
 * Copyright (C) 2023 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.glpi.rest.utils;

import org.exoplatform.glpi.model.GLPISettings;
import org.exoplatform.glpi.rest.model.GLPISettingsEntity;
import org.exoplatform.glpi.rest.model.GLPISettingsResponseEntity;
import org.exoplatform.services.security.Identity;

public class EntityBuilder {

  private static final String ADMINISTRATORS_GROUP = "/platform/administrators";

  private EntityBuilder() {
  }

  public static GLPISettingsEntity toGLPISettingsEntity(GLPISettings glpiSettings) {
    if (glpiSettings == null) {
      return null;
    }
    return new GLPISettingsEntity(glpiSettings.getServerApiUrl(),
                                  glpiSettings.getAppToken(),
                                  glpiSettings.getMaxTicketsToDisplay());
  }

  public static Object toGLPISettingsResponseEntity(GLPISettings glpiSettings, Identity identity) {
    return new GLPISettingsResponseEntity(toGLPISettingsEntity(glpiSettings), identity.isMemberOf(ADMINISTRATORS_GROUP));
  }
}