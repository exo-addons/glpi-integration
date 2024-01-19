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
  <div>
    <div class="mt-n1">
      <div>
        <v-icon
          class="d-block ms-n6 me-1 tertiary-color">
          fas fa-calendar-alt
        </v-icon>
        <date-format
          :value="lastUpdatedTime"
          :format="dateFormat"
          class="ms-1 mt-n5 font-weight-bold" />
      </div>
      <div class="ms-1 ">
        {{ ticket.title }}
      </div>
    </div>
    <div class="mt-2">
      <div>
        <v-icon
          class="d-block ms-n6 me-1 mt-auto tertiary-color">
          fas fa-file-alt
        </v-icon>
        <div class="mt-n5 font-weight-bold text-color">
          {{ $t('glpi.ticket.description.label') }}
        </div>
      </div>
      <div
        class="ms-1 rich-editor-content"
        v-sanitized-html="ticketDescriptionHtml"></div>
    </div>
    <div
      v-if="ticket.comments.length"
      class="mt-2">
      <div>
        <v-icon
          class="d-block ms-n6 me-1 mt-auto tertiary-color">
          fas fa-comment-dots
        </v-icon>
        <div class="mt-n5 ms-1 font-weight-bold text-color">
          {{ $t('glpi.ticket.lastComment.label') }}
        </div>
      </div>
      <div
        class="ms-1 rich-editor-content"
        v-sanitized-html="ticketLastComment"></div>
    </div>
    <div
      v-if="ticket.assignees.length"
      class="mt-2">
      <div>
        <v-icon
          size="23"
          class="d-block ms-n7 me-1 mt-auto tertiary-color">
          fas fa-user-cog
        </v-icon>
        <div class="mt-n5 ms-1 text-color">
          {{ $t('glpi.ticket.assignee.message') }}
          <v-chip
            v-for="user in ticket.assignees"
            :key="user.id"
            class="text-capitalize ma-auto"
            small
            outlined
            pill>
            {{ getAssigneeName(user) }}
          </v-chip>
          <span>
          </span>
        </div>
      </div>
    </div>
    <div class="d-flex mx-auto mt-3">
      <v-btn
        class="btn btn-primary mb-auto mx-auto"
        color="primary"
        target="_blank"
        flat
        outlined
        link
        :href="openTicketLink">
        <span class="text-capitalize-first-letter">
          {{ $t('glpi.ticket.open.message') }}
        </span>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    ticket: {
      type: Object,
      default: null
    },
    serverUrl: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      imgBaseUrl: '/portal/rest/v1/glpi-integration/image/',
      dateFormat: {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
      },
    };
  },
  computed: {
    ticketDescriptionHtml() {
      return this.ticket && this.decodeHtml(this.ticket?.content);
    },
    ticketLastComment() {
      return this.ticket && this.decodeHtml(this.ticket?.comments[0]);
    },
    lastUpdatedTime() {
      return this.ticket && new Date(this.ticket?.lastUpdated).getTime();
    },
    openTicketLink() {
      return this.ticket && `${this.serverUrl}/front/ticket.form.php?id=${this.ticket.id}`;
    },
  },
  methods: {
    getAssigneeName(assignee) {
      return assignee.firstName !== 'null' && assignee.lastName !== 'null'
                                           && `${assignee.firstName} ${assignee.lastName}` || assignee.name;
    },
    updateImagesURl(img) {
      const imgURl = img.getAttribute('src');
      const docId = new URLSearchParams(imgURl.substring(imgURl.indexOf('?') + 1)).get('docid');
      if (docId) {
        img.setAttribute('src', `${this.imgBaseUrl}${docId}?v=${this.lastUpdatedTime}`);
      }
    },
    decodeHtml(html) {
      const element = document.createElement('textarea');
      const wrapper = document.createElement('div');
      element.innerHTML = html;
      wrapper.innerHTML = element.value;
      wrapper.querySelectorAll('img').forEach(img => {
        this.updateImagesURl(img);
        const href = img.parentNode.getAttribute('href');
        img.parentNode.setAttribute('href',this.serverUrl + href);
      });
      return wrapper.innerHTML;
    },
  }
};
</script>
