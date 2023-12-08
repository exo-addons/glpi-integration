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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.glpi.model.GLPISettings;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.exoplatform.commons.api.settings.SettingService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GLPIServiceImplTest {

  @Mock
  private SettingService                         settingService;

  @Mock
  private HttpClient                             httpClient;

  private static final MockedStatic<EntityUtils> ENTITY_UTILS                     = mockStatic(EntityUtils.class);

  private GLPIService                            glpiService;

  private static final Context                   GLPI_INTEGRATION_SETTING_CONTEXT = Context.GLOBAL.id("glpi-integration");

  private static final Scope                     GLPI_INTEGRATION_SETTING_SCOPE   = Scope.APPLICATION.id("glpi-integration");

  private static final Context                   GLPI_USER_TOKEN_SETTING_CONTEXT  = Context.USER.id("1");

  @Before
  public void setUp() throws Exception {
    glpiService = new GLPIServiceImpl(settingService);
    Field httpClient = glpiService.getClass().getDeclaredField("httpClient");
    httpClient.setAccessible(true);
    httpClient.set(glpiService, this.httpClient);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    ENTITY_UTILS.close();
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

    GLPISettings glpiSettings = glpiService.saveGLPISettings("url", "token", 10);

    assertNotNull(glpiSettings);
    assertEquals("url", glpiSettings.getServerApiUrl());
    assertEquals("token", glpiSettings.getAppToken());
    assertEquals(10, glpiSettings.getMaxTicketsToDisplay());
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

  @Test
  public void saveUserToken() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveUserToken(null, null));
    assertEquals("GLPI user token is mandatory", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveUserToken("token", null));
    assertEquals("userIdentityId is mandatory", exception.getMessage());
    assertNotNull(this.glpiService.saveUserToken("token", "1"));
    verify(settingService, times(1)).set(any(), any(), anyString(), any());
  }

  @Test
  public void getUserToken() {
    when(settingService.get(GLPI_USER_TOKEN_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIUserToken")).thenReturn(null);
    assertNull(this.glpiService.getUserToken("1"));
    SettingValue userTokenSettingValue = SettingValue.create("token");
    when(settingService.get(GLPI_USER_TOKEN_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIUserToken")).thenReturn(userTokenSettingValue);
    assertNotNull(this.glpiService.getUserToken("1"));
    assertEquals("token", this.glpiService.getUserToken("1"));
  }

  private void mockGetGLPISettingsAndUserToken() {
    SettingValue serverApiUrlSettingValue = SettingValue.create("url");
    SettingValue appTokenSettingValue = SettingValue.create("token");
    SettingValue maxDisplayTicketsSettingValue = SettingValue.create("10");
    SettingValue userTokenSettingValue = SettingValue.create("userToken");

    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            "GLPIIntegrationServerApiUrlSetting")).thenReturn(serverApiUrlSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            "GLPIIntegrationAppTokenSetting")).thenReturn(null, appTokenSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            "GLPIIntegrationMaxDisplayTicketsSetting")).thenReturn(maxDisplayTicketsSettingValue);
    when(settingService.get(GLPI_USER_TOKEN_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            "GLPIUserToken")).thenReturn(null, userTokenSettingValue);
  }
  
  @Test
  public void initSession() throws Exception {

    mockGetGLPISettingsAndUserToken();

    Method initSession = glpiService.getClass().getDeclaredMethod("initSession", String.class);
    initSession.setAccessible(true);
    String sessionToken = (String) initSession.invoke(glpiService, "1");
    assertNull(sessionToken);

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    ENTITY_UTILS.when(() -> EntityUtils.toString(httpEntity)).thenReturn("{session_token: abc}");
    when(this.httpClient.execute(any())).thenReturn(httpResponse);

    sessionToken = (String) initSession.invoke(glpiService, "1");
    assertEquals("abc", sessionToken);

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertNull(initSession.invoke(glpiService, "1"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertNull(initSession.invoke(glpiService, "1"));
  }

  @Test
  public void killSession() throws Exception {

    mockGetGLPISettingsAndUserToken();

    Method killSession = glpiService.getClass().getDeclaredMethod("killSession", String.class);
    killSession.setAccessible(true);
    int statusCode = (Integer) killSession.invoke(glpiService, "abc");
    assertEquals(400, statusCode);

    HttpResponse httpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(this.httpClient.execute(any())).thenReturn(httpResponse);

    statusCode = (Integer) killSession.invoke(glpiService, "1");
    assertEquals(204, statusCode);

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertEquals(401, killSession.invoke(glpiService, "1"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertEquals(500, killSession.invoke(glpiService, "1"));
  }
}
