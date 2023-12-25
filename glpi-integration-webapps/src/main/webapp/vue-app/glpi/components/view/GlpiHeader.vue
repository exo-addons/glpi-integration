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
    <div
      v-if="canSeeAll"
      class="mt-auto ms-auto">
      <v-btn
        class="pa-1 body-2"
        color="primary"
        small
        text
        @click="seeAll"
        @mouseover="showAdminControls"
        @focusin="showAdminControls">
        {{ $t('glpi.settings.see.all.label') }}
      </v-btn>
    </div>
    <div
      v-if="canEditAdminConfig && isAdmin"
      role="button"
      tabIndex="0"
      class="ms-auto d-flex"
      @mouseleave="hideAdminControls"
      @focusout="hideAdminControls">
      <v-btn
        class="ms-auto me-0 my-0 pt-1 icon-default-color"
        small
        icon
        @click="openListTicketDrawer">
        <v-icon
          size="20">
          fas fa-external-link-alt
        </v-icon>
      </v-btn>
      <v-btn
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
  </div>
</template>

<script>
export default {
  data() {
    return {
      showMenu: false,
      hover: false
    };
  },
  props: {
    isAdmin: {
      type: Boolean,
      default: false
    },
    isConnected: {
      type: Boolean,
      default: false,
    }
  },
  computed: {
    canEditAdminConfig() {
      return this.isAdmin && this.showMenu;
    },
    showSeeAll() {
      return !this.showMenu || !this.isAdmin;
    },
    canSeeAll() {
      return this.isConnected && this.showSeeAll;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
  },
  methods: {
    openListTicketDrawer() {
      this.$emit('open-list-ticket-drawer');
    },
    openSettingsDrawer() {
      this.$emit('open-settings-drawer');
    },
    showAdminControls() {
      if (this.isMobile || !this.isAdmin) {
        return;
      }
      this.seeAll();
    },
    hideAdminControls() {
      this.showMenu = false;
    },
    seeAll() {
      if (!this.isAdmin) {
        this.openListTicketDrawer();
        return;
      }
      this.showMenu = true;
    }
  }
};
</script>
