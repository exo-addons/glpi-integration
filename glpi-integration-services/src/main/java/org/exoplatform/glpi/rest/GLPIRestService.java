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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.glpi.model.GLPISettings;
import org.exoplatform.glpi.rest.model.GLPISettingsEntity;
import org.exoplatform.glpi.rest.utils.EntityBuilder;
import org.exoplatform.glpi.service.GLPIService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

import java.io.InputStream;
import java.util.Date;

@Path("/v1/glpi-integration")
@Tag(name = "/v1/glpi-integration", description = "Manages GLPI Integration")
public class GLPIRestService implements ResourceContainer {

  private static final Log  LOG = ExoLogger.getLogger(GLPIRestService.class);

  private final GLPIService         glpiService;

  private static final int          CACHE_DURATION_SECONDS      = 31536000;

  private static final long         CACHE_DURATION_MILLISECONDS = CACHE_DURATION_SECONDS * 1000L;

  private static final CacheControl ILLUSTRATION_CACHE_CONTROL  = new CacheControl();

  public GLPIRestService(GLPIService glpiService) {
    this.glpiService = glpiService;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Path("/settings")
  @Operation(summary = "Save GLPI Settings", description = "Saves GLPI Settings", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Object saved"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response saveGLPISettings(@RequestBody(description = "GLPISettings settings object", required = true)
                                   GLPISettingsEntity glpiSettingsEntity) {
    if (glpiSettingsEntity == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("GLPISettings object is mandatory").build();
    }
    try {
      GLPISettings glpiSettings = glpiService.saveGLPISettings(glpiSettingsEntity.getServerApiUrl(),
                                                               glpiSettingsEntity.getAppToken(),
                                                               glpiSettingsEntity.getMaxTicketsToDisplay());
      return Response.status(Response.Status.CREATED).entity(EntityBuilder.toGLPISettingsEntity(glpiSettings, true)).build();
    } catch (Exception e) {
      LOG.error("Error while saving GLPI Settings", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Path("/settings")
  @Operation(summary = "Retrieves GLPI Settings", description = "Retrieves saved GLPI Settings", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "404", description = "Object not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getGLPISettings() {
    try {
      Identity identity = ConversationState.getCurrent().getIdentity();
      GLPISettings glpiSettings = glpiService.getGLPISettings();
      if (glpiSettings == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(EntityBuilder.toGLPISettingsResponseEntity(null, identity, glpiService))
                       .build();
      }
      return Response.ok(EntityBuilder.toGLPISettingsResponseEntity(glpiSettings, identity, glpiService)).build();
    } catch (Exception e) {
      LOG.error("Error while getting GLPI Settings", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Path("/token")
  @Operation(summary = "Save GLPI user token", description = "Save GLPI user token", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "not authorized"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response saveGLPIUserToken(@Parameter(description = "GLPI user token", required = true)
                                    String token) {
    if (StringUtils.isBlank(token)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("GLPI user token is mandatory").build();
    }
    Identity identity = ConversationState.getCurrent().getIdentity();
    try {
      if (!glpiService.isUserTokenValid(token)) {
        return Response.status(Response.Status.UNAUTHORIZED).entity("token is not valid").build();
      }
      glpiService.saveUserToken(token, identity.getUserId());
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.error("Error while saving GLPI user token", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Path("/tickets")
  @Operation(summary = "Retrieves GLPI ticket list", description = "Retrieves GLPI ticket list", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getGLPITickets(@Parameter(description = "Ticket list result limit")
                                 @DefaultValue("0") @QueryParam("offset") int offset,
                                 @Parameter(description = "Ticket list result offset")
                                 @DefaultValue("9") @QueryParam("limit") int limit) {
    Identity identity = ConversationState.getCurrent().getIdentity();
    try {
      return Response.ok(EntityBuilder.toGLPITicketListEntity(glpiService.getGLPITickets(offset, limit, identity.getUserId())))
                     .build();
    } catch (Exception e) {
      LOG.error("Error while getting GLPI tickets", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path( "/image/{docId}")
  @RolesAllowed("users")
  @Operation(
          summary = "Gets a GLPI ticket document image by its id",
          description = "Gets a GLPI ticket document image by its id",
          method = "GET")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "404", description = "Resource not found") })
  public Response getImageIllustration(@Context Request request,
                                       @Parameter(description = "workflow id", required = true) @PathParam("docId") Long docId,
                                       @Parameter(description = "Last modified parameter") @QueryParam("v") long lastModified) {

    if (docId == null) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Image doc id is mandatory").build();
    }
    Identity identity = ConversationState.getCurrent().getIdentity();
    try {
      EntityTag eTag = new EntityTag(String.valueOf(lastModified), true);
      Response.ResponseBuilder builder = request.evaluatePreconditions(eTag);
      if (builder == null) {
        InputStream inputStream = glpiService.readTicketImageDocument(docId, identity.getUserId());
        builder = Response.ok(inputStream, "image/png");
        builder.tag(eTag);
        if (lastModified > 0) {
          builder.lastModified(new Date(lastModified));
          builder.expires(new Date(System.currentTimeMillis() + CACHE_DURATION_MILLISECONDS));
          builder.cacheControl(ILLUSTRATION_CACHE_CONTROL);
        }
      }
      return builder.build();
    } catch (Exception e) {
      LOG.error("An error occurred while getting GLPI ticket document image", e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Path("/token")
  @Operation(summary = "Remove GLPI user token", description = "Remove GLPI user token", method = "DELETE")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "500", description = "Internal server error"),})
  public Response removeGLPIUserToken() {
    Identity identity = ConversationState.getCurrent().getIdentity();
    try {
      glpiService.removeUserToken(identity.getUserId());
      return Response.noContent().build();
    } catch (Exception e) {
      LOG.error("Error while saving GLPI user token", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
