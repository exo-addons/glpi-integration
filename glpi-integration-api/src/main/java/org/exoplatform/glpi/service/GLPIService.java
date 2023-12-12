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
}
