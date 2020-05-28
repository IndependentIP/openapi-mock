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
import _ from "lodash";

import {
  Container,
  Button,
  lightColors,
  darkColors
} from "react-floating-action-button";
import { connect } from "react-redux";
import { statusDISABLE } from "../constant/index";
import {
  editStubModal,
createStub,
deleteStub,
duplicateStub,
disableStub,
enableStub,
shareStub,
importStub,
shareGroupStub,
exportStub,
exportGroup,
} from '../actions';
// Dependencies for icons within UI
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
library.add(
  fas,
)
const ActionButton = ({
  selectedNode,
  edit,
  enable,
  disable,
  hasDelete,
  createStub,
  editStub,
  duplicateStub,
  deleteStub,
  disableStub,
  enableStub,
  shareStub,
  shareGroupStub,
  isGroupSelected,
  exportStub,
  exportGroup
}) => {
  return (
    <Container>
      <Button
        href="#"
        tooltip="Create new stub"
        styles={{
          backgroundColor: darkColors.cyan,
          color: lightColors.white
        }}
        onClick={createStub}
      >
        <FontAwesomeIcon icon={['fas', 'plus']}/>
      </Button>
      {edit && (
        <Button
          href="#"
          tooltip="Duplicate new stub"
          styles={{
            backgroundColor: darkColors.cyan,
            color: lightColors.white
          }}
          onClick={()=>duplicateStub(selectedNode.obj)}
          >
            <FontAwesomeIcon icon={['fas', 'copy']}/>
          </Button>
      )}
      {hasDelete && (
        <Button
          href="#"
          tooltip="Delete stub"
          onClick={deleteStub}
        >
          <FontAwesomeIcon icon={['fas', 'trash']}/>
        </Button>
      )}
      {edit && (
        <Button
          href="#"
          onClick={editStub}
          tooltip="Edit mapping"
          styles={{
            backgroundColor: darkColors.teal,
            color: lightColors.white
          }}
          disabled={!edit}
        >
          <FontAwesomeIcon icon={['fas', 'edit']}/>
        </Button>
      )}
      {disable && (
        <Button
          href="#"
          tooltip="Enable this stub"
          styles={{
            backgroundColor: darkColors.blue,
            color: lightColors.green
          }}
          onClick={enableStub}
        >
          <FontAwesomeIcon icon={['fas', 'play-circle']}/>
        </Button>
      )}
      {enable && (
        <Button
          href="#"
          tooltip="Disable this stub"
          styles={{
            backgroundColor: darkColors.orange,
            color: lightColors.purple
          }}
          onClick={disableStub}
        >
          <FontAwesomeIcon icon={['fas', 'stop-circle']}/>
        </Button>
      )}
      {hasDelete && (
        <Button
          href="#"
          tooltip="Share this stub to context"
          onClick={()=>shareStub(selectedNode.obj)}
          styles={{
            backgroundColor: darkColors.cyan,
            color: lightColors.white
          }}
        >
          <FontAwesomeIcon icon={['fas', 'share-alt']}/>
        </Button>
      )}
      {hasDelete && (
        <Button
          href="#"
          tooltip="Download the stub"
          onClick={exportStub}
          styles={{
            backgroundColor: darkColors.cyan,
            color: lightColors.white
          }}
        >
          <FontAwesomeIcon icon={['fas', 'download']}/>
        </Button>
      )}
      {isGroupSelected && (
        <Button
          href="#"
          tooltip="Download the group"
          onClick={exportGroup}
          styles={{
            backgroundColor: darkColors.cyan,
            color: lightColors.white
          }}
        >
          <FontAwesomeIcon icon={['fas', 'download']}/>
        </Button>
      )}
      {isGroupSelected && (
        <Button
          href="#"
          tooltip="Share group to context"
          onClick={()=>shareGroupStub(selectedNode)}
          styles={{
            backgroundColor: darkColors.cyan,
            color: lightColors.white
          }}
        >
                  <FontAwesomeIcon icon={['fas', 'share-alt-square']}/>
                </Button>
      )}
      <Button
        styles={{
          backgroundColor: darkColors.blue,
          color: lightColors.green
        }}
        tooltip="What do you want?"
        style={{ color: "green" }}
        rotate={true}
      >
        <FontAwesomeIcon icon={['fas', 'location-arrow']}/>
      </Button>
    </Container>
  );
};

export default connect(
  ({ ui: { selectedNode } }) => ({
    selectedNode: selectedNode,
    edit: !selectedNode.children && selectedNode.hashId,
    hasDelete: !selectedNode.children && selectedNode.hashId,
    disable: _.get(selectedNode, "obj.metadata.status", "") === statusDISABLE,
    enable: !selectedNode.children && selectedNode.hashId && _.get(selectedNode, "obj.metadata.status", "") !== statusDISABLE,
    isGroupSelected: true && selectedNode.children
  }),
  {
    editStub:editStubModal,
    createStub,
    deleteStub,
    duplicateStub,
    disableStub,
    enableStub,
    shareStub,
    importStub,
    shareGroupStub,
    exportStub,
    exportGroup
  }
)(ActionButton);
