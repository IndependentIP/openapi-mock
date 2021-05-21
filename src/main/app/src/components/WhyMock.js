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
import React, { PureComponent } from "react";
import logoOpenApiMock from "./../OpenAPI-Mock-logo.png";
import PerfectScrollbar from "react-perfect-scrollbar";
import _ from "lodash";
import { __esModule } from "react-treebeard/dist";
import querySearch from "stringquery";
import Select from "react-select";
import { SplitButton, Dropdown, Button } from "react-bootstrap";
import { connect } from "react-redux";

// Dependencies for icons within UI
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import Request from "./Request";
import Response from "./Response";
import EditStub from "./EditStub";
import ActionButton from "./ActionButton";
import CreateStub from "./CreateStub";

import TreeOfStubs from "./TreeOfStubs";
import Setting from "./Setting";
import About from "./About";
import Context from "./Context";
import GroupSharing from "./GroupSharing";
import UploadStub from "./UploadStub";
import {
  showAboutModal,
  updateMode,
  toggleLayout,
  showSettingModal,
  loadUserSetting,
  selectNode,
  loadAllMappings,
  resetMapping,
  uploadStub,
  showContextModal,
  toggleNode,
  toggleNodeById,
  switchNewContext
} from "../actions";

library.add(
  fas,
)

class WhyMock extends PureComponent {
  state = {
    activeStub: {},
    edit: false,
    delete: false,
    shareGroup: { showModal: false },
    uploadStub: { showModal: false }
  };

  changeHistoryUrl = (id, context) => {
    let params = [];
    if (id) {
      params.push(`id=${id}`);
    }
    if (context) {
      params.push(`context=${context}`);
    }
    this.props.history.push({
      pathname: "/",
      search: `?${_.join(params, "&")}`
    });
  };

  componentDidMount = () => {
    this.handleContextFromURLParams();
    this.props.loadAllMappings();
    this.props.loadUserSetting();
  };

  handleContextFromURLParams = () => {
    const { context } = querySearch(this.props.location.search);
    if (context) {
      this.props.switchNewContext(context);
    }
  };

  getContextFromURL = () => {
    const { context } = querySearch(this.props.location.search);
    return context;
  };

  selectNodeByURLId = () => {
    const { id } = querySearch(this.props.location.search);
        if (!id) {
          return;
        }
        this.props.toggleNodeById({
          node: { id },
          toggled: true
        });
  };

  componentDidUpdate() {
    // const { selectedNode } = this.props;
    // if (selectedNode && selectedNode.obj) {
    //   const { id } = querySearch(this.props.location.search);
    //   if (selectedNode.obj.id !== id) {
    //     this.selectNodeByURLId();
    //   }
    // }else{
    //   this.selectNodeByURLId();
    // }
  }

  

  handleSuggestedItemChanged = seletedItem => {
    if (!seletedItem.value) {
      return;
    }
    this.props.toggleNode({
      node: { hashId: seletedItem.value },
      toggled: true
    });
  };
  render() {
    const {
      userSettings: { mode, layout },
      context,
      suggestedItems
    } = this.props;

    return (
      <div className={mode === "dard" ? "dard-mode" : "light-mode"}>
        <div
          className={
            "container" + (!layout || layout === "standard" ? "" : "-fluid")
          }
        >
          <div className="fullHeight">
            <Header
              setMode={this.setMode}
              mode={mode}
              showSettingModal={this.props.showSettingModal}
              showAboutModal={this.props.showAboutModal}
              showContextModal={this.props.showContextModal}
              context={context}
              resetMocks={this.props.resetMapping}
              toggleLayout={this.props.toggleLayout}
              layout={layout}
              showUploadModal={this.props.uploadStub}
            >
              <SuggestedStubs
                suggestedItems={suggestedItems}
                onItemChanged={this.handleSuggestedItemChanged}
              />
            </Header>
            <div className="row tree-view">
              <PerfectScrollbar className="col-sm-4 border-right tree-view-content">
                <TreeOfStubs />
              </PerfectScrollbar>
              <div className="col-sm-8 view-panel">
                <div>
                  <Request />
                  <Response />
                </div>
              </div>
            </div>
            <EditStub />
            <CreateStub />
            <ActionButton />
            <Setting></Setting>
            <About></About>
            <Context></Context>
            <GroupSharing />
            <UploadStub></UploadStub>
            <Footer />
          </div>
        </div>
      </div>
    );
  }
}

const Header = ({
  children,
  setMode,
  mode,
  context,
  showSettingModal,
  showAboutModal,
  showContextModal,
  resetMocks,
  toggleLayout,
  layout,
  showUploadModal
}) => (
  <div className="row header-row">
    <div className="col-sm-2 header-img">
      <img width="100%" alt="OpenApi-Mock" src={logoOpenApiMock} />
    </div>
    <div className="col-sm-6 suggestionZone">{children}</div>
    <div className="col-sm-4 right-toolbar">
      <SplitButton
        alignRight
        onClick={showContextModal}
        title={"As " + (context ? _.startCase(context) : "Mocker")}
        size="sm"
        className="button-header"
        variant="secondary"
      >
        <Dropdown.Item href="#/action-1" onClick={showContextModal}>
          <i><FontAwesomeIcon icon={['fas', 'rocket']}/></i> Change context
        </Dropdown.Item>
        <Dropdown.Item href="#/action-1" onClick={resetMocks}>
          <i><FontAwesomeIcon icon={['fas', 'eraser']}/></i> Reset
        </Dropdown.Item>
        <Dropdown.Item href="#/action-1" onClick={showUploadModal}>
          <i><FontAwesomeIcon icon={['fas', 'upload']}/></i> Upload mocks
        </Dropdown.Item>
        <Dropdown.Item href="#/action-2" onClick={showSettingModal}>
          <i><FontAwesomeIcon icon={['fas', 'cog']}/></i> Setting
        </Dropdown.Item>
        <Dropdown.Divider />
        <Dropdown.Item href="#/action-3" onClick={showAboutModal}>
          <i><FontAwesomeIcon icon={['fas', 'info-circle']}/></i> About
        </Dropdown.Item>
      </SplitButton>
      <Button
        className="btn btn-success btn-sm button-header-next"
        onClick={showUploadModal}>
        <i><FontAwesomeIcon icon={['fas', 'upload']}/></i>
      </Button>
      <Button
        className="btn btn-info btn-sm button-header-next"
        onClick={toggleLayout}
      >
        <i><FontAwesomeIcon icon={['fas',
            (!layout || layout === "standard" ? "expand" : "compress")]}/></i>
      </Button>
    </div>
  </div>
);
const Footer = () => (
  <div className="row  footer header-row">
    <div className="col-sm-12 header-img" />
  </div>
);
const SuggestedStubs = ({ suggestedItems, onItemChanged }) => {
  return suggestedItems && suggestedItems.length > 0 ? (
    <Select
      options={suggestedItems}
      placeholder="search..."
      onChange={onItemChanged}
    />
  ) : null;
};

export default connect(
  ({ userSettings, mapping, context, ui }) => ({
    userSettings,
    mapping,
    context,
    suggestedItems: ui.suggestedItems,
    selectedNode: ui.selectedNode
  }),
  {
    updateMode,
    toggleLayout,
    loadUserSetting,
    showAboutModal,
    showSettingModal,
    selectNode,
    resetMapping,
    loadAllMappings,
    uploadStub,
    showContextModal,
    toggleNode,
    toggleNodeById,
    showSettingModal,
    switchNewContext
  }
)(WhyMock);
