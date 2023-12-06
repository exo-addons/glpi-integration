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
  <v-app v-if="showApplication">
    <v-card
      min-width="100%"
      max-width="100%"
      min-height="400"
      class="d-flex border-box-sizing flex-column pa-3 overflow-hidden position-relative card-border-radius"
      color="white"
      flat>
      <glpi-add-settings v-if="!hasGLPISettings" />
    </v-card>
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      hasGLPISettings: false,
      isAdmin: false
    };
  },
  beforeCreate() {
    this.$glpiService.getGLPISettings().then(response => {
      this.hasGLPISettings = response?.glpiSettings !== null;
      this.isAdmin = response?.admin;
    });
  },
  computed: {
    showApplication() {
      return this.hasGLPISettings || !this.hasGLPISettings && this.isAdmin;
    }
  }
};
</script>
