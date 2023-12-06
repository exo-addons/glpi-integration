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

package org.exoplatform.glpi.rest;

import org.exoplatform.glpi.model.GLPISettings;
import org.exoplatform.glpi.rest.model.GLPISettingsEntity;
import org.exoplatform.glpi.service.GLPIService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GLPIRestServiceTest {

  @Mock
  private GLPIService                                  glpiService;

  private GLPIRestService                              glpiRestService;

  private static final MockedStatic<ConversationState> CONVERSATION_STATE = mockStatic(ConversationState.class);

  @Mock
  private Identity                                     identity;

  @Before
  public void setUp() {
    glpiRestService = new GLPIRestService(glpiService);
    ConversationState conversationState = mock(ConversationState.class);
    CONVERSATION_STATE.when(ConversationState::getCurrent).thenReturn(conversationState);
    CONVERSATION_STATE.when(conversationState::getIdentity).thenReturn(identity);
  }

  @AfterClass
  public static void afterRunBare() {
    CONVERSATION_STATE.close();
  }

  @Test
  public void saveGLPISettings() {
    GLPISettingsEntity glpiSettingsEntity = new GLPISettingsEntity("url", "token", 10);
    Response response = glpiRestService.saveGLPISettings(null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    response = glpiRestService.saveGLPISettings(glpiSettingsEntity);
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    doThrow(new RuntimeException()).when(glpiService).saveGLPISettings("url", "token", 10);
    response = glpiRestService.saveGLPISettings(glpiSettingsEntity);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  @Test
  public void getGLPISettings() {
    when(identity.isMemberOf(anyString())).thenReturn(true);
    GLPISettings glpiSettings = new GLPISettings("url", "token", 10);
    when(glpiService.getGLPISettings()).thenReturn(null);
    Response response = glpiRestService.getGLPISettings();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    when(glpiService.getGLPISettings()).thenReturn(glpiSettings);
    response = glpiRestService.getGLPISettings();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    when(glpiService.getGLPISettings()).thenThrow(RuntimeException.class);
    response = glpiRestService.getGLPISettings();
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }
}
