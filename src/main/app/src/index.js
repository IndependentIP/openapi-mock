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
"use strict";
import React from "react";
import ReactDOM from "react-dom";
import "./index.css";
import "bootstrap/dist/css/bootstrap.css";
import "font-awesome/css/font-awesome.min.css";
import { createStore, applyMiddleware, compose } from "redux";
import { Provider } from "react-redux";
import { createEpicMiddleware } from "redux-observable";
import { createBrowserHistory } from "history";
import { routerMiddleware } from "connected-react-router";

import App from "./App";
import rootReducer from "./reducers";
import rootEpic from "./epics";
import {saveUserSetting,$getUserSetting} from './services/userProfileService';
import {$resetMapping,$getMappings,$importStub,$saveStub , createStub , $deleteStub} from './services/WireMockService';

const epicMiddleware = createEpicMiddleware({
  dependencies:{
    saveUserSetting,
    $getUserSetting,
    $resetMapping,
    $getMappings,
    $importStub,
    $saveStub,
    createStub,
    $deleteStub,
  }
});

const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

const history = createBrowserHistory();

const store = createStore(
  rootReducer,
  composeEnhancers(applyMiddleware(
    routerMiddleware(history),
    epicMiddleware,
    ))
);


epicMiddleware.run(rootEpic);

ReactDOM.render(
  <Provider store={store}>
    <App />
  </Provider>,
  document.getElementById("root")
);
