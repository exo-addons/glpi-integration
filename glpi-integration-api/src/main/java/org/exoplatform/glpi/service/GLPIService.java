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

package org.exoplatform.glpi.service;

import org.exoplatform.glpi.model.GLPISettings;
import org.exoplatform.glpi.model.GlpiTicket;

import java.io.InputStream;
import java.util.List;

public interface GLPIService {

  /**
   * Saves GLPI settings
   *
   * @param serverApiUrl       server api url
   * @param appToken           app token generated in api client
   * @param maxTicketToDisplay max tickets to display
   * @return {@link GLPISettings}
   */
  GLPISettings saveGLPISettings(String serverApiUrl, String appToken, int maxTicketToDisplay);

  /**
   * Retrieves saved GLPI settings
   *
   * @return {@link GLPISettings}
   */
  GLPISettings getGLPISettings();


  /**
   * Save user token
   *
   * @param userToken user token
   * @param userIdentityId user identity id
   * @return saved token
   */
  String saveUserToken(String userToken, String userIdentityId);


  /**
   * Retrieves saved user token
   *
   * @param userIdentityId user identity id
   * @return saved token
   */
  String getUserToken(String userIdentityId);

  /**
   * Checks if user token valid
   * 
   * @param userToken user token
   * @return true if token valid or false if else
   */
  boolean isUserTokenValid(String userToken);


  /**
   * Get list of glpi tickets
   *
   * @param offset search offset
   * @param limit search limit
   * @param userIdentityId user identity id
   * @return {@link List} of {@link GlpiTicket}
   */
  List<GlpiTicket> getGLPITickets(int offset, int limit, String userIdentityId);

  /**
   * Read GLPI ticket document image
   *
   * @param imageId image id
   * @param userIdentityId user identity id
   * @return {@link InputStream}
   */
  InputStream readTicketImageDocument(long imageId, String userIdentityId);

  /**
   * Removes saved user GLPI token
   *
   * @param userIdentityId user identity id
   */
  void removeUserToken(String userIdentityId);
}
