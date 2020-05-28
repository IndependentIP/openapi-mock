/*
 * Copyright © 2020 FUGA (mark.schenk@fuga.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import Axios from "axios-observable";

import { createStub } from "./WireMockService";
import { WMurl } from "../constant";


export function saveSetting(settings) {
  saveUserSetting(settings);
}
const userSettingURL = "/_administrator/userSetting";
const userSettingId = "cbe04d58-7419-41e0-be49-0d32f86715a0";

export function getUserSetting(callback) {
  fetch(WMurl + userSettingURL, { method: "GET" }).then(response => {
    response.json().then(json=>callback(json));
  });
}
export function $getUserSetting() {
  return Axios.get(WMurl +userSettingURL);
}

export const saveUserSetting = setting => {
  const requestSetting = {
    id: userSettingId,
    uuid: userSettingId,
    name: "setting",
    metadata: {},
    request: { urlPattern: userSettingURL, method: "GET" },
    response: {
      status: 200,
      body: JSON.stringify(setting),
      headers: { "content-type": "application/json" }
    },
    persistent: true
  };
  
return createStub(requestSetting);
};
