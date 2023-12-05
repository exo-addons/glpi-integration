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

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.glpi.model.GLPISettings;

public class GLPIServiceImpl implements GLPIService {

  private final SettingService settingService;

  private static final Context GLPI_INTEGRATION_SETTING_CONTEXT                 = Context.GLOBAL.id("glpi-integration");

  private static final Scope   GLPI_INTEGRATION_SETTING_SCOPE                   = Scope.APPLICATION.id("glpi-integration");

  private static final String  GLPI_INTEGRATION_SERVER_API_URL_SETTING_KEY      = "GLPIIntegrationServerApiUrlSetting";

  private static final String  GLPI_INTEGRATION_APP_TOKEN_SETTING_KEY           = "GLPIIntegrationAppTokenSetting";

  private static final String  GLPI_INTEGRATION_MAX_DISPLAY_TICKETS_SETTING_KEY = "GLPIIntegrationMaxDisplayTicketsSetting";

  public GLPIServiceImpl(SettingService settingsService) {
    this.settingService = settingsService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveGLPISettings(String serverApiUrl, String appToken, int maxTicketToDisplay) {
    if (StringUtils.isBlank(serverApiUrl)) {
      throw new IllegalArgumentException("GLPI serverApiUrl is mandatory");
    }
    if (StringUtils.isBlank(appToken)) {
      throw new IllegalArgumentException("GLPI app token is mandatory");
    }
    if (maxTicketToDisplay < 1 || maxTicketToDisplay > 10) {
      throw new IllegalArgumentException("GLPI max Tickets to display should be between 1 and 10");
    }
    this.settingService.set(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            GLPI_INTEGRATION_SERVER_API_URL_SETTING_KEY,
                            SettingValue.create(serverApiUrl));
    this.settingService.set(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            GLPI_INTEGRATION_APP_TOKEN_SETTING_KEY,
                            SettingValue.create(appToken));
    this.settingService.set(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            GLPI_INTEGRATION_MAX_DISPLAY_TICKETS_SETTING_KEY,
                            SettingValue.create(String.valueOf(maxTicketToDisplay)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GLPISettings getGLPISettings() {
    SettingValue<?> ServerApiUrlSettingValue = settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                                                                  GLPI_INTEGRATION_SETTING_SCOPE,
                                                                  GLPI_INTEGRATION_SERVER_API_URL_SETTING_KEY);
    SettingValue<?> AppTokenSettingValue = settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                                                              GLPI_INTEGRATION_SETTING_SCOPE,
                                                              GLPI_INTEGRATION_APP_TOKEN_SETTING_KEY);
    SettingValue<?> maxDisplayTicketsSettingValue = settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                                                                       GLPI_INTEGRATION_SETTING_SCOPE,
                                                                       GLPI_INTEGRATION_MAX_DISPLAY_TICKETS_SETTING_KEY);
    if (ServerApiUrlSettingValue == null || AppTokenSettingValue == null || maxDisplayTicketsSettingValue == null) {
      return null;
    }
    return new GLPISettings(ServerApiUrlSettingValue.getValue().toString(),
                            AppTokenSettingValue.getValue().toString(),
                            Integer.parseInt(maxDisplayTicketsSettingValue.getValue().toString()));
  }

}
