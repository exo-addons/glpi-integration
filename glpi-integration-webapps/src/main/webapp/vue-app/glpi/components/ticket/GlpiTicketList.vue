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
  <div class="my-auto">
    <v-container
      v-if="tickets.length">
      <glpi-ticket-list-item
        v-for="ticket in tickets"
        :key="ticket.id"
        :ticket="ticket" />
    </v-container>
    <div
      v-else
      class="align-center">
      <v-progress-circular
        class="mx-auto"
        indeterminate
        color="primary" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    tickets: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      statusIcons: new Map([
        ['NEW', {color: 'green--text', icon: 'fas fa-circle'}],
        ['PROCESSING_ASSIGNED',  {color: 'green--text', icon: 'far fa-circle'}],
        ['PROCESSING_PLANNED', {color: 'primary--text', icon: 'fas fa-calendar-alt'}],
        ['PENDING', {color: 'orange--text', icon: 'fas fa-circle'}],
        ['SOLVED', {color: 'black--text', icon: 'far fa-circle'}],
        ['CLOSED', {color: 'black--text', icon: 'fas fa-circle'}]
      ])
    };
  }
};
</script>
