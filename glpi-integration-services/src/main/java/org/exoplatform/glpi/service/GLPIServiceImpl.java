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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.glpi.model.GLPISettings;

import org.apache.http.client.HttpClient;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URI;

public class GLPIServiceImpl implements GLPIService {

  private static final Log     LOG                                              = ExoLogger.getLogger(GLPIService.class);

  private final SettingService settingService;

  private static final Context GLPI_INTEGRATION_SETTING_CONTEXT                 = Context.GLOBAL.id("glpi-integration");

  private static final Scope   GLPI_INTEGRATION_SETTING_SCOPE                   = Scope.APPLICATION.id("glpi-integration");

  private static final String  GLPI_INTEGRATION_SERVER_API_URL_SETTING_KEY      = "GLPIIntegrationServerApiUrlSetting";

  private static final String  GLPI_INTEGRATION_APP_TOKEN_SETTING_KEY           = "GLPIIntegrationAppTokenSetting";

  private static final String  GLPI_INTEGRATION_MAX_DISPLAY_TICKETS_SETTING_KEY = "GLPIIntegrationMaxDisplayTicketsSetting";

  private static final String  GLPI_USER_TOKEN_SETTING_KEY                      = "GLPIUserToken";

  private final HttpClient     httpClient;

  private static final String  GLPI_SERVICE_API                                 = "glpi-service-api";

  private static final int     DEFAULT_POOL_CONNECTION                          = 100;

  public GLPIServiceImpl(SettingService settingsService) {
    this.settingService = settingsService;
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(DEFAULT_POOL_CONNECTION);
    HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                     .setConnectionManager(connectionManager)
                                                     .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy())
                                                     .setMaxConnPerRoute(DEFAULT_POOL_CONNECTION);
    this.httpClient = httpClientBuilder.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GLPISettings saveGLPISettings(String serverApiUrl, String appToken, int maxTicketToDisplay) {
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
    return new GLPISettings(serverApiUrl, appToken, maxTicketToDisplay);
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

  /**
   * {@inheritDoc}
   */
  @Override
  public String saveUserToken(String userToken, String userIdentityId) {
    if (StringUtils.isBlank(userToken)) {
      throw new IllegalArgumentException("GLPI user token is mandatory");
    }
    if (StringUtils.isBlank(userIdentityId)) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }
    this.settingService.set(Context.USER.id(userIdentityId),
                            GLPI_INTEGRATION_SETTING_SCOPE,
                            GLPI_USER_TOKEN_SETTING_KEY,
                            SettingValue.create(userToken));
    return userToken;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUserToken(String userIdentityId) {
    SettingValue<?> userTokenSettingValue = this.settingService.get(Context.USER.id(userIdentityId),
                                                                    GLPI_INTEGRATION_SETTING_SCOPE,
                                                                    GLPI_USER_TOKEN_SETTING_KEY);

    if (userTokenSettingValue == null) {
      return null;
    }
    return userTokenSettingValue.getValue().toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isUserTokenValid(String userToken) {
    if (userToken == null) {
      return false;
    }
    String sessionToken = initSession(userToken);
    if (sessionToken != null) {
      killSession(sessionToken);
      return true;
    }
    return false;
  }
  
  private String initSession(String userToken) {
    long startTime = System.currentTimeMillis();
    GLPISettings glpiSettings = getGLPISettings();
    if (glpiSettings == null) {
      return null;
    }
    try {
      HttpGet httpTypeRequest = new HttpGet(glpiSettings.getServerApiUrl() + "/initSession");
      httpTypeRequest.setHeader("Authorization", "user_token " + userToken);
      httpTypeRequest.setHeader("App-Token", glpiSettings.getAppToken());
      URI uri = new URIBuilder(httpTypeRequest.getURI()).addParameter("get_full_session", "true").build();
      httpTypeRequest.setURI(uri);
      HttpResponse httpResponse = httpClient.execute(httpTypeRequest);
      String responseString = new BasicResponseHandler().handleResponse(httpResponse);
      JSONObject jsonResponse = new JSONObject(responseString);
      return jsonResponse.getString("session_token");
    } catch (HttpResponseException e) {
      LOG.error("remote_service={} operation={} parameters=\"user token:{}, status=ko "
          + "duration_ms={} error_msg=\"{}, status : {} \"",
                GLPI_SERVICE_API,
                "GLPI initSession",
                userToken,
                System.currentTimeMillis() - startTime,
                e.getReasonPhrase(),
                e.getStatusCode());
    } catch (Exception e) {
      LOG.error("Error while init GLPI session", e);
    }
    return null;
  }

  private int killSession(String sessionToken) {
    long startTime = System.currentTimeMillis();
    GLPISettings glpiSettings = getGLPISettings();
    if (glpiSettings == null) {
      return HttpURLConnection.HTTP_BAD_REQUEST;
    }
    try {
      HttpGet httpTypeRequest = new HttpGet(glpiSettings.getServerApiUrl() + "/killSession");
      httpTypeRequest.setHeader("Session-Token", sessionToken);
      httpTypeRequest.setHeader("App-Token", glpiSettings.getAppToken());
      HttpResponse httpResponse = httpClient.execute(httpTypeRequest);
      return httpResponse.getStatusLine().getStatusCode();
    } catch (HttpResponseException e) {
      LOG.error("remote_service={} operation={} parameters=\"session token:{}, status=ko "
          + "duration_ms={} error_msg=\"{}, status : {} \"",
                GLPI_SERVICE_API,
                "GLPI killSession",
                sessionToken,
                System.currentTimeMillis() - startTime,
                e.getReasonPhrase(),
                e.getStatusCode());
      return e.getStatusCode();
    } catch (Exception e) {
      LOG.error("while destroying GLPI session", e);
      return HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
  }
}
