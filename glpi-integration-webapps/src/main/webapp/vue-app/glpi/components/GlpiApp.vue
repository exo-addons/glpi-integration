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
          v-else-if="!hasValidGLPIUserToken"
          @open-connection-drawer="openUserConnectionDrawer" />
        <glpi-ticket-list
          v-else />
      </v-card>
    </v-hover>
    <glpi-settings-drawer
      :is-saving-settings="isSavingSettings"
      ref="settingsDrawer"
      @save-glpi-settings="saveGLPISettings" />
    <glpi-user-connection-drawer
      :is-saving-token="isSavingUserToken"
      ref="userConnectionDrawer"
      @save-glpi-user-token="saveUserToken" />
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      isSavingSettings: false,
      isSavingUserToken: false,
      isAdmin: false,
      hasValidUserToken: false,
      glpiSettings: null
    };
  },
  beforeCreate() {
    this.$glpiService.getGLPISettings().then(response => {
      this.glpiSettings = response?.glpiSettings;
      this.hasValidUserToken = response?.hasValidUserToken;
      this.isAdmin = response?.admin;
    });
  },
  computed: {
    showApplication() {
      return this.hasGLPISettings || !this.hasGLPISettings && this.isAdmin;
    },
    hasGLPISettings() {
      return !!this.glpiSettings;
    },
    hasValidGLPIUserToken() {
      return this.hasValidUserToken;
    }
  },
  methods: {
    openSettingsDrawer() {
      this.$root.$emit('open-glpi-settings-drawer', this.glpiSettings);
    },
    openUserConnectionDrawer() {
      this.$root.$emit('open-glpi-user-connection-drawer');
    },
    saveGLPISettings(glpiSettings) {
      this.isSavingSettings = true;
      return this.$glpiService.saveGLPISettings(glpiSettings).then((glpiSettings) => {
        this.glpiSettings = glpiSettings;
        this.$root.$emit('alert-message', this.$t('glpi.settings.saved.success.message'), 'success');
        this.$refs.settingsDrawer.closeDrawer();
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('glpi.settings.saved.error.message'), 'error');
      }).finally(() => {
        this.isSavingSettings = false;
      });
    },
    saveUserToken(token) {
      this.isSavingUserToken = true;
      return this.$glpiService.saveUserToken(token).then(() => {
        this.hasValidUserToken = true;
        this.$root.$emit('alert-message', this.$t('glpi.user.token.saved.success.message'), 'success');
        this.$refs.userConnectionDrawer.closeDrawer();
      }).catch((e) => {
        if (e.status === 401) {
          this.$root.$emit('alert-message', this.$t('glpi.user.token.not.valid.message'), 'error');
        } else {
          this.$root.$emit('alert-message', this.$t('glpi.user.token.saved.error.message'), 'error');
        }
      }).finally(() => {
        this.isSavingUserToken = false;
      });
    }
  }
};
</script>
