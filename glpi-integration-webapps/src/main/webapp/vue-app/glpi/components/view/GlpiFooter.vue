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
    <v-btn
      class="btn btn-primary mb-auto mx-auto"
      color="primary"
      target="_blank"
      flat
      outlined
      link
      :href="createTicketLink">
      <span class="text-capitalize">{{ $t('glpi.ticket.create.label') }}</span>
    </v-btn>
  </div>
</template>

<script>
export default {
  props: {
    isConnected: {
      type: Boolean,
      default: false
    },
    serverUrl: {
      type: String,
      default: ''
    }
  },
  computed: {
    createTicketLink() {
      return `${this.serverUrl}front/ticket.form.php`;
    }
  }
};
</script>
