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
import React from "react";
import "./App2.css";

export const App2 = () => {
  return (
    <div className="container">
      <Header />
    </div>
  );
};

const Header = () => {
  return (
    <div className="row header-row">
      <div className="col-sm-3 header-img" />
      <div className="col-sm-9" />
    </div>
  );
};
