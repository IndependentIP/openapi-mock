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
import React, { useState, useEffect } from "react";
import { Modal, Button } from "react-bootstrap";
//import ReactJson from "react-json-view";
//import PerfectScrollbar from "react-perfect-scrollbar";
import _ from "lodash";

import { connect } from "react-redux";
import { hideuploadStub, importStub } from "../actions";

const UploadStub = ({
  context,
  show,
  mode,
  handleClose,
  theme,
  importStub
}) => {
  const [data, setData] = useState({ rawData: {} });

  useEffect(() => {
    if (!show) {
      setData({ rawData: {} });
    }
  }, [show]);

  const onFileLoad = event => {
    const content = event.target.result;
    setData({ rawData: content });
  };
  const onChooseFile = (event, onLoadFileHandler) => {
    if (typeof window.FileReader !== "function")
      throw "The file API isn't supported on this browser.";
    let input = event.target;
    if (!input)
      throw "The browser does not properly implement the event object";
    if (!input.files)
      throw "This browser does not support the `files` property of the file input.";

    if (!input.files[0]) return undefined;
    let file = input.files[0];
    let fr = new FileReader();
    fr.onload = onLoadFileHandler;
    fr.readAsText(file);
  };


  const handleSave = () => {
    let sendData = [];
    if(data.rawData===0){
        console.log("No data to send");
        return;
    }
    importStub(data.rawData);
  };
  const modeClass = mode === "dard" ? "dard-mode" : "light-mode";

  return (
    <Modal
      size="lg"
      show={show}
      className={`${modeClass} uploadStub`}
      onHide={handleClose}
    >
      <Modal.Header closeButton>
        <Modal.Title>
          Upload stubs for{" "}
          <span className="badge badge-info">
            {(context ? _.startCase(context) : "Default") + " context"}
          </span>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        Please choose the exported file and we can play with it immediately:
        <br></br>
        <br></br>
        <input
          accept=".yaml"
          type="file"
          onChange={event =>
            onChooseFile(event, (elementId, event) =>
              onFileLoad(elementId, event)
            )
          }
        />
        <br></br>
        <br></br>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Close
        </Button>
        <Button variant="primary" onClick={handleSave}>
          Import
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default connect(
  state => ({
    show: state.modal.uploadStub.show,
    context: state.context
  }),
  {
    handleClose: hideuploadStub,
    importStub
  }
)(UploadStub);
