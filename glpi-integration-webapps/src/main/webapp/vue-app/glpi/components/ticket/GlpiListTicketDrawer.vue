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
  <exo-drawer
    ref="glpiListTicketDrawer"
    allow-expand
    right>
    <template slot="title">
      <span class="text-color mt-1">
        {{ $t('glpi.my.ticket.list.label') }}
      </span>
    </template>
    <template slot="titleIcons">
      <div class="d-flex">
        <v-btn
          target="_blank"
          flat
          outlined
          icon
          :href="createTicketLink">
          <v-icon
            class="icon-default-color icon-default-size"
            link
            :size="18">
            fas fa-plus
          </v-icon>
        </v-btn>
        <v-menu
          v-model="menu"
          transition="slide-x-reverse-transition"
          offset-y>
          <template #activator="{ on, attrs }">
            <v-btn
              icon
              v-bind="attrs"
              v-on="on">
              <v-icon
                class="icon-default-color icon-default-size"
                :size="18">
                fas fa-ellipsis-v
              </v-icon>
            </v-btn>
          </template>
          <v-list
            class="pa-0"
            dense>
            <v-list-item
              @click="disconnect">
              <v-list-item-title
                class="subtitle-2">
                {{ $t('glpi.user.disconnect.label') }}
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </template>
    <template slot="content">
      <glpi-expansion-list-ticket
        :tickets="tickets"
        :server-url="serverUrl" />
    </template>
    <template
      v-if="hasMore"
      slot="footer">
      <div class="ma-auto d-flex width-full">
        <v-btn
          :loading="loading"
          class="btn btn-primary width-full"
          flat
          outlined
          @click="loadMoreTickets">
          {{ $t('glpi.ticket.list.load.more.label') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      menu: false
    };
  },
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    tickets: {
      type: Array,
      default: () => []
    },
    serverUrl: {
      type: String,
      default: null
    },
    hasMore: {
      type: Boolean,
      default: false
    }
  },
  created() {
    this.$root.$on('open-list-ticket-drawer', this.openDrawer);
    document.addEventListener('mousedown',this.closeMenu, false);
  },
  computed: {
    createTicketLink() {
      return `${this.serverUrl}/front/ticket.form.php`;
    }
  },
  methods: {
    loadMoreTickets() {
      this.$emit('load-more-tickets');
    },
    openDrawer() {
      this.$refs.glpiListTicketDrawer.open();
    },
    closeDrawer() {
      this.$refs.glpiListTicketDrawer.close();
    },
    disconnect() {
      this.$emit('disconnect-user');
    },
    closeMenu() {
      if (this.menu) {
        setTimeout(() => {
          this.menu = false;
        }, 200);
      }
    }
  }
};
</script>
