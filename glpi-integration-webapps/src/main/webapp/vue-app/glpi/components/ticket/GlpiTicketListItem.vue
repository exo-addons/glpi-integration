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
  <v-row
    class="clickable text-color">
    <v-col class="px-0 py-2">
      <v-row
        no-gutters>
        <v-col class="py-1">
          <p class="mb-0 text-truncate">
            <span class="me-3">
              {{ ticket.id }}
            </span>
            <v-icon
              size="20"
              :class="statusIcon.color">
              {{ statusIcon.icon }}
            </v-icon>
            {{ $t(`glpi.ticket.status.${ticket.status}.label`) }}
          </p>
        </v-col>
        <v-col
          cols="12"
          class="pa-0">
          <p
            v-if="!expanded"
            class="text-truncate-2 body-2 mb-0">
            {{ ticket.title }}
          </p>
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: {
    ticket: {
      type: Object,
      default: null
    },
    statusIcon: {
      type: Object,
      default: null
    },
    expanded: {
      type: Boolean,
      default: false
    },
  },
};
</script>
