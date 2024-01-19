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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.glpi.model.GLPISettings;
import org.exoplatform.glpi.model.GlpiTicket;
import org.exoplatform.glpi.model.GlpiUser;
import org.exoplatform.glpi.model.TicketStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

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

  private static final String  SEARCH_TICKET_CRITERIA                           =
                                                      "&sort[]=19&order[]=DESC&is_deleted=0&criteria[0][link]=AND"
                                                          + "&criteria[0][field]=4&criteria[0][searchtype]=equals&criteria[0][value]=myself&criteria[1][link]=AND&criteria[1][criteria][0][link]=AND%20NOT"
                                                          + "&criteria[1][criteria][0][field]=12&criteria[1][criteria][0][searchtype]=equals&criteria[1][criteria][0][value]=6"
                                                          + "&criteria[1][criteria][2][link]=OR&criteria[1][criteria][2][criteria][0][link]=AND&criteria[1][criteria][2][criteria][0][field]=12"
                                                          + "&criteria[1][criteria][2][criteria][0][searchtype]=equals&criteria[1][criteria][2][criteria][0][value]=6"
                                                          + "&criteria[1][criteria][2][criteria][1][link]=AND&criteria[1][criteria][2][criteria][1][field]=17"
                                                          + "&criteria[1][criteria][2][criteria][1][searchtype]=morethan&_select_criteria[1][criteria][2][criteria][1][value]=-1MONTH"
                                                          + "&criteria[1][criteria][2][criteria][1][value]=-1MONTH&forcedisplay[0]=21&forcedisplay[1]=25&forcedisplay[2]=5";

  private static GLPISettings GLPI_SETTINGS = null;
  
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

    GLPI_SETTINGS = new GLPISettings(serverApiUrl, appToken, maxTicketToDisplay);
    return GLPI_SETTINGS;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GlpiTicket> getGLPITickets(int offset, int limit, String userIdentityId) {
    long startTime = System.currentTimeMillis();
    String sessionToken = initSession(getUserToken(userIdentityId));
    HttpGet httpGet = initHttpGet("/search/Ticket?range=" + offset + "-" + limit + SEARCH_TICKET_CRITERIA, sessionToken);
    if (httpGet == null) {
      return null;
    }
    List<GlpiTicket> tickets = new ArrayList<>();
    try {
      HttpResponse httpResponse = httpClient.execute(httpGet);
      String responseString = new BasicResponseHandler().handleResponse(httpResponse);
      EntityUtils.consume(httpResponse.getEntity());
      JSONObject jsonResponse = new JSONObject(responseString);
      if (!jsonResponse.has("data")) {
        return tickets;
      }
      JSONArray jsonArray = jsonResponse.getJSONArray("data");
      jsonArray.forEach(object -> {
        GlpiTicket ticket = new GlpiTicket();
        JSONObject jsonObject = (JSONObject) object;
        ticket.setId(jsonObject.getLong("2")); // Ticket.id
        ticket.setTitle(jsonObject.getString("1")); // Ticket.name
        ticket.setContent(jsonObject.getString("21")); // Ticket.content
        ticket.setStatus(TicketStatus.values()[jsonObject.getInt("12") - 1]); // Ticket.status
        Object assignees = jsonObject.get("5"); //Ticket.Ticket_User.User.name
        ticket.setAssignees(assignees != null && !assignees.equals(null) ? getGLPIUsersInfo(assignees, sessionToken)
                                                                         : new ArrayList<>());
        Object comments = jsonObject.get("25");
        ticket.setComments(parseComments(comments));
        Object solveDate = jsonObject.get("17"); // Ticket.solvedate
        ticket.setSolveDate(solveDate != null ? String.valueOf(solveDate) : null);
        ticket.setLastUpdated(jsonObject.getString("19")); // Ticket.date_mod
        tickets.add(ticket);
      });
      return tickets;
    } catch (HttpResponseException e) {
      LOG.error("remote_service={} operation={} parameters=\"offset:{}, limit:{}, userIdentityId:{}, status=ko "
          + "duration_ms={} error_msg=\"{}, status : {} \"",
                GLPI_SERVICE_API,
                "getGLPITickets",
                offset,
                limit,
                userIdentityId,
                System.currentTimeMillis() - startTime,
                e.getReasonPhrase(),
                e.getStatusCode());
    } catch (Exception e) {
      LOG.error("Error while getting GLPI Tickets", e);
    } finally {
      killSession(sessionToken);
    }
    return tickets;
  }

  private List<Object> parseComments(Object comments) {
    List<Object> commentList = new ArrayList<>();
    if (comments.equals(null)) {
      return commentList;
    } else if (comments instanceof String) {
      commentList.add(comments);
    } else {
      commentList = ((JSONArray) comments).toList();
    }
    return commentList;
  }

  private GLPISettings getCurrentGLPISettings() {
    if (GLPI_SETTINGS == null) {
      GLPI_SETTINGS = getGLPISettings();
    }
    return GLPI_SETTINGS;
  }

  private List<GlpiUser> getGLPIUsersInfo(Object assignees, String sessionToken) {
    List<GlpiUser> GLPIUsers = new ArrayList<>();
    if (assignees instanceof String) {
      GLPIUsers.add(getGLPIUserInfo(Long.parseLong((String) assignees), sessionToken));
    } else {
      ((JSONArray) assignees).forEach(object -> {
        String glpiUserId = (String) object;
        GLPIUsers.add(getGLPIUserInfo(Long.parseLong(glpiUserId), sessionToken));
      });
    }
    return GLPIUsers;
  }

  private GlpiUser getGLPIUserInfo(long glpiUserId, String sessionToken) {
    long startTime = System.currentTimeMillis();
    HttpGet httpGet = initHttpGet("/User/" + glpiUserId, sessionToken);
    if (httpGet == null) {
      return null;
    }
    try {
      HttpResponse httpResponse = httpClient.execute(httpGet);
      String responseString = new BasicResponseHandler().handleResponse(httpResponse);
      EntityUtils.consume(httpResponse.getEntity());
      JSONObject jsonResponse = new JSONObject(responseString);
      GlpiUser glpiUser = new GlpiUser();
      glpiUser.setId(jsonResponse.getLong("id"));
      glpiUser.setName((jsonResponse.getString("name")));
      Object firstName = jsonResponse.get("firstname");
      Object lastName = jsonResponse.get("realname");
      glpiUser.setFirstName(firstName != null ? String.valueOf(firstName) : null);
      glpiUser.setLastName(lastName != null ? String.valueOf(lastName) : null);
      return glpiUser;
    } catch (HttpResponseException e) {
      LOG.error("remote_service={} operation={} parameters=\"glpiUserId:{}, sessionToken:{}, status=ko "
                      + "duration_ms={} error_msg=\"{}, status : {} \"",
              GLPI_SERVICE_API,
              "getGLPIUser",
              glpiUserId,
              sessionToken,
              System.currentTimeMillis() - startTime,
              e.getReasonPhrase(),
              e.getStatusCode());
    } catch (Exception e) {
      LOG.error("Error while getting GLPI user info", e);
    }
    return null;
  }
  
  private HttpGet initHttpGet(String uri, String sessionToken) {
    GLPISettings glpiSettings = getCurrentGLPISettings();
    if (glpiSettings == null || sessionToken == null) {
      return null;
    }
    HttpGet httpTypeRequest = new HttpGet(glpiSettings.getServerApiUrl() + uri);
    httpTypeRequest.setHeader("Session-Token", sessionToken);
    httpTypeRequest.setHeader("App-Token", glpiSettings.getAppToken());
    return httpTypeRequest;
  }

  @Override
  public InputStream readTicketImageDocument(long imageId, String userIdentityId) {
    long startTime = System.currentTimeMillis();
    String sessionToken = initSession(getUserToken(userIdentityId));
    HttpGet httpGet = initHttpGet("/Document/" + imageId + "?alt=media", sessionToken);
    if (httpGet == null) {
      return null;
    }
    try {
      httpGet.setHeader("Accept", "application/octet-stream");
      HttpResponse httpResponse = httpClient.execute(httpGet);
      byte[] content = httpResponse.getEntity().getContent().readAllBytes();
      InputStream imageContent = new ByteArrayInputStream(content);
      EntityUtils.consume(httpResponse.getEntity());
      return imageContent;
    } catch (HttpResponseException e) {
      LOG.error("remote_service={} operation={} parameters=\"imageId:{}, sessionToken:{}, status=ko "
                      + "duration_ms={} error_msg=\"{}, status : {} \"",
              GLPI_SERVICE_API,
              "getGLPIUser",
              imageId,
              sessionToken,
              System.currentTimeMillis() - startTime,
              e.getReasonPhrase(),
              e.getStatusCode());
    } catch (Exception e) {
      LOG.error("Error while reading GLPI ticket image document", e);
    } finally {
      killSession(sessionToken);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeUserToken(String userIdentityId) {
    if (userIdentityId == null) {
      throw new IllegalArgumentException("user identity id is mandatory");
    }
    settingService.remove(Context.USER.id(userIdentityId),
            GLPI_INTEGRATION_SETTING_SCOPE,
            GLPI_USER_TOKEN_SETTING_KEY);
  }

  private String initSession(String userToken) {
    long startTime = System.currentTimeMillis();
    GLPISettings glpiSettings = getCurrentGLPISettings();
    if (glpiSettings == null || userToken == null) {
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
      EntityUtils.consume(httpResponse.getEntity());
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
    HttpGet httpGet = initHttpGet("/killSession", sessionToken);
    if (httpGet == null) {
      return HttpURLConnection.HTTP_BAD_REQUEST;
    }
    try {
      HttpResponse httpResponse = httpClient.execute(httpGet);
      EntityUtils.consume(httpResponse.getEntity());
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
