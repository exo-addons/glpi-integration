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
    ref="glpiUserConnectionDrawer"
    allow-expand
    right>
    <template slot="title">
      <span class="text-color">
        {{ $t('glpi.user.connection.message') }}
      </span>
    </template>
    <template slot="content">
      <div class="pa-4">
        <div class="text-color">
          <p>
            {{ $t('glpi.user.connection.todo.message') }}
          </p>
        </div>
        <div class="mt-5">
          <v-form
            v-model="valid"
            ref="connectionForm">
            <v-label
              for="userToken">
              <span class="text-subtitle-2 mt-5 mb-3">
                {{ $t('glpi.connection.user.token.label') }}
              </span>
            </v-label>
            <v-text-field
              v-model="token"
              :rules="[rules.required]"
              name="userToken"
              class="mt-n3 mb-2"
              :placeholder="$t('glpi.connection.user.token.placeholder')"
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
          :loading="isSavingToken"
          :disabled="!valid"
          class="btn btn-primary"
          @click="saveUserToken">
          {{ $t('glpi.settings.validate.label') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    isSavingToken: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      valid: false,
      token: null,
      rules: {
        required: v => !!v || this.$t('glpi.settings.form.required.error.message'),
      }
    };
  },
  created() {
    this.$root.$on('open-glpi-user-connection-drawer', this.openDrawer);
  },
  methods: {
    openDrawer() {
      this.$refs.glpiUserConnectionDrawer.open();
    },
    closeDrawer() {
      this.$refs.connectionForm.reset();
      this.$refs.glpiUserConnectionDrawer.close();
    },
    saveUserToken() {
      this.$emit('save-glpi-user-token', this.token);
    }
  }
};
</script>

