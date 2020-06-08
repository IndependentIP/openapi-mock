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
import { Modal, Button } from "react-bootstrap";
import JSONInput from "react-json-editor-ajrm";
import locale from "react-json-editor-ajrm/locale/en";
import Select from "react-select";
import { useState, useEffect, useRef } from "react";
import FormControl from "react-bootstrap/FormControl";
import _ from "lodash";

const PLACEHOLDERDATA = {
  request: {
    urlPattern: "/path",
    method: "GET"
  },
  response: {
    status: 200,
    headers: {
      "content-type": "application/json"
    },
    body: { field: "value" }
  }
};

export const ImportStub = props => {
  const [userEnterResponse, setUserEnterResponse] = useState(
    _.cloneDeep(PLACEHOLDERDATA.response)
  );
  const [response, setResponse] = useState(
    _.cloneDeep(PLACEHOLDERDATA.response)
  );

  const groupNameRef = useRef();
  const entryNameRef = useRef();
  const urlPatternRef = useRef();
  const methodNameRef = useRef("GET");
  useEffect(() => {
    handleDefaultValue();
  }, [props.initialData]);

  const handleDefaultValue = () => {
    if (props.initialData && props.initialData.metadata) {
      const { initialData } = props;
      groupNameRef.current.value = initialData.metadata.specification;
      entryNameRef.current.value = initialData.name;
      methodNameRef.current.value = initialData.request.method;
      urlPatternRef.current.value = initialData.request.urlPattern;
      setResponse(initialData.response);
      setUserEnterResponse(initialData.response);
    } else {
      setResponse(_.cloneDeep(PLACEHOLDERDATA.response));
    }
  };

  const handleClose = () => {
    methodNameRef.current.value = "GET";
    setResponse(PLACEHOLDERDATA.response);
    props.handleClose();
  };
  const handleSave = () => {
    const data = {
      name: entryNameRef.current.value || Math.random() * (+100 - +1) + +1,
      metadata: {
        specification: groupNameRef.current.value || "unknown"
      },
      request: {
        urlPattern: urlPatternRef.current.value,
        method: methodNameRef.current.value
      },
      response: userEnterResponse,
      persistent: true
    };
    if (typeof data.response.body !== "string") {
      data.response.body = JSON.stringify(data.response.body);
    }
    props.saveChangeHandler(data);
  };
  const selectGroup = seletedItem => {
    if (seletedItem.value <= 0) {
      return;
    }
    groupNameRef.current.value = seletedItem.value;
  };
  const { groups } = props;
  if (!groups) {
    return null;
  }
  const modeClass = props.mode === "dard" ? "dard-mode" : "";
  return (
    <Modal
      size="lg"
      show={props.show}
      onHide={handleClose}
      className={modeClass}
    >
      <Modal.Header closeButton>
        <Modal.Title>Import mock data</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="row">
          <div className="col-sm-12">
            <div className="card">
              <div className="card-body">
                <div className="card-header">Mock data
                </div>
                <div className="input-group mb-3">
                  <FormControl
                    as="textarea"
                    placeholder="paste HAR here..."
                    ref={entryNameRef}
                    rows="15"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Close
        </Button>
        <Button variant="primary" onClick={handleSave}>
          Save Stub
        </Button>
      </Modal.Footer>
    </Modal>
  );
};
