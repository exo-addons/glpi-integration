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
      <span class="text-color">
        {{ $t('glpi.my.ticket.list.label') }}
      </span>
    </template>
    <template slot="content">
      <glpi-expansion-list-ticket
        :tickets="tickets"
        :server-url="serverUrl" />
    </template>
    <template
      v-if="hasMore"
      slot="footer">
      <div class="ma-auto d-flex width-fit-content">
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
  },
  methods: {
    loadMoreTickets() {
      this.$emit('load-more-tickets');
    },
    openDrawer() {
      this.$refs.glpiListTicketDrawer.open();
    }
  }
};
</script>
