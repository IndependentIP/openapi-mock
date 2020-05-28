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
export const download = (content, fileName) => {
  const c = "data:text/json;charset=utf-8," + content;
  var encodedUri = encodeURI(c);
  var link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", fileName + ".json");
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export const _getHashId = () => uuidv4();

function uuidv4() {
  return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(c) {
    var r = (Math.random() * 16) | 0,
      v = c == "x" ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
