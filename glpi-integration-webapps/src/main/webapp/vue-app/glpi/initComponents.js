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

import GlpiApp from './components/GlpiApp.vue';
import GlpiAddSettings from './components/settings/GlpiAddSettings.vue';
import GlpiSettingsDrawer from './components/settings/GlpiSettingsDrawer.vue';
import GlpiHeader from './components/view/GlpiHeader.vue';
import GlpiUserConnection from './components/view/GlpiUserConnection.vue';
import GlpiUserConnectionDrawer from './components/view/GlpiUserConnectionDrawer.vue';
import GlpiTicketList from './components/ticket/GlpiTicketList.vue';
import GlpiTicketListItem from './components/ticket/GlpiTicketListItem.vue';
import GlpiFooter from './components/view/GlpiFooter.vue';

import * as glpiService from './js/GLPIService';

const components = {
  'glpi-app': GlpiApp,
  'glpi-add-settings': GlpiAddSettings,
  'glpi-settings-drawer': GlpiSettingsDrawer,
  'glpi-header': GlpiHeader,
  'glpi-user-connection': GlpiUserConnection,
  'glpi-user-connection-drawer': GlpiUserConnectionDrawer,
  'glpi-ticket-list': GlpiTicketList,
  'glpi-ticket-list-item': GlpiTicketListItem,
  'glpi-footer': GlpiFooter
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (!Vue.prototype.$glpiService) {
  window.Object.defineProperty(Vue.prototype, '$glpiService', {
    value: glpiService,
  });
}
