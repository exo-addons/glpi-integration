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
    <v-hover v-slot="{ hover }">
      <v-card
        min-width="100%"
        max-width="100%"
        min-height="400"
        class="d-flex border-box-sizing flex-column pa-5 overflow-hidden position-relative card-border-radius"
        color="white"
        flat>
        <glpi-header
          :is-admin="isAdmin"
          :hover="hover"
          @open-settings-drawer="openSettingsDrawer" />
        <glpi-add-settings
          v-if="!hasGLPISettings"
          @open-settings-drawer="openSettingsDrawer" />
        <glpi-user-connection
          v-else />
      </v-card>
    </v-hover>
    <glpi-settings-drawer
      :is-saving-settings="isSavingSettings"
      ref="settingsDrawer"
      @save-glpi-settings="saveGLPISettings" />
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      isSavingSettings: false,
      isAdmin: false,
      glpiSettings: null
    };
  },
  beforeCreate() {
    this.$glpiService.getGLPISettings().then(response => {
      this.glpiSettings = response?.glpiSettings;
      this.isAdmin = response?.admin;
    });
  },
  computed: {
    showApplication() {
      return this.hasGLPISettings || !this.hasGLPISettings && this.isAdmin;
    },
    hasGLPISettings() {
      return !!this.glpiSettings;
    }
  },
  methods: {
    openSettingsDrawer() {
      this.$root.$emit('open-glpi-settings-drawer', this.glpiSettings);
    },
    closeSettingsDrawer() {
      this.$root.$emit('close-glpi-settings-drawer');
    },
    saveGLPISettings(glpiSettings) {
      this.isSavingSettings = true;
      return this.$glpiService.saveGLPISettings(glpiSettings).then((glpiSettings) => {
        this.glpiSettings = glpiSettings;
        this.$root.$emit('alert-message', this.$t('glpi.settings.saved.success.message'), 'success');
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('glpi.settings.saved.error.message'), 'error');
      }).finally(() => {
        this.isSavingSettings = false;
        this.closeSettingsDrawer();
      });
    }
  }
};
</script>
