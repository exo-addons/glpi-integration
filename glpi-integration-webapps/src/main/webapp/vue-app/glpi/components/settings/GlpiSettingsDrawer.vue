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
    ref="glpiSettingsDrawer"
    allow-expand
    right>
    <template slot="title">
      <span class="text-color">
        {{ $t('glpi.create.connection.message') }}
      </span>
    </template>
    <template slot="content">
      <div class="pa-4">
        <div class="text-color">
          <p>
            {{ $t('glpi.enable.users.connection.message') }}
          </p>
          <p>
            {{ $t('glpi.users.connection.todo.message') }}
          </p>
        </div>
        <div class="mt-5">
          <v-form
            v-model="valid"
            ref="settingsForm">
            <v-label
              for="serverApiUrl">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('glpi.settings.server.api.url.label') }}
              </span>
            </v-label>
            <v-text-field
              v-model="glpiSettings.serverApiUrl"
              :rules="[rules.required]"
              name="serverApiUrl"
              class="mt-n3 mb-2"
              :placeholder="$t('glpi.settings.server.api.url.placeholder')"
              dense
              outlined />
            <v-label
              for="appToken">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('glpi.settings.app.token.label') }}
              </span>
            </v-label>
            <v-text-field
              v-model="glpiSettings.appToken"
              :rules="[rules.required]"
              name="appToken"
              class="mt-n3 mb-2"
              :placeholder="$t('glpi.settings.app.token.placeholder')"
              dense
              outlined />
            <v-label
              for="maxTicketsToDisplay">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('glpi.settings.max.tickets.display.label') }}
              </span>
            </v-label>
            <v-text-field
              v-model="glpiSettings.maxTicketsToDisplay"
              :rules="[rules.TicketsRule]"
              name="maxTicketsToDisplay"
              class="mt-n3 mb-2"
              type="number"
              min="1"
              max="10"
              :placeholder="$t('glpi.settings.max.tickets.display.hint.message')"
              dense
              outlined />
          </v-form>
        </div>
      </div>
    </template>
    <template slot="footer">
      <div class="ms-auto d-flex width-fit-content">
        <v-btn
          @click="closeDrawer"
          class="btn me-4">
          {{ $t('glpi.settings.cancel.label') }}
        </v-btn>
        <v-btn
          :loading="isSavingSettings"
          :disabled="!valid"
          class="btn btn-primary"
          @click="addGLPISettings">
          {{ $t('glpi.settings.validate.label') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  props: {
    isSavingSettings: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      valid: false,
      glpiSettings: {},
      rules: {
        TicketsRule: v => v <= 10 && v >=1 || this.$t('glpi.settings.max.tickets.display.hint.message'),
        required: v => !!v || this.$t('glpi.settings.form.required.error.message'),
      }
    };
  },
  created() {
    this.$root.$on('open-glpi-settings-drawer', this.openDrawer);
    this.$root.$on('close-glpi-settings-drawer', this.closeDrawer);
  },
  methods: {
    openDrawer(glpiSettings) {
      if (glpiSettings) {
        this.glpiSettings = glpiSettings;
      }
      this.$refs.glpiSettingsDrawer.open();
    },
    closeDrawer() {
      this.$refs.settingsForm.reset();
      this.$refs.glpiSettingsDrawer.close();
    },
    addGLPISettings() {
      this.$emit('save-glpi-settings', this.glpiSettings);
    }
  }
};
</script>
