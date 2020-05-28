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
import ReactJson from "react-json-view";
import PerfectScrollbar from 'react-perfect-scrollbar';
import { connect } from "react-redux";

const ignoredHeaders = ["Access-Control-Allow-Origin","Access-Control-Allow-Methods","Access-Control-Allow-Headers"];
const Response = ({ node, theme }) => {
  
  if (node && node.obj) {
    Object.keys(node.obj.response.headers).filter(header=> {
      if(ignoredHeaders.filter(h=>h === header).length > 0){
        delete node.obj.response.headers[header];
      }
    });
    return (
      <div className="block-area block-area-response">
        <nav aria-label="breadcrumb">
          <ol className="breadcrumb">
            <span className="breadcrumb-item active" aria-current="page">
              Mock response
            </span>
          </ol>
        </nav>
        <PerfectScrollbar>
          <ReactJson
            theme={theme}
            collapsed={false}
            displayDataTypes={false}
            className="react-json-view"
            src={node.obj.response}
          />
          </PerfectScrollbar>
      </div>
    );
  }
  return null;
};

export default connect(state => ({
  node:state.ui.selectedNode.children? undefined: state.ui.selectedNode,
  context: state.context,
  theme: state.userSettings.jsonTheme,

}))(Response);
