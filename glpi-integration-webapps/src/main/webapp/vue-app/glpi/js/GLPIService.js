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
 *
*/

export function saveGLPISettings(GLPISettings) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/glpi-integration/settings`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(GLPISettings, (key, value) => {
      if (value !== null) {
        return value;
      }
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while saving GLPI Settings');
    } else {
      return resp.json();
    }
  });
}

export function getGLPISettings() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/glpi-integration/settings`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp?.ok && resp?.status !== 404) {
      throw resp;
    } else {
      return resp.json();
    }
  });
}

export function saveUserToken(token) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/glpi-integration/token`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: token,
  }).then(resp => {
    if (!resp?.ok) {
      throw resp;
    } else {
      return resp.text();
    }
  });
}

export function getGLPITickets(offset, limit) {
  const formData = new FormData();
  formData.append('offset', offset);
  if (limit) {
    formData.append('limit', limit);
  }
  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/glpi-integration/tickets?${params}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (!resp?.ok) {
      throw resp;
    } else {
      return resp.json();
    }
  });
}

export function removeUserToken() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/glpi-integration/token`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw resp;
    } else {
      return resp.text();
    }
  });
}
