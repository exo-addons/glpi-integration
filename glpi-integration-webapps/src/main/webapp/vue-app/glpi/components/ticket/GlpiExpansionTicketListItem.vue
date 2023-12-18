<!--
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
-->

<template>
  <v-expansion-panel
    class="px-5">
    <v-expansion-panel-header
      class="pa-0">
      <template #default="{ open }">
        <v-container class="py-0">
          <glpi-ticket-list-item
            :expanded="open"
            :ticket="ticket"
            :status-icon="statusIcon" />
        </v-container>
      </template>
      <template #actions>
        <v-icon
          class="ms-2 icon-default-color"
          size="28">
          $expand
        </v-icon>
      </template>
    </v-expansion-panel-header>
    <v-expansion-panel-content>
      <glpi-ticket-details
        :ticket="ticket"
        :server-url="serverUrl" />
    </v-expansion-panel-content>
  </v-expansion-panel>
</template>

<script>
export default {
  props: {
    ticket: {
      type: Array,
      default: () => []
    },
    statusIcon: {
      type: Object,
      default: () => null
    },
    serverUrl: {
      type: String,
      default: null
    }
  }
};
</script>
