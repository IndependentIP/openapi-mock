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
  USER_SETTING_UPDATE,
  USER_SETTING_TOGGLE_LAYOUT,
  LOAD_USER_SETTING,
  ADD_NEW_CONTEXT
} from "../actions/actions";
import { ofType, combineEpics } from "redux-observable";
import { map, mergeMap, concatAll } from "rxjs/operators";
import { updateUserSetting, updateUserSettingSuccess, addNewContextSuccess } from "../actions";
import { of, empty } from "rxjs";

const getUserSettingEpic = (action$, state$, { $getUserSetting }) =>
  action$.pipe(
    ofType(LOAD_USER_SETTING),
    mergeMap(() => {
      return $getUserSetting().pipe(
        map(({ data }) => updateUserSettingSuccess({userSettings:data}))
      );
    })
  );

const updateUserSettingEpic = (action$, state$, { saveUserSetting }) =>
  action$.pipe(
    ofType(USER_SETTING_UPDATE),
    mergeMap(({ payload }) => {
      return saveUserSetting(payload).pipe(
        concatAll(),
        map(({ data }) => {
          if (data.request.method === "OPTIONS") {
            return updateUserSettingSuccess();
          }
          const newSetting = JSON.parse(data.response.body);
          return updateUserSettingSuccess({userSettings:newSetting});
        })
      );
    })
  );

const updateUserSettingLayoutEpic = (action$, state$) =>
  action$.pipe(
    ofType(USER_SETTING_TOGGLE_LAYOUT),
    mergeMap(() => {
      let { layout } = state$.value.userSettings;
      layout = !layout || layout === "full" ? "standard" : "full";
      return of(
        updateUserSetting({
          ...state$.value.userSettings,
          layout
        })
      );
    })
  );

const addNewContextEpic = (action$, state$) =>
  action$.pipe(
    ofType(ADD_NEW_CONTEXT),
    mergeMap(({payload}) => {
      const { userSettings } = state$.value;


        let newUserSettings = {  ...userSettings };
        if (newUserSettings["contexts"]) {
          newUserSettings.contexts = Array.from(
            new Set([...newUserSettings.contexts, payload])
          );
        } else {
          newUserSettings.contexts = [payload];
        }

        return of(
          updateUserSetting(newUserSettings),
          addNewContextSuccess(payload));
      }
      
    )
  );
export default combineEpics(
  getUserSettingEpic,
  updateUserSettingEpic,
  updateUserSettingLayoutEpic,
  addNewContextEpic,
);
