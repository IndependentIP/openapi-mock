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
import { Observable } from "rxjs";
import Axios from "axios-observable";
import { from, of } from "rxjs";

import { WMurl } from "../constant";

export const $getMappings = (parameters) => {
    return Axios.get(WMurl + "/__admin/mappings" + (parameters ? "?" + parameters : ""));
};
export const deleteStub = (stubId, callback) => {
  fetch(WMurl + "/__admin/mappings/" + stubId, {
    method: "DELETE"
  }).then(response => {
    callback();
  });
};

export const $deleteStub = (stubId) => {
  return Axios.delete(WMurl + "/__admin/mappings/" + stubId);
};

export const saveStub = (stub, callback) => {
  fetch(WMurl + "/__admin/mappings/" + stub.id, {
    method: "PUT",
    body: JSON.stringify(stub)
  }).then(response => {
    callback();
  });
};

export const $saveStub = (stub) => {
 
  return Axios.put(WMurl + "/__admin/mappings/" + stub.id,stub);

};
export const $resetMapping = () => {
  return Axios.post(WMurl + "/__admin/mappings/reset");

};
export const importStub = (stubs, callback) => {
  fetch(WMurl + "/__admin/mappings/import", {
    method: "POST",
    body: JSON.stringify(stubs)
  }).then(response => {
    callback();
  });
};

export const $importStub = (stubs) => {
  return Axios.post(WMurl + "/__admin/mappings/import", stubs);
};

export const createStub = data => {
 return of(createOptionStub(data), createSpecificStub(data));
}
const createSpecificStub = (data, callback) => {
  return Axios.post(WMurl + "/__admin/mappings/", data);
};
const createOptionStub = data => {
  const optionData = { ...data };
  optionData.request = { ...data.request };
  optionData.request.method = "OPTIONS";
  optionData.persistent = false;
  return Axios.post(WMurl + "/__admin/mappings/", optionData);
};
