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
  <div class="d-flex">
    <p class="pt-1 mb-auto mt-auto widget-text-header text-truncate">
      {{ $t('glpi.settings.last.requests.label') }}
    </p>
    <v-btn
      v-if="isAdmin && hover"
      class="ms-auto my-0 pt-1 icon-default-color"
      small
      icon
      @click="openSettingsDrawer">
      <v-icon
        size="20">
        fas fa-cog
      </v-icon>
    </v-btn>
  </div>
</template>

<script>
export default {
  props: {
    isAdmin: {
      type: Boolean,
      default: false
    },
    hover: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    openSettingsDrawer() {
      this.$emit('open-settings-drawer');
    }
  }
};
</script>
