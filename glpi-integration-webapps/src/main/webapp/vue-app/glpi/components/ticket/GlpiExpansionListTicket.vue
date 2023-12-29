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
  <v-expansion-panels
    v-model="panel"
    accordion>
    <glpi-expansion-ticket-list-item
      v-for="ticket in tickets"
      :key="ticket.id"
      :ticket="ticket"
      :server-url="serverUrl"
      :status-icon="statusIcons.get(ticket?.status)" />
  </v-expansion-panels>
</template>

<script>
export default {
  props: {
    tickets: {
      type: Array,
      default: () => []
    },
    serverUrl: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      panel: null,
      statusIcons: new Map([
        ['NEW', {color: 'green--text', icon: 'fas fa-circle'}],
        ['PROCESSING_ASSIGNED',  {color: 'green--text', icon: 'far fa-circle'}],
        ['PROCESSING_PLANNED', {color: 'black--text', icon: 'far fa-calendar'}],
        ['PENDING', {color: 'orange--text', icon: 'fas fa-circle'}],
        ['SOLVED', {color: 'black--text', icon: 'far fa-circle'}],
        ['CLOSED', {color: 'black--text', icon: 'fas fa-circle'}]
      ])
    };
  },
  created() {
    this.$root.$on('set-panel-expanded', this.setPanelExpanded);
  },
  methods: {
    setPanelExpanded(index) {
      this.panel = index;
    }
  }
};
</script>>
