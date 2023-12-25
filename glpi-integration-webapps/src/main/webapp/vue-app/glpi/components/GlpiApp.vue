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
      min-height="200"
      class="d-flex border-box-sizing flex-column pa-5 overflow-hidden position-relative card-border-radius"
      :loading="loading"
      loader-height="2"
      flat>
      <glpi-header
        v-if="!loading"
        :is-admin="isAdmin"
        :is-connected="hasValidGLPIUserToken"
        @open-settings-drawer="openSettingsDrawer"
        @open-list-ticket-drawer="openListTicketsDrawer" />
      <glpi-add-settings
        v-if="!hasGLPISettings"
        @open-settings-drawer="openSettingsDrawer" />
      <glpi-user-connection
        v-else-if="!hasValidGLPIUserToken"
        @open-connection-drawer="openUserConnectionDrawer" />
      <glpi-ticket-list
        v-else
        :tickets="tickets.slice(0, maxDisplayTickets)"
        :loading="loading" />
      <glpi-footer
        v-if="!loading && hasValidGLPIUserToken"
        :server-url="serverUrl"
        :is-connected="hasValidGLPIUserToken" />
    </v-card>
    <glpi-settings-drawer
      :is-saving-settings="isSavingSettings"
      ref="settingsDrawer"
      @save-glpi-settings="saveGLPISettings" />
    <glpi-user-connection-drawer
      :is-saving-token="isSavingUserToken"
      ref="userConnectionDrawer"
      @save-glpi-user-token="saveUserToken" />
    <glpi-list-ticket-darwer
      ref="listTicketDrawer"
      :tickets="tickets"
      :has-more="hasMore"
      :server-url="serverUrl"
      :loading="loading"
      @disconnect-user="removeUserToken"
      @load-more-tickets="loadMoreTickets" />
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      isSavingSettings: false,
      isSavingUserToken: false,
      isRemovingUserToken: true,
      isAdmin: false,
      hasValidUserToken: false,
      glpiSettings: null,
      tickets: [],
      pageSize: 8,
      limit: 0,
      offset: 0,
      loading: false,
      hasMore: false
    };
  },
  beforeCreate() {
    this.$glpiService.getGLPISettings().then(response => {
      this.glpiSettings = response?.glpiSettings;
      this.hasValidUserToken = response.hasValidUserToken;
      this.isAdmin = response.admin;
    });
  },
  watch: {
    isRemovingUserToken() {
      if (this.isRemovingUserToken) {
        this.$refs.listTicketDrawer.startLoading();
      } else {
        this.$refs.listTicketDrawer.endLoading();
      }
    }
  },
  created() {
    this.getTickets(0, this.pageSize);
  },
  computed: {
    showApplication() {
      return this.hasGLPISettings || !this.hasGLPISettings && this.isAdmin;
    },
    maxDisplayTickets() {
      return this.glpiSettings?.maxTicketsToDisplay;
    },
    hasGLPISettings() {
      return !!this.glpiSettings;
    },
    hasValidGLPIUserToken() {
      return this.hasValidUserToken;
    },
    serverUrl() {
      return this.glpiSettings?.serverApiUrl?.replace('/apirest.php','');
    }
  },
  methods: {
    openListTicketsDrawer() {
      this.$root.$emit('open-list-ticket-drawer');
    },
    loadMoreTickets() {
      this.offset = this.tickets.length || 0;
      this.limit = this.limit || this.pageSize;
      this.limit+= this.offset;
      this.getTickets(this.offset, this.limit);
    },
    getTickets(offset, limit) {
      this.loading = true;
      return this.$glpiService.getGLPITickets(offset, limit + 1).then(tickets => {
        this.tickets.push(...tickets);
        this.hasMore = tickets?.length > this.limit - this.offset;
      }).finally(() => this.loading = false);
    },
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
        this.getTickets(0, this.pageSize);
      }).catch((e) => {
        if (e.status === 401) {
          this.$root.$emit('alert-message', this.$t('glpi.user.token.not.valid.message'), 'error');
        } else {
          this.$root.$emit('alert-message', this.$t('glpi.user.token.saved.error.message'), 'error');
        }
      }).finally(() => {
        this.isSavingUserToken = false;
      });
    },
    removeUserToken() {
      this.isRemovingUserToken = true;
      return this.$glpiService.removeUserToken().then(() => {
        this.hasValidUserToken = false;
      }).finally(() => {
        this.isRemovingUserToken = false;
        this.$refs.listTicketDrawer.closeDrawer();
      });
    }
  }
};
</script>
