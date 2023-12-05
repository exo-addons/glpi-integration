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

import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.glpi.model.GLPISettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.exoplatform.commons.api.settings.SettingService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GLPIServiceImplTest {

  @Mock
  private SettingService       settingService;

  private GLPIService          glpiService;

  private static final Context GLPI_INTEGRATION_SETTING_CONTEXT = Context.GLOBAL.id("glpi-integration");

  private static final Scope   GLPI_INTEGRATION_SETTING_SCOPE   = Scope.APPLICATION.id("glpi-integration");

  private static final String  GLPI_INTEGRATION_SETTING_KEY     = "GLPIIntegrationSettings";

  @Before
  public void setUp() throws Exception {
    glpiService = new GLPIServiceImpl(settingService);
  }

  @Test
  public void saveGLPISettings() {
    Throwable exception =
                        assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveGLPISettings(null, "token", 10));
    assertEquals("GLPI serverApiUrl is mandatory", exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveGLPISettings("url", null, 10));
    assertEquals("GLPI app token is mandatory", exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveGLPISettings("url", "token", 0));
    assertEquals("GLPI max Tickets to display should be between 1 and 10", exception.getMessage());

    glpiService.saveGLPISettings("url", "token", 10);
    verify(settingService, times(3)).set(any(), any(), anyString(), any());
  }

  @Test
  public void getGLPISettings() {
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationServerApiUrlSetting")).thenReturn(null);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationAppTokenSetting")).thenReturn(null);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationMaxDisplayTicketsSetting")).thenReturn(null);

    assertNull(glpiService.getGLPISettings());
    SettingValue serverApiUrlSettingValue = SettingValue.create("url");
    
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationServerApiUrlSetting")).thenReturn(serverApiUrlSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationAppTokenSetting")).thenReturn(null);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationMaxDisplayTicketsSetting")).thenReturn(null);

    assertNull(glpiService.getGLPISettings());
    SettingValue appTokenSettingValue = SettingValue.create("token");

    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationServerApiUrlSetting")).thenReturn(serverApiUrlSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationAppTokenSetting")).thenReturn(appTokenSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationMaxDisplayTicketsSetting")).thenReturn(null);

    assertNull(glpiService.getGLPISettings());
    SettingValue maxDisplayTicketsSettingValue = SettingValue.create("10");
    
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationServerApiUrlSetting")).thenReturn(serverApiUrlSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationAppTokenSetting")).thenReturn(appTokenSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIIntegrationMaxDisplayTicketsSetting")).thenReturn(maxDisplayTicketsSettingValue);

    GLPISettings glpiSettings = glpiService.getGLPISettings(); 
    assertNotNull(glpiSettings);
    assertEquals("url", glpiSettings.getServerApiUrl());
    assertEquals("token", glpiSettings.getAppToken());
    assertEquals(10, glpiSettings.getMaxTicketsToDisplay());
  }
}
