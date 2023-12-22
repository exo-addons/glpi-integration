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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

  private static final Context                   GLPI_USER_TOKEN_SETTING_CONTEXT  = Context.USER.id("user");

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
  public void saveUserToken() throws IOException {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveUserToken(null, null));
    assertEquals("GLPI user token is mandatory", exception.getMessage());
    exception = assertThrows(IllegalArgumentException.class, () -> this.glpiService.saveUserToken("token", null));
    assertEquals("userIdentityId is mandatory", exception.getMessage());
    assertNotNull(this.glpiService.saveUserToken("token", "user"));
    verify(settingService, times(1)).set(any(), any(), anyString(), any());
  }

  @Test
  public void getUserToken() {
    when(settingService.get(GLPI_USER_TOKEN_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIUserToken")).thenReturn(null);
    assertNull(this.glpiService.getUserToken("user"));
    SettingValue userTokenSettingValue = SettingValue.create("token");
    when(settingService.get(GLPI_USER_TOKEN_SETTING_CONTEXT, GLPI_INTEGRATION_SETTING_SCOPE, "GLPIUserToken")).thenReturn(userTokenSettingValue);
    assertNotNull(this.glpiService.getUserToken("user"));
    assertEquals("token", this.glpiService.getUserToken("user"));
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
                            "GLPIIntegrationAppTokenSetting")).thenReturn(appTokenSettingValue);
    when(settingService.get(GLPI_INTEGRATION_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            "GLPIIntegrationMaxDisplayTicketsSetting")).thenReturn(maxDisplayTicketsSettingValue);
    when(settingService.get(GLPI_USER_TOKEN_SETTING_CONTEXT,
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            "GLPIUserToken")).thenReturn(userTokenSettingValue);
  }
  
  private void mockGetValidSessionTokenResponse(HttpResponse httpResponse) throws IOException {
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    ENTITY_UTILS.when(() -> EntityUtils.toString(httpEntity)).thenReturn("{session_token: abc}");
  }

  @Test
  public void initSession() throws Exception {
    Method initSession = glpiService.getClass().getDeclaredMethod("initSession", String.class);
    initSession.setAccessible(true);
    String sessionToken = (String) initSession.invoke(glpiService, "abc");
    assertNull(sessionToken);

    mockGetGLPISettingsAndUserToken();

    HttpResponse httpResponse = mock(HttpResponse.class);
    mockGetValidSessionTokenResponse(httpResponse);
    when(this.httpClient.execute(any())).thenReturn(httpResponse);

    sessionToken = (String) initSession.invoke(glpiService, "abc");
    assertEquals("abc", sessionToken);

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertNull(initSession.invoke(glpiService, "user"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertNull(initSession.invoke(glpiService, "user"));
  }

  private void mockGetValidKillSessionResponse(HttpResponse httpResponse) throws IOException {
    StatusLine statusLine = mock(StatusLine.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
  }
  
  @Test
  public void killSession() throws Exception {


    Method killSession = glpiService.getClass().getDeclaredMethod("killSession", String.class);
    killSession.setAccessible(true);
    int statusCode = (Integer) killSession.invoke(glpiService, "abc");
    assertEquals(400, statusCode);

    mockGetGLPISettingsAndUserToken();

    HttpResponse httpResponse = mock(HttpResponse.class);
    mockGetValidKillSessionResponse(httpResponse);
    when(this.httpClient.execute(any())).thenReturn(httpResponse);

    statusCode = (Integer) killSession.invoke(glpiService, "user");
    assertEquals(204, statusCode);

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertEquals(401, killSession.invoke(glpiService, "user"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertEquals(500, killSession.invoke(glpiService, "user"));
  }

  @Test
  public void isUserTokenValid() throws IOException {
    assertFalse(glpiService.isUserTokenValid(null));
    assertFalse(glpiService.isUserTokenValid("token"));
    mockGetGLPISettingsAndUserToken();

    HttpResponse initSessionResponse = mock(HttpResponse.class);
    mockGetValidSessionTokenResponse(initSessionResponse);

    HttpResponse killSessionResponse = mock(HttpResponse.class);
    mockGetValidKillSessionResponse(initSessionResponse);

    when(this.httpClient.execute(any())).thenReturn(initSessionResponse, killSessionResponse);

    assertTrue(glpiService.isUserTokenValid("token"));
  }

  @Test
  public void getGLPIUserInfo() throws Exception {
    Method getGLPIUserInfo = glpiService.getClass().getDeclaredMethod("getGLPIUserInfo", long.class, String.class);
    getGLPIUserInfo.setAccessible(true);
    assertNull(getGLPIUserInfo.invoke(glpiService, 1L, "abc"));
    mockGetGLPISettingsAndUserToken();

    HttpResponse userInfoHttpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(userInfoHttpResponse.getEntity()).thenReturn(httpEntity);
    when(userInfoHttpResponse.getStatusLine()).thenReturn(statusLine);
    ENTITY_UTILS.when(() -> EntityUtils.toString(httpEntity)).thenReturn("{id: 1, name: user, firstname: first, realname:last}");

    when(this.httpClient.execute(any())).thenReturn(userInfoHttpResponse);
    assertNotNull(getGLPIUserInfo.invoke(glpiService, 1L, "abc"));

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertNull(getGLPIUserInfo.invoke(glpiService, 1L, "abc"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertNull(getGLPIUserInfo.invoke(glpiService, 1L, "abc"));
  }

  @Test
  public void getGLPITickets() throws Exception {
    assertNull(glpiService.getGLPITickets(0, 3, null));
    mockGetGLPISettingsAndUserToken();

    HttpResponse initSessionResponse = mock(HttpResponse.class);
    mockGetValidSessionTokenResponse(initSessionResponse);

    HttpResponse killSessionResponse = mock(HttpResponse.class);
    mockGetValidKillSessionResponse(initSessionResponse);

    HttpResponse listTicketsHttpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(listTicketsHttpResponse.getEntity()).thenReturn(httpEntity);
    when(listTicketsHttpResponse.getStatusLine()).thenReturn(statusLine);
    ENTITY_UTILS.when(() -> EntityUtils.toString(httpEntity))
                .thenReturn("{data: [{\"21\":\"content\","
                    + "\"17\":null,\"19\":\"2023-12-12 09:46:54\","
                    + "\"12\":1,\"2\":3,\"25\":null,"
                    + "\"1\":\"title\",\"5\":[\"8\",\"7\"]}]}");

    assertNull(glpiService.getGLPITickets(0, 3, "user"));

    when(this.httpClient.execute(any())).thenReturn(initSessionResponse, listTicketsHttpResponse, killSessionResponse);
    assertNotNull(glpiService.getGLPITickets(0, 3, "user"));

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertNull(glpiService.getGLPITickets(0, 3, "user"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertNull(glpiService.getGLPITickets(0, 3, "user"));
  }

  @Test
  public void readTicketImageDocument() throws Exception {
    assertNull(glpiService.readTicketImageDocument(1, "root"));
    mockGetGLPISettingsAndUserToken();

    HttpResponse initSessionResponse = mock(HttpResponse.class);
    mockGetValidSessionTokenResponse(initSessionResponse);

    HttpResponse killSessionResponse = mock(HttpResponse.class);
    mockGetValidKillSessionResponse(initSessionResponse);

    HttpResponse readImageHttpResponse = mock(HttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    HttpEntity httpEntity = mock(HttpEntity.class);
    when(statusLine.getStatusCode()).thenReturn(204);
    when(readImageHttpResponse.getEntity()).thenReturn(httpEntity);
    when(readImageHttpResponse.getStatusLine()).thenReturn(statusLine);
    InputStream inputStream = mock(InputStream.class);
    when(httpEntity.getContent()).thenReturn(inputStream);
    when(inputStream.readAllBytes()).thenReturn("test".getBytes());

    assertNull(glpiService.readTicketImageDocument(1L, "user"));

    when(this.httpClient.execute(any())).thenReturn(initSessionResponse, readImageHttpResponse, killSessionResponse);
    assertNotNull(glpiService.readTicketImageDocument(1L, "user"));

    doThrow(new HttpResponseException(401, "unauthorized")).when(this.httpClient).execute(any());
    assertNull(glpiService.readTicketImageDocument(1L, "user"));

    doThrow(new RuntimeException()).when(this.httpClient).execute(any());
    assertNull(glpiService.readTicketImageDocument(1L, "user"));
  }

  @Test
  public void removeUserToken() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> glpiService.removeUserToken(null));
    assertEquals("user identity id is mandatory", exception.getMessage());
    glpiService.removeUserToken("user");
    verify(settingService, times(1)).remove(any(), any(), anyString());
  }

}
