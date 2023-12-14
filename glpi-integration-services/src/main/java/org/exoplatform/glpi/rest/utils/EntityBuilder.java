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
import org.exoplatform.glpi.model.GlpiTicket;
import org.exoplatform.glpi.model.GlpiUser;
import org.exoplatform.glpi.rest.model.GLPISettingsEntity;
import org.exoplatform.glpi.rest.model.GLPISettingsResponseEntity;
import org.exoplatform.glpi.rest.model.GlpiTicketEntity;
import org.exoplatform.glpi.rest.model.GlpiUserEntity;
import org.exoplatform.glpi.service.GLPIService;
import org.exoplatform.services.security.Identity;

import java.util.List;

public class EntityBuilder {

  private static final String ADMINISTRATORS_GROUP = "/platform/administrators";

  private EntityBuilder() {
  }

  public static GLPISettingsEntity toGLPISettingsEntity(GLPISettings glpiSettings, boolean isAdmin) {
    if (glpiSettings == null) {
      return null;
    }
    String appToken = isAdmin ? glpiSettings.getAppToken(): null;
    return new GLPISettingsEntity(glpiSettings.getServerApiUrl(),
                                  appToken,
                                  glpiSettings.getMaxTicketsToDisplay());
  }

  public static GLPISettingsResponseEntity toGLPISettingsResponseEntity(GLPISettings glpiSettings, Identity identity, GLPIService glpiService) {
    boolean isAdmin = identity.isMemberOf(ADMINISTRATORS_GROUP);
    return new GLPISettingsResponseEntity(toGLPISettingsEntity(glpiSettings, isAdmin),
                                          glpiService.isUserTokenValid(glpiService.getUserToken(identity.getUserId())),
                                          isAdmin);
  }
  
  public static GlpiUserEntity toGLPIUserEntity(GlpiUser glpiUser) {
    if (glpiUser == null) {
      return null;
    }
    return new GlpiUserEntity(glpiUser.getId(), glpiUser.getName(), glpiUser.getFirstName(), glpiUser.getLastName());
  }
  
  public static GlpiTicketEntity toGLPITicketEntity(GlpiTicket glpiTicket) {
    if (glpiTicket == null) {
      return null;
    }
    return new GlpiTicketEntity(glpiTicket.getId(),
                                glpiTicket.getTitle(),
                                glpiTicket.getContent(),
                                glpiTicket.getStatus(),
                                toGLPIUserEntity(glpiTicket.getCreator()),
                                glpiTicket.getComments(),
                                glpiTicket.getSolveDate(),
                                glpiTicket.getLastUpdated());
  }
  
  public static List<GlpiTicketEntity> toGLPITicketListEntity(List<GlpiTicket> glpiTickets) {
    return glpiTickets.stream().map(EntityBuilder::toGLPITicketEntity).toList();
  }
}
