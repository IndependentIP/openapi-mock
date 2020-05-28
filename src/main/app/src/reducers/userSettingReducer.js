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
import {
  USER_SETTING_UPDATE_MODE,
  USER_SETTING_UPDATE_SUCCESS
} from "../actions/actions";

const initialState = {
  mode: "light",
  layout: "standard",
  jsonTheme: "rjv",
  jsonEditTheme: "",
  contexts: []
};

export default (state = initialState, action) => {
  switch (action.type) {
    case USER_SETTING_UPDATE_MODE:
      return {
        ...state,
        mode: action.payload
      };
    case USER_SETTING_UPDATE_SUCCESS:
      if (action.payload) {
         console.log("update success, payload", action.payload);
        return {
          ...state,
          ...action.payload.userSettings
        };
      }
    default:
      return state;
  }
};
